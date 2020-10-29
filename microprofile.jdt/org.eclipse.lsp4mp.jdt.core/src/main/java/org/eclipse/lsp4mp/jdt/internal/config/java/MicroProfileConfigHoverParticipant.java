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
package org.eclipse.lsp4mp.jdt.internal.config.java;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_NAME;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.util.Ranges;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.jdt.core.java.hover.IJavaHoverParticipant;
import org.eclipse.lsp4mp.jdt.core.java.hover.JavaHoverContext;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProject;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProjectManager;
import org.eclipse.lsp4mp.jdt.core.project.MicroProfileConfigPropertyInformation;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 *
 * MicroProfile Config Hover
 *
 * @author Angelo ZERR
 *
 * @See https://github.com/eclipse/microprofile-config
 *
 */
public class MicroProfileConfigHoverParticipant implements IJavaHoverParticipant {

	@Override
	public boolean isAdaptedForHover(JavaHoverContext context, IProgressMonitor monitor) throws JavaModelException {
		// Hover is done only if microprofile-config is on the classpath
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, CONFIG_PROPERTY_ANNOTATION) != null;
	}

	@Override
	public Hover collectHover(JavaHoverContext context, IProgressMonitor monitor) throws CoreException {
		IJavaElement hoverElement = context.getHoverElement();
		if (hoverElement.getElementType() != IJavaElement.FIELD
				&& hoverElement.getElementType() != IJavaElement.LOCAL_VARIABLE) {
			return null;
		}

		ITypeRoot typeRoot = context.getTypeRoot();
		IJDTUtils utils = context.getUtils();

		Position hoverPosition = context.getHoverPosition();
		IAnnotatable hoverField = (IAnnotatable) hoverElement;

		IAnnotation annotation = getAnnotation(hoverField, CONFIG_PROPERTY_ANNOTATION);

		if (annotation == null) {
			return null;
		}

		String annotationSource = ((ISourceReference) annotation).getSource();
		String propertyKey = getAnnotationMemberValue(annotation, CONFIG_PROPERTY_ANNOTATION_NAME);

		if (propertyKey == null) {
			return null;
		}

		ISourceRange r = ((ISourceReference) annotation).getSourceRange();
		int offset = annotationSource.indexOf(propertyKey);
		final Range propertyKeyRange = utils.toRange(typeRoot, r.getOffset() + offset, propertyKey.length());

		if (hoverPosition.equals(propertyKeyRange.getEnd())
				|| !Ranges.containsPosition(propertyKeyRange, hoverPosition)) {
			return null;
		}

		IJavaProject javaProject = typeRoot.getJavaProject();

		if (javaProject == null) {
			return null;
		}

		JDTMicroProfileProject mpProject = JDTMicroProfileProjectManager.getInstance()
				.getJDTMicroProfileProject(javaProject);
		List<MicroProfileConfigPropertyInformation> propertyInformation = getConfigPropertyInformation(propertyKey,
				annotation, mpProject);
		return new Hover(getDocumentation(propertyInformation, context.getDocumentFormat(),
				context.isSurroundEqualsWithSpaces()), propertyKeyRange);
	}

	/**
	 * Returns all the config property information for the given property key.
	 *
	 * Includes the information for all the different profiles.
	 *
	 * @param propertyKey              the property key without the profile
	 * @param configPropertyAnnotation the annotation that defines the config property
	 * @param project                  the project
	 * @return the config property information for the given property key
	 * @throws JavaModelException if an error occurs when accessing the config property annotation
	 */
	public static List<MicroProfileConfigPropertyInformation> getConfigPropertyInformation(String propertyKey,
			IAnnotation configPropertyAnnotation, JDTMicroProfileProject project) throws JavaModelException {

		List<MicroProfileConfigPropertyInformation> infos = project.getPropertyInformations(propertyKey);
		boolean defaultProfileDefined = false;

		for (MicroProfileConfigPropertyInformation info : infos) {
			if (info.getPropertyNameWithProfile().equals(propertyKey)) {
				defaultProfileDefined = true;
			}
		}

		if (!defaultProfileDefined) {
			infos.add(new MicroProfileConfigPropertyInformation(propertyKey,
					getAnnotationMemberValue(configPropertyAnnotation, CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE),
					configPropertyAnnotation.getResource().getName()));
		}

		return infos;
	}

	/**
	 * Returns documentation about the property keys and values provided in
	 * <code>propertyMap</code>
	 *
	 * @param propertyInformation the microprofile property information
	 * @param documentFormat      the document format
	 * @param insertSpacing       true if spacing should be inserted around the
	 *                            equals sign and false otherwise
	 *
	 * @return documentation about the property keys and values provided in
	 *         <code>propertyMap</code>
	 */
	public static MarkupContent getDocumentation(List<MicroProfileConfigPropertyInformation> propertyInformation,
			DocumentFormat documentFormat, boolean insertSpacing) {
		StringBuilder content = new StringBuilder();

		boolean markdown = DocumentFormat.Markdown.equals(documentFormat);
		buildDocumentation(propertyInformation, markdown, insertSpacing, content);
		return new MarkupContent(markdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, content.toString());
	}

	private static void buildDocumentation(List<MicroProfileConfigPropertyInformation> propertyInformation,
			boolean markdownSupported, boolean insertSpacing, StringBuilder content) {

		for (MicroProfileConfigPropertyInformation info : propertyInformation) {

			if (content.length() > 0) {
				content.append("  \n");
			}

			if (markdownSupported) {
				content.append("`");
			}

			content.append(info.getPropertyNameWithProfile());

			if (info.getValue() == null) {
				if (markdownSupported) {
					content.append("`");
				}
				content.append(" is not set");
			} else {
				if (insertSpacing) {
					content.append(" = ");
				} else {
					content.append("=");
				}
				content.append(info.getValue());
				if (markdownSupported) {
					content.append("`");
				}
				if (info.getConfigFileName() != null) {
					content.append(" ");
					if (markdownSupported) {
						content.append("*");
					}
					content.append("in");
					if (markdownSupported) {
						content.append("*");
					}
					content.append(" ");
					content.append(info.getConfigFileName());
				}
			}
		}
	}
}
