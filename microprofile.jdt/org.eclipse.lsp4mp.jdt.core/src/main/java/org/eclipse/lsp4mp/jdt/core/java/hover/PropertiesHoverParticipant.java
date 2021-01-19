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
package org.eclipse.lsp4mp.jdt.core.java.hover;

import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProject;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProjectManager;
import org.eclipse.lsp4mp.jdt.core.project.MicroProfileConfigPropertyInformation;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 *
 * Properties hover participant to hover properties values declared in
 * properties files.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesHoverParticipant implements IJavaHoverParticipant {

	private final String annotationName;

	private final String defaultValueAnnotationMemberName;

	private final String [] annotationMembers;

	public PropertiesHoverParticipant(String annotationName, String annotationMemberName) {
		this(annotationName, annotationMemberName, null);
	}

	/**
	 * The definition participant constructor.
	 * 
	 * @param annotationName                   the annotation name (ex :
	 *                                         org.eclipse.microprofile.config.inject.ConfigProperty)
	 * @param annotationMemberName             the annotation member name (ex :
	 *                                         name)
	 * @param defaultValueAnnotationMemberName the annotation member name for
	 *                                         default value and null otherwise.
	 */
	public PropertiesHoverParticipant(String annotationName, String annotationMemberName,
			String defaultValueAnnotationMemberName) {
		this(annotationName, new String [] { annotationMemberName }, defaultValueAnnotationMemberName);
	}

	/**
	 * The definition participant constructor.
	 *
	 * @param annotationName                   the annotation name (ex :
	 *                                         io.quarkus.scheduler.Scheduled)
	 * @param annotationMembers                a list of annotation members (ex :
	 *                                         cron, every, etc.
	 * @param defaultValueAnnotationMemberName the annotation member name for
	 *                                         default value and null otherwise.
	 *
	 */
	public PropertiesHoverParticipant(String annotationName, String [] annotationMembers,
			String defaultValueAnnotationMemberName) {
		this.annotationName = annotationName;
		this.annotationMembers = annotationMembers;
		this.defaultValueAnnotationMemberName = defaultValueAnnotationMemberName;
	}

	@Override
	public boolean isAdaptedForHover(JavaHoverContext context, IProgressMonitor monitor) throws JavaModelException {
		// Definition is done only if the annotation is on the classpath
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, annotationName) != null;
	}

	@Override
	public Hover collectHover(JavaHoverContext context, IProgressMonitor monitor) throws CoreException {
		IJavaElement hoverElement = context.getHoverElement();
		if (!isAdaptableFor(hoverElement)) {
			return null;
		}

		ITypeRoot typeRoot = context.getTypeRoot();
		IJDTUtils utils = context.getUtils();

		Position hoverPosition = context.getHoverPosition();
		IAnnotatable hoverField = (IAnnotatable) hoverElement;

		IAnnotation annotation = getAnnotation(hoverField, annotationName);

		if (annotation == null) {
			return null;
		}

		String annotationSource = ((ISourceReference) annotation).getSource();
		String propertyKey = null;
		Range propertyKeyRange = null;
		boolean found = false;
		for (String annotationMemberName : annotationMembers) {
			propertyKey = getAnnotationMemberValue(annotation, annotationMemberName);
			if (propertyKey != null) {
				ISourceRange r = ((ISourceReference) annotation).getSourceRange();
				Pattern memberPattern = Pattern.compile(".*[^\"]\\s*(" + annotationMemberName + ")\\s*=.*", Pattern.DOTALL);
				Matcher match = memberPattern.matcher(annotationSource);
				if (match.matches()) {
					int offset = annotationSource.indexOf(propertyKey, match.end(1));
					propertyKeyRange = utils.toRange(typeRoot, r.getOffset() + offset, propertyKey.length());

					if (!hoverPosition.equals(propertyKeyRange.getEnd())
							&& Ranges.containsPosition(propertyKeyRange, hoverPosition)) {
						found = true;
						break;
					}
				}
			}
		}

		if (!found) {
			return null;
		}

		IJavaProject javaProject = typeRoot.getJavaProject();

		if (javaProject == null) {
			return null;
		}

		// property references may be surrounded by curly braces in Java file
		propertyKey = propertyKey.replace("{", "").replace("}", "");

		JDTMicroProfileProject mpProject = JDTMicroProfileProjectManager.getInstance()
				.getJDTMicroProfileProject(javaProject);
		List<MicroProfileConfigPropertyInformation> propertyInformation = getConfigPropertyInformation(propertyKey,
				annotation, defaultValueAnnotationMemberName, typeRoot, mpProject, utils);
		return new Hover(getDocumentation(propertyInformation, context.getDocumentFormat(),
				context.isSurroundEqualsWithSpaces()), propertyKeyRange);
	}

	/**
	 * Returns true if the given hovered Java element is adapted for this
	 * participant and false otherwise.
	 * 
	 * <p>
	 * 
	 * By default this method returns true if the hovered annotation belongs to a
	 * Java field or local variable and false otherwise.
	 * 
	 * </p>
	 * 
	 * @param hoverElement the hovered Java element.
	 * 
	 * @return true if the given hovered Java element is adapted for this
	 *         participant and false otherwise.
	 */
	protected boolean isAdaptableFor(IJavaElement hoverElement) {
		return hoverElement.getElementType() == IJavaElement.FIELD
				|| hoverElement.getElementType() == IJavaElement.LOCAL_VARIABLE;
	}

	/**
	 * Returns all the config property information for the given property key.
	 *
	 * Includes the information for all the different profiles.
	 *
	 * @param propertyKey                      the property key without the profile
	 * @param annotation                       the annotation that defines the
	 *                                         config property
	 * @param defaultValueAnnotationMemberName the annotation member name for
	 *                                         default value and null otherwise.
	 * @param project                          the project
	 * @param utils                            the JDT LS utilities.
	 * @return the config property information for the given property key
	 * @throws JavaModelException if an error occurs when accessing the config
	 *                            property annotation
	 */
	private static List<MicroProfileConfigPropertyInformation> getConfigPropertyInformation(String propertyKey,
			IAnnotation annotation, String defaultValueAnnotationMemberName, ITypeRoot typeRoot,
			JDTMicroProfileProject project, IJDTUtils utils) throws JavaModelException {

		List<MicroProfileConfigPropertyInformation> infos = project.getPropertyInformations(propertyKey);
		boolean defaultProfileDefined = false;

		for (MicroProfileConfigPropertyInformation info : infos) {
			if (info.getPropertyNameWithProfile().equals(propertyKey)) {
				defaultProfileDefined = true;
			}
		}

		if (defaultValueAnnotationMemberName != null && !defaultProfileDefined) {
			infos.add(new MicroProfileConfigPropertyInformation(propertyKey,
					getAnnotationMemberValue(annotation, defaultValueAnnotationMemberName), utils.toUri(typeRoot),
					annotation.getResource().getName()));
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
	private static MarkupContent getDocumentation(List<MicroProfileConfigPropertyInformation> propertyInformation,
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
					String href = info.getSourceConfigFileURI();
					if (markdownSupported && href != null) {
						content.append("[");
					}
					content.append(info.getConfigFileName());
					if (markdownSupported && href != null) {
						content.append("]");
						content.append("(");
						content.append(href);
						content.append(")");
					}
				}
			}
		}
	}
}
