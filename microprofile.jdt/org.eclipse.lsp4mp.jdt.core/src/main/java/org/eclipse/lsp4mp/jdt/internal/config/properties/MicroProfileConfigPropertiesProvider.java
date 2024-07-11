/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.config.properties;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTIES_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTIES_ANNOTATION_PREFIX;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTIES_ANNOTATION_UNCONFIGURED_PREFIX;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getFirstAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.findType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getResolvedTypeName;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.isSimpleFieldType;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.jdt.core.IPropertiesCollector;
import org.eclipse.lsp4mp.jdt.core.SearchContext;

/**
 * Properties provider to collect MicroProfile properties from the Java fields
 * annotated with "org.eclipse.microprofile.config.inject.ConfigProperties"
 * annotation.
 * 
 * <code>
 * &#64;ConfigProperties(prefix="server")
&#64;Dependent
public class Details {
    public String host; // the value of the configuration property server.host
    public int port;   // the value of the configuration property server.port
    private String endpoint; //the value of the configuration property server.endpoint
    public @ConfigProperty(name="old.location")
    String location; //the value of the configuration property server.old.location
    ...
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://download.eclipse.org/microprofile/microprofile-config-2.0/microprofile-config-spec-2.0.html#_aggregate_related_properties_into_a_cdi_bean
 * @see https://github.com/eclipse/microprofile-config/blob/master/api/src/main/java/org/eclipse/microprofile/config/inject/ConfigProperties.java
 */
public class MicroProfileConfigPropertiesProvider extends MicroProfileConfigPropertyProvider {

	private static final String[] ANNOTATION_NAMES = { CONFIG_PROPERTIES_ANNOTATION };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation configPropertiesAnnotation,
			String annotationName, SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		switch (javaElement.getElementType()) {
		case IJavaElement.TYPE:
			// @ConfigProperties(prefix = "server3")
			// public class ServerConfigProperties
			generatePropertiesFromClassType((IType) javaElement, configPropertiesAnnotation, context, monitor);
			break;
		case IJavaElement.FIELD:
			// @ConfigProperties(prefix = "cloud")
			// ServerConfigProperties configPropertiesCloud;
			generatePropertiesFromField((IField) javaElement, configPropertiesAnnotation, context, monitor);
			break;
		}
	}

	/**
	 * Generate properties from the given class type annotated
	 * with @ConfigProperties.
	 * 
	 * <code>
	 * &#64;ConfigProperties(prefix = "server3")
	   public class ServerConfigProperties
	 * </code>
	 * 
	 * @param classType                  the class type.
	 * @param configPropertiesAnnotation the @ConfigProperties annotation.
	 * @param context                    the search context.
	 * @param monitor                    the progress monitor.
	 * @throws JavaModelException
	 */
	private void generatePropertiesFromClassType(IType classType, IAnnotation configPropertiesAnnotation,
			SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		String prefix = getPrefixFromAnnotation(configPropertiesAnnotation);
		populateConfigObject(classType, prefix, new HashSet<>(), context.getCollector(), monitor);
	}

	/**
	 * Generate properties from the given field annotated with @ConfigProperties.
	 * 
	 * <code>
	 * &#64;ConfigProperties(prefix = "cloud")
	 * ServerConfigProperties configPropertiesCloud;
	 * </code>
	 * 
	 * @param field                      the Java field.
	 * @param configPropertiesAnnotation the @ConfigProperties annotation.
	 * @param context                    the search context.
	 * @param monitor                    the progress monitor.
	 * @throws JavaModelException
	 */
	private void generatePropertiesFromField(IField field, IAnnotation configPropertiesAnnotation,
			SearchContext context, IProgressMonitor monitor) throws JavaModelException {

		String fieldTypeName = getResolvedTypeName(field);
		IType fieldType = findType(field.getJavaProject(), fieldTypeName);
		if (isSimpleFieldType(fieldType, fieldTypeName)) {
			return;
		}

		String prefix = getPrefixFromAnnotation(configPropertiesAnnotation);
		if (prefix == null) {
			// @ConfigProperties
			// ServerConfigProperties configProperties;
			//
			// @ConfigProperties(prefix = "server3")
			// public static class ServerConfigProperties {
			// public String host3;
			// ...

			// In this case the configProperties field must generate 'host3' property, we
			// ignore it to avoid duplicate properties, because 'host3' will be generated
			// in generatePropertiesFromClassType step
			return;
		}
		populateConfigObject(fieldType, prefix, new HashSet<>(), context.getCollector(), monitor);

	}

	private static String getPrefixFromAnnotation(IAnnotation configPropertiesAnnotation) throws JavaModelException {
		String prefix = getAnnotationMemberValue(configPropertiesAnnotation, CONFIG_PROPERTIES_ANNOTATION_PREFIX);
		return prefix == null || CONFIG_PROPERTIES_ANNOTATION_UNCONFIGURED_PREFIX.equals(prefix) ? null : prefix;
	}

	private void populateConfigObject(IType configPropertiesType, String prefix, Set<IType> typesAlreadyProcessed,
			IPropertiesCollector collector, IProgressMonitor monitor) throws JavaModelException {
		if (typesAlreadyProcessed.contains(configPropertiesType)) {
			return;
		}
		typesAlreadyProcessed.add(configPropertiesType);
		IJavaElement[] elements = configPropertiesType.getChildren();
		// Loop for each Java fields.
		for (IJavaElement child : elements) {
			if (child.getElementType() == IJavaElement.FIELD || child.getElementType() == IJavaElement.LOCAL_VARIABLE) {
				String fieldTypeName = getResolvedTypeName(child);
				IType fieldClass = findType(child.getJavaProject(), fieldTypeName);
				if (isSimpleFieldType(fieldClass, fieldTypeName)) {
					// Java simple type (int, String, etc...) generate a property.
					IAnnotation configPropertyAnnotation = getFirstAnnotation((IAnnotatable) child,
							CONFIG_PROPERTY_ANNOTATION);
					super.collectProperty(child, configPropertyAnnotation, prefix, true, collector);
				} else {
					// Class type, generate properties from this class type.
					IAnnotation configPropertyAnnotation = getFirstAnnotation((IAnnotatable) child,
							CONFIG_PROPERTY_ANNOTATION);
					String propertyName = super.getPropertyName(child, configPropertyAnnotation, prefix, true);
					populateConfigObject(fieldClass, propertyName, typesAlreadyProcessed, collector, monitor);
				}
			}
		}
	}
}
