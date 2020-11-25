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

import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;

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
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.util.Ranges;
import org.eclipse.lsp4mp.commons.MicroProfileDefinition;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 *
 * Abstract class for collecting Java definition participant from a given Java
 * annotation member.
 *
 * @author Angelo ZERR
 *
 */
public abstract class AbstractAnnotationDefinitionParticipant implements IJavaDefinitionParticipant {

	private final String annotationName;

	private final String annotationMemberName;

	/**
	 * The definition participant constructor.
	 * 
	 * @param annotationName       the annotation name (ex :
	 *                             org.eclipse.microprofile.config.inject.ConfigProperty)
	 * @param annotationMemberName the annotation member name (ex : name)
	 */
	public AbstractAnnotationDefinitionParticipant(String annotationName, String annotationMemberName) {
		this.annotationName = annotationName;
		this.annotationMemberName = annotationMemberName;
	}

	@Override
	public boolean isAdaptedForDefinition(JavaDefinitionContext context, IProgressMonitor monitor)
			throws JavaModelException {
		// Definition is done only if the annotation is on the classpath
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, annotationName) != null;
	}

	@Override
	public List<MicroProfileDefinition> collectDefinitions(JavaDefinitionContext context, IProgressMonitor monitor)
			throws CoreException {
		ITypeRoot typeRoot = context.getTypeRoot();
		IJavaProject javaProject = typeRoot.getJavaProject();
		if (javaProject == null) {
			return null;
		}

		// Get the hyperlinked element.
		// If user hyperlinks an annotation, member annotation which is bound a Java
		// field, the hyperlinked Java element is the Java field (not the member or the
		// annotation).
		IJavaElement hyperlinkedElement = context.getHyperlinkedElement();
		if (!isAdaptableFor(hyperlinkedElement)) {
			return null;
		}

		Position definitionPosition = context.getHyperlinkedPosition();

		// Try to get the annotation
		IAnnotation annotation = getAnnotation((IAnnotatable) hyperlinkedElement, annotationName);

		if (annotation == null) {
			return null;
		}

		// Try to get the annotation member value
		String annotationSource = ((ISourceReference) annotation).getSource();
		String annotationMemberValue = getAnnotationMemberValue(annotation, annotationMemberName);

		if (annotationMemberValue == null) {
			return null;
		}

		// Get the annotation member value range
		ISourceRange r = ((ISourceReference) annotation).getSourceRange();
		int offset = annotationSource.indexOf(annotationMemberValue);
		IJDTUtils utils = context.getUtils();
		final Range annotationMemberValueRange = utils.toRange(typeRoot, r.getOffset() + offset,
				annotationMemberValue.length());

		if (definitionPosition.equals(annotationMemberValueRange.getEnd())
				|| !Ranges.containsPosition(annotationMemberValueRange, definitionPosition)) {
			return null;
		}

		// Collect definitions
		return collectDefinitions(annotationMemberValue, annotationMemberValueRange, annotation, context, monitor);
	}

	/**
	 * Returns true if the given hyperlinked Java element is adapted for this
	 * participant and false otherwise.
	 * 
	 * <p>
	 * 
	 * By default this method returns true if the hyperlinked annotation belongs to
	 * a Java field or local variable and false otherwise.
	 * 
	 * </p>
	 * 
	 * @param hyperlinkedElement the hyperlinked Java element.
	 * 
	 * @return true if the given hyperlinked Java element is adapted for this
	 *         participant and false otherwise.
	 */
	protected boolean isAdaptableFor(IJavaElement hyperlinkedElement) {
		return hyperlinkedElement.getElementType() == IJavaElement.FIELD
				|| hyperlinkedElement.getElementType() == IJavaElement.LOCAL_VARIABLE;
	}

	/**
	 * Returns the definitions for the given annotation member value and null
	 * otherwise.
	 * 
	 * @param annotationMemberValue      the annotation member value content.
	 * @param annotationMemberValueRange the annotation member value range.
	 * @param annotation                 the hyperlinked annotation.
	 * @param context                    the definition context.
	 * @param monitor                    the progress monitor
	 * @return the definitions for the given annotation member value and null
	 *         otherwise.
	 * @throws JavaModelException
	 */
	protected abstract List<MicroProfileDefinition> collectDefinitions(String annotationMemberValue,
			Range annotationMemberValueRange, IAnnotation annotation, JavaDefinitionContext context,
			IProgressMonitor monitor) throws JavaModelException;
}
