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
package org.eclipse.lsp4mp.jdt.core.utils;

import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.FALLBACK_METHOD_FALLBACK_ANNOTATION_MEMBER;

import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

/**
 * Java annotations utilities.
 *
 * @author Angelo ZERR
 *
 */
public class AnnotationUtils {

	public static boolean hasAnnotation(IAnnotatable annotatable, String annotationName) throws JavaModelException {
		return getAnnotation(annotatable, annotationName) != null;
	}

	/**
	 * Returns the annotation from the given <code>annotatable</code> element with
	 * the given name <code>annotationName</code> and null otherwise.
	 *
	 * @param annotatable    the class, field which can be annotated.
	 * @param annotationName the annotation name
	 * @return the annotation from the given <code>annotatable</code> element with
	 *         the given name <code>annotationName</code> and null otherwise.
	 * @throws JavaModelException
	 */
	public static IAnnotation getAnnotation(IAnnotatable annotatable, String annotationName) throws JavaModelException {
		if (annotatable == null) {
			return null;
		}
		IAnnotation[] annotations = annotatable.getAnnotations();
		for (IAnnotation annotation : annotations) {
			if (isMatchAnnotation(annotation, annotationName)) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * Returns true if the given annotation match the given annotation name and
	 * false otherwise.
	 *
	 * @param annotation     the annotation.
	 * @param annotationName the annotation name.
	 * @return true if the given annotation match the given annotation name and
	 *         false otherwise.
	 */
	public static boolean isMatchAnnotation(IAnnotation annotation, String annotationName) {
		return annotationName.endsWith(annotation.getElementName());
	}

	/**
	 * Returns true if the given annotation match the given annotation name and
	 * false otherwise.
	 * 
	 * @param annotation     the annotation.
	 * @param annotationName the annotation name.
	 * @return true if the given annotation match the given annotation name and
	 *         false otherwise.
	 */
	public static boolean isMatchAnnotation(Annotation annotation, String annotationName) {
		return annotationName.endsWith(annotation.getTypeName().getFullyQualifiedName());
	}

	/**
	 * Returns the value of the given member name of the given annotation.
	 *
	 * @param annotation the annotation.
	 * @param memberName the member name.
	 * @return the value of the given member name of the given annotation.
	 * @throws JavaModelException
	 */
	public static String getAnnotationMemberValue(IAnnotation annotation, String memberName) throws JavaModelException {
		for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
			if (memberName.equals(pair.getMemberName())) {
				return pair.getValue() != null ? pair.getValue().toString() : null;
			}
		}
		return null;
	}

	/**
	 * Returns the expression for the value of the given member name of the given annotation.
	 * 
	 * @param annotation the annotation.
	 * @param memberName the member name.
	 * @return the expression for the value of the given member name of the given annotation.
	 * @throws JavaModelException
	 */
	public static Expression getAnnotationMemberValueExpression(Annotation annotation, String memberName)
			throws JavaModelException {
		if (annotation instanceof NormalAnnotation) {
			NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
			for (Object pair : normalAnnotation.values()) {
				MemberValuePair castPair = (MemberValuePair) pair;
				if (memberName.equals(castPair.getName().toString())) {
					return castPair.getValue();
				}
			}
			return null;
		} else if (annotation instanceof SingleMemberAnnotation) {
			SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) annotation;
			return singleMemberAnnotation.getProperty(memberName) != null ? singleMemberAnnotation.getValue() : null;
		}
		// MarkerAnnotation has no members
		return null;
	}

}
