/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_NAME;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.hasAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.findType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getEnclosedType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getPropertyType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getResolvedTypeName;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceField;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceMethod;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.isBinary;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.jdt.core.AbstractAnnotationTypeReferencePropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.IPropertiesCollector;
import org.eclipse.lsp4mp.jdt.core.SearchContext;

/**
 * Properties provider to collect MicroProfile properties from the Java fields
 * annotated with "org.eclipse.microprofile.config.inject.ConfigProperty"
 * annotation.
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfileConfigPropertyProvider extends AbstractAnnotationTypeReferencePropertiesProvider {

	private static final String[] ANNOTATION_NAMES = { CONFIG_PROPERTY_ANNOTATION };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation configPropertyAnnotation,
			String annotationName, SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		if (javaElement.getElementType() == IJavaElement.FIELD
				|| javaElement.getElementType() == IJavaElement.LOCAL_VARIABLE) {
			// Generate the property only class is not annotated with @ConfigProperties
			IType classType = (IType) javaElement.getAncestor(IJavaElement.TYPE);
			boolean hasConfigPropertiesAnnotation = hasAnnotation(classType, CONFIG_PROPERTIES_ANNOTATION);
			if (!hasConfigPropertiesAnnotation) {
				collectProperty(javaElement, configPropertyAnnotation, null, false, context.getCollector());
			}
		}
	}

	protected void collectProperty(IJavaElement javaElement, IAnnotation configPropertyAnnotation, String prefix,
			boolean useFieldNameIfAnnotationIsNotPresent, IPropertiesCollector collector) throws JavaModelException {
		String propertyName = getPropertyName(javaElement, configPropertyAnnotation, prefix,
				useFieldNameIfAnnotationIsNotPresent);
		if (propertyName != null && !propertyName.isEmpty()) {
			String defaultValue = configPropertyAnnotation != null
					? getAnnotationMemberValue(configPropertyAnnotation, CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE)
					: null;
			collectProperty(javaElement, propertyName, defaultValue, collector);
		}
	}

	protected String getPropertyName(IJavaElement javaElement, IAnnotation configPropertyAnnotation, String prefix,
			boolean useFieldNameIfAnnotationIsNotPresent) throws JavaModelException {
		if (configPropertyAnnotation != null) {
			return getPropertyName(getAnnotationMemberValue(configPropertyAnnotation, CONFIG_PROPERTY_ANNOTATION_NAME),
					prefix);
		} else if (useFieldNameIfAnnotationIsNotPresent) {
			return getPropertyName(javaElement.getElementName(), prefix);
		}
		return null;
	}

	public static String getPropertyName(String propertyName, String prefix) {
		return StringUtils.isNotEmpty(prefix) ? (prefix + "." + propertyName) : propertyName;
	}

	private void collectProperty(IJavaElement javaElement, String name, String defaultValue,
			IPropertiesCollector collector) throws JavaModelException {
		IJavaProject javaProject = javaElement.getJavaProject();
		String varTypeName = getResolvedTypeName(javaElement);
		IType varType = findType(javaProject, varTypeName);
		String type = getPropertyType(varType, varTypeName);
		String description = null;
		String sourceType = getSourceType(javaElement);
		String sourceField = null;
		String sourceMethod = null;

		String extensionName = null;

		if (javaElement.getElementType() == IJavaElement.FIELD) {
			sourceField = getSourceField(javaElement);
		} else if (javaElement.getElementType() == IJavaElement.LOCAL_VARIABLE) {
			ILocalVariable localVariable = (ILocalVariable) javaElement;
			IMethod method = (IMethod) localVariable.getDeclaringMember();
			sourceMethod = getSourceMethod(method);
		}

		// Enumerations
		IType enclosedType = getEnclosedType(varType, type, javaProject);
		super.updateHint(collector, enclosedType);

		boolean binary = isBinary(javaElement);
		super.addItemMetadata(collector, name, type, description, sourceType, sourceField, sourceMethod, defaultValue,
				extensionName, binary);
	}

}
