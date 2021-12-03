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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.internal.core.ImportContainerInfo;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.util.Ranges;

/**
 * Java annotations utilities.
 *
 * @author Angelo ZERR
 *
 */
public class AnnotationUtils {

	private static final String ATTRIBUTE_VALUE = "value";

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
		// Annotation name is the fully qualified name of the annotation class (ex :
		// org.eclipse.microprofile.config.inject.ConfigProperties)
		// - when IAnnotation comes from binary, IAnnotation#getElementName() =
		// 'org.eclipse.microprofile.config.inject.ConfigProperties'
		// - when IAnnotation comes from source, IAnnotation#getElementName() =
		// 'ConfigProperties'
		if (!annotationName.endsWith(annotation.getElementName())) {
			return false;
		}
		if (annotationName.equals(annotation.getElementName())) {
			return true;
		}
		// Here IAnnotation comes from source and match only 'ConfigProperties', we must
		// check if the CU declares the proper import (ex : import
		// org.eclipse.microprofile.config.inject.ConfigProperties;)
		return isMatchAnnotationFullyQualifiedName(annotation, annotationName);
	}

	private static boolean isMatchAnnotationFullyQualifiedName(IAnnotation annotation, String annotationName) {

		// The clean code should use resolveType:

		// IJavaElement parent = annotation.getParent();
		// if (parent instanceof IMember) {
		// IType declaringType = parent instanceof IType ? (IType) parent : ((IMember)
		// parent).getDeclaringType();
		// String elementName = annotation.getElementName();
		// try {
		// String[][] fullyQualifiedName = declaringType.resolveType(elementName);
		// return annotationName.equals(fullyQualifiedName[0][0] + "." +
		// fullyQualifiedName[0][1]);
		// } catch (JavaModelException e) {
		// }
		// }

		// But for performance reason, we check if the import of annotation name is
		// declared

		ICompilationUnit unit = (ICompilationUnit) annotation.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (unit == null) {
			return false;
		}
		IImportContainer container = unit.getImportContainer();
		if (container == null) {
			return false;
		}

		// The following code uses JDT internal class and looks like
		// ICompilationUnit#getImports()
		// To avoid creating an array of IImportDeclaration, we do the following code:

		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		Object info = manager.getInfo(container);
		if (info == null) {
			if (manager.getInfo(unit) != null) {
				// CU was opened, but no import container, then no imports
				// return NO_IMPORTS;
				return false;
			} else {
				try {
					unit.open(null);
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // force opening of CU
				info = manager.getInfo(container);
				if (info == null)
					// after opening, if no import container, then no imports
					// return NO_IMPORTS;
					return false;
			}
		}
		IJavaElement[] elements = ((ImportContainerInfo) info).getChildren();
		for (IJavaElement child : elements) {
			IImportDeclaration importDeclaration = (IImportDeclaration) child;
			if (importDeclaration.isOnDemand()) {
				String fqn = importDeclaration.getElementName();
				String qualifier = fqn.substring(0, fqn.lastIndexOf('.'));
				if (qualifier.equals(annotationName.substring(0, annotationName.lastIndexOf('.')))) {
					return true;
				}
			} else if (importDeclaration.getElementName().equals(annotationName)) {
				return true;
			}
		}
		return false;
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
	 * Returns the expression for the value of the given member name of the given
	 * annotation.
	 *
	 * @param annotation the annotation.
	 * @param memberName the member name.
	 * @return the expression for the value of the given member name of the given
	 *         annotation.
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
			return ATTRIBUTE_VALUE.equals(memberName) || singleMemberAnnotation.getProperty(memberName) != null
					? singleMemberAnnotation.getValue()
					: null;
		}
		// MarkerAnnotation has no members
		return null;
	}

	/**
	 * Retrieve the value and range of an annotation member given a supported list
	 * of annotation members
	 *
	 * @param annotation            the annotation of the retrieved members
	 * @param annotationSource      the qualified name of the annotation
	 * @param annotationMemberNames the supported members of the annotation
	 * @param position              the hover position
	 * @param typeRoot              the java type root
	 * @param utils                 the utility to retrieve the member range
	 *
	 * @return an AnnotationMemberInfo object if the member exists, null otherwise
	 * @throws JavaModelException
	 */
	public static AnnotationMemberInfo getAnnotationMemberAt(IAnnotation annotation, String[] annotationMemberNames,
			Position position, ITypeRoot typeRoot, IJDTUtils utils) throws JavaModelException {
		String annotationSource = ((ISourceReference) annotation).getSource();
		ISourceRange r = ((ISourceReference) annotation).getSourceRange();
		String annotationMemberValue = null;
		for (String annotationMemberName : annotationMemberNames) {
			annotationMemberValue = getAnnotationMemberValue(annotation, annotationMemberName);
			if (annotationMemberValue != null) {
				// A regex is used to match the member and member value to find the position
				Pattern memberPattern = Pattern.compile(".*[^\"]\\s*(" + annotationMemberName + ")\\s*=.*",
						Pattern.DOTALL);
				Matcher match = memberPattern.matcher(annotationSource);
				if (match.matches()) {
					int offset = annotationSource.indexOf(annotationMemberValue, match.end(1));
					Range range = utils.toRange(typeRoot, r.getOffset() + offset, annotationMemberValue.length());

					if (!position.equals(range.getEnd()) && Ranges.containsPosition(range, position)) {
						return new AnnotationMemberInfo(annotationMemberValue, range);
					}
				}
			}
		}

		return null;

	}

}
