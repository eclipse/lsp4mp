/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.internal.graphql.java;

import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.isMatchAnnotation;

import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4mp.jdt.core.java.validators.JavaASTValidator;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;
import org.eclipse.lsp4mp.jdt.internal.graphql.MicroProfileGraphQLConstants;

/**
 * Diagnostics for microprofile-graphql.
 *
 * @see https://download.eclipse.org/microprofile/microprofile-graphql-1.0/microprofile-graphql.html
 */
public class MicroProfileGraphQLASTValidator extends JavaASTValidator {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileGraphQLASTValidator.class.getName());

	private static final String NO_VOID_MESSAGE = "Methods annotated with microprofile-graphql''s `@Query` cannot have ''void'' as a return type.";

	@Override
	public boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		IJavaProject javaProject = context.getJavaProject();
		// Check if microprofile-graphql is on the path
		return JDTTypeUtils.findType(javaProject, MicroProfileGraphQLConstants.QUERY_ANNOTATION) != null;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		try {
			validateMethod(node);
		} catch (JavaModelException e) {
			LOGGER.log(Level.WARNING, "An exception occurred when attempting to validate the annotation marked method");
		}
		super.visit(node);
		return true;
	}

	private void validateMethod(MethodDeclaration node) throws JavaModelException {
		List modifiers = node.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof Annotation) {
				Annotation annotation = (Annotation) modifier;
				if (isMatchAnnotation(annotation, MicroProfileGraphQLConstants.QUERY_ANNOTATION)) {
					Type returnType = node.getReturnType2();
					if (returnType.isPrimitiveType() //
							&& PrimitiveType.VOID.equals(((PrimitiveType) returnType).getPrimitiveTypeCode())) {
						String message = MessageFormat.format(NO_VOID_MESSAGE, node.getName());
						super.addDiagnostic(message, //
								MicroProfileGraphQLConstants.DIAGNOSTIC_SOURCE, //
								returnType, //
								MicroProfileGraphQLErrorCode.NO_VOID_QUERIES, //
								DiagnosticSeverity.Error);
					}
				}
			}
		}
	}

}
