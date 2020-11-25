/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.core.java.definition;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.commons.MicroProfileDefinition;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProjectManager;
import org.eclipse.lsp4mp.jdt.core.project.MicroProfileConfigPropertyInformation;

/**
 * Java definition participant to go to the definition of the the property
 * declared in a member value of annotation.
 * 
 * For instance:
 * 
 * <ul>
 * <li>from Java annotation: &#64;ConfigProperty(name="foo.bar")</li>
 * <li>to properties file : foo.bar = 10</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class PropertiesDefinitionParticipant extends AbstractAnnotationDefinitionParticipant {

	public PropertiesDefinitionParticipant(String annotationName, String annotationAttributeName) {
		super(annotationName, annotationAttributeName);
	}

	@Override
	protected List<MicroProfileDefinition> collectDefinitions(String propertyKey, Range propertyKeyRange,
			IAnnotation annotation, JavaDefinitionContext context, IProgressMonitor monitor) throws JavaModelException {
		IJavaProject javaProject = context.getJavaProject();
		// Collect all properties files (properties, yaml files) where the given
		// property key is configured
		List<MicroProfileConfigPropertyInformation> infos = JDTMicroProfileProjectManager.getInstance()
				.getJDTMicroProfileProject(javaProject).getPropertyInformations(propertyKey);
		if (!infos.isEmpty()) {
			return infos.stream().map(info -> {
				MicroProfileDefinition definition = new MicroProfileDefinition();
				definition.setSelectPropertyName(info.getPropertyNameWithProfile());
				LocationLink location = new LocationLink();
				definition.setLocation(location);
				location.setTargetUri(info.getSourceConfigFileURI());
				location.setOriginSelectionRange(propertyKeyRange);
				return definition;
			}).collect(Collectors.toList());
		}
		return null;
	}

}
