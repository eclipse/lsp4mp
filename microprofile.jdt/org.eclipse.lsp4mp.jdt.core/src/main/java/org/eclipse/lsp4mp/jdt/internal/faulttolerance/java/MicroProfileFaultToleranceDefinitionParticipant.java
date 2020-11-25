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
package org.eclipse.lsp4mp.jdt.internal.faulttolerance.java;

import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.FALLBACK_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.FALLBACK_METHOD_FALLBACK_ANNOTATION_MEMBER;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.commons.MicroProfileDefinition;
import org.eclipse.lsp4mp.jdt.core.java.definition.AbstractAnnotationDefinitionParticipant;
import org.eclipse.lsp4mp.jdt.core.java.definition.JavaDefinitionContext;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.core.utils.PositionUtils;

/**
 *
 * MicroProfile Fallback Tolerance Definition to navigate from Java
 * file @Fallback/fallbackMethod to the Java method name.
 *
 * @author Angelo ZERR
 *
 * @See https://github.com/eclipse/microprofile-config
 *
 */
public class MicroProfileFaultToleranceDefinitionParticipant extends AbstractAnnotationDefinitionParticipant {

	public MicroProfileFaultToleranceDefinitionParticipant() {
		super(FALLBACK_ANNOTATION, FALLBACK_METHOD_FALLBACK_ANNOTATION_MEMBER);
	}

	@Override
	protected List<MicroProfileDefinition> collectDefinitions(String annotationValue, Range annotationValueRange,
			IAnnotation annotation, JavaDefinitionContext context, IProgressMonitor monitor) throws JavaModelException {
		IType type = getOwnerType(annotation);
		if (type != null) {
			ITypeRoot typeRoot = context.getTypeRoot();
			IJDTUtils utils = context.getUtils();
			for (IMethod method : type.getMethods()) {
				if (annotationValue.equals(method.getElementName())) {
					Range methodNameRange = PositionUtils.toNameRange(method, utils);
					MicroProfileDefinition definition = new MicroProfileDefinition();
					LocationLink location = new LocationLink();
					definition.setLocation(location);
					location.setTargetUri(utils.toUri(typeRoot));
					location.setTargetRange(methodNameRange);
					location.setTargetSelectionRange(methodNameRange);
					location.setOriginSelectionRange(annotationValueRange);
					return Arrays.asList(definition);
				}
			}
		}
		return null;
	}

	private static IType getOwnerType(IJavaElement element) {
		while (element != null) {
			if (element.getElementType() == IJavaElement.TYPE) {
				return (IType) element;
			}
			element = element.getParent();
		}
		return null;
	}

	@Override
	protected boolean isAdaptableFor(IJavaElement definitionElement) {
		return definitionElement.getElementType() == IJavaElement.METHOD;
	}
}
