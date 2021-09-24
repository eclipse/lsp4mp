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
package org.eclipse.lsp4mp.jdt.internal.faulttolerance.java;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.COMPLETION_STAGE_TYPE_UTILITY;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.FUTURE_TYPE_UTILITY;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.UNI_TYPE_UTILITY;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValueExpression;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.isMatchAnnotation;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.ASYNCHRONOUS_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.FALLBACK_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.FALLBACK_METHOD_FALLBACK_ANNOTATION_MEMBER;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.TIMEOUT_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.VALUE_ANNOTATION_MEMBER;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.java.MicroProfileFaultToleranceErrorCode.FALLBACK_METHOD_DOES_NOT_EXIST;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.java.MicroProfileFaultToleranceErrorCode.FAULT_TOLERANCE_DEFINITION_EXCEPTION;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4mp.jdt.core.java.validators.JavaASTValidator;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Collects diagnostics related to the <code>@Fallback</code> and
 * <code>@Asynchronous</code> annotations.
 */
public class MicroProfileFaultToleranceASTValidator extends JavaASTValidator {

	private static final String FALLBACK_ERROR_MESSAGE = "The referenced fallback method ''{0}'' does not exist.";

	private static final String ASYNCHRONOUS_ERROR_MESSAGE = "The annotated method ''{0}'' with @Asynchronous should return an object of type {1}.";

	private static final String TIMEOUT_ERROR_MESSAGE = "{0} shouldn't be lower than 0.";

	private final Map<TypeDeclaration, Set<String>> methodsCache;

	private final List<String> allowedReturnTypesForAsynchronousAnnotation;

	private static Logger LOGGER = Logger.getLogger(MicroProfileFaultToleranceASTValidator.class.getName());

	public MicroProfileFaultToleranceASTValidator() {
		super();
		this.methodsCache = new HashMap<>();
		this.allowedReturnTypesForAsynchronousAnnotation = new ArrayList<>(
				Arrays.asList(FUTURE_TYPE_UTILITY, COMPLETION_STAGE_TYPE_UTILITY));
	}

	@Override
	public boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		IJavaProject javaProject = context.getJavaProject();
		boolean adapted = JDTTypeUtils.findType(javaProject, FALLBACK_ANNOTATION) != null
				|| JDTTypeUtils.findType(javaProject, ASYNCHRONOUS_ANNOTATION) != null
				|| JDTTypeUtils.findType(javaProject, TIMEOUT_ANNOTATION) != null;
		if (adapted) {
			addAllowedReturnTypeForAsynchronousAnnotation(javaProject, UNI_TYPE_UTILITY);
		}
		return adapted;
	}

	private void addAllowedReturnTypeForAsynchronousAnnotation(IJavaProject javaProject, String returnType) {
		if (JDTTypeUtils.findType(javaProject, returnType) != null) {
			allowedReturnTypesForAsynchronousAnnotation.add(returnType);
		}
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

	@Override
	public boolean visit(TypeDeclaration type) {
		@SuppressWarnings("rawtypes")
		List modifiers = type.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof Annotation) {
				Annotation annotation = (Annotation) modifier;
				if (isMatchAnnotation(annotation, ASYNCHRONOUS_ANNOTATION)) {
					try {
						MethodDeclaration[] methods = type.getMethods();
						for (MethodDeclaration node : methods) {
							validateAsynchronousAnnotation(node, (MarkerAnnotation) modifier);
						}
					} catch (JavaModelException e) {
						LOGGER.log(Level.WARNING, "An exception occurred when attempting to validate the annotation");
					}
					break;
				}
			}
		}
		super.visit(type);
		return true;
	}

	/**
	 * Checks if the given method declaration has a supported annotation, and if so,
	 * provides diagnostics if necessary
	 *
	 * @param node The method declaration to validate
	 * @throws JavaModelException
	 */
	private void validateMethod(MethodDeclaration node) throws JavaModelException {
		@SuppressWarnings("rawtypes")
		List modifiers = node.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof Annotation) {
				Annotation annotation = (Annotation) modifier;
				if (isMatchAnnotation(annotation, FALLBACK_ANNOTATION)) {
					NormalAnnotation normAnnotation = (NormalAnnotation) modifier;
					validateFallbackAnnotation(node, normAnnotation);
				} else if (isMatchAnnotation(annotation, ASYNCHRONOUS_ANNOTATION)) {
					MarkerAnnotation markAnnotation = (MarkerAnnotation) modifier;
					validateAsynchronousAnnotation(node, markAnnotation);
				} else if (isMatchAnnotation(annotation, TIMEOUT_ANNOTATION)) {
					validateTimeoutAnnotation(node, annotation);
				}
			}
		}
	}

	/**
	 * Checks if the given method declaration has a fallback annotation, and if so,
	 * provides diagnostics for the fallbackMethod
	 *
	 * @param node       The method declaration to validate
	 * @param annotation The @Fallback annotation
	 * @throws JavaModelException
	 */
	private void validateFallbackAnnotation(MethodDeclaration node, NormalAnnotation annotation)
			throws JavaModelException {
		Expression fallbackMethodExpr = getAnnotationMemberValueExpression(annotation,
				FALLBACK_METHOD_FALLBACK_ANNOTATION_MEMBER);
		if (fallbackMethodExpr != null) {
			String fallbackMethodName = fallbackMethodExpr.toString();
			fallbackMethodName = fallbackMethodName.substring(1, fallbackMethodName.length() - 1);
			if (!getExistingMethods(node).contains(fallbackMethodName)) {
				String message = MessageFormat.format(FALLBACK_ERROR_MESSAGE, fallbackMethodName);
				super.addDiagnostic(message, DIAGNOSTIC_SOURCE, fallbackMethodExpr, FALLBACK_METHOD_DOES_NOT_EXIST,
						DiagnosticSeverity.Error);
			}
		}
	}

	/**
	 * Checks if the given method declaration has an asynchronous annotation, and if
	 * so, provides diagnostics for the method return type
	 *
	 * @param node       The method declaration to validate
	 * @param annotation The @Asynchronous annotation
	 * @throws JavaModelException
	 */
	private void validateAsynchronousAnnotation(MethodDeclaration node, MarkerAnnotation annotation)
			throws JavaModelException {
		Type methodReturnType = node.getReturnType2();
		String methodReturnTypeString;
		try {
			methodReturnTypeString = methodReturnType.resolveBinding().getErasure().getQualifiedName();
		} catch (Exception e) {
			throw e;
		}
		if ((!isAllowedReturnTypeForAsynchronousAnnotation(methodReturnTypeString))) {
			String allowedTypes = allowedReturnTypesForAsynchronousAnnotation.stream()
					.collect(Collectors.joining("', '", "'", "'"));
			String message = MessageFormat.format(ASYNCHRONOUS_ERROR_MESSAGE, node.getName(), allowedTypes);
			super.addDiagnostic(message, DIAGNOSTIC_SOURCE, methodReturnType, FAULT_TOLERANCE_DEFINITION_EXCEPTION,
					DiagnosticSeverity.Error);
		}
	}

	/**
	 * Checks if the given method declaration has a timeout annotation, and if so,
	 * provides diagnostics for the timeout value
	 *
	 * @param node       The method declaration to validate
	 * @param annotation The @Timeout annotation
	 * @throws JavaModelException
	 */
	private void validateTimeoutAnnotation(MethodDeclaration node, Annotation annotation) throws JavaModelException {
		Expression timeoutExpr = null;
		if (annotation instanceof SingleMemberAnnotation) {
			SingleMemberAnnotation singleTimeout = (SingleMemberAnnotation) annotation;
			timeoutExpr = singleTimeout.getValue();
		} else if (annotation instanceof NormalAnnotation) {
			NormalAnnotation normalTimeout = (NormalAnnotation) annotation;
			timeoutExpr = getAnnotationMemberValueExpression(normalTimeout, VALUE_ANNOTATION_MEMBER);
		}
		if (timeoutExpr != null) {
			int timeoutExprVal = Integer.parseInt(timeoutExpr.toString());
			if (timeoutExprVal < 0) {
				String message = MessageFormat.format(TIMEOUT_ERROR_MESSAGE, VALUE_ANNOTATION_MEMBER);
				super.addDiagnostic(message, DIAGNOSTIC_SOURCE, timeoutExpr, FAULT_TOLERANCE_DEFINITION_EXCEPTION,
						DiagnosticSeverity.Error);
			}
		}
	}

	private boolean isAllowedReturnTypeForAsynchronousAnnotation(String returnType) {
		return allowedReturnTypesForAsynchronousAnnotation.contains(returnType);
	}

	private Set<String> getExistingMethods(MethodDeclaration node) {
		TypeDeclaration type = getOwnerType(node);
		if (type == null) {
			return Collections.emptySet();
		}
		return getExistingMethods(type);
	}

	private TypeDeclaration getOwnerType(ASTNode node) {
		while (node != null) {
			if (node instanceof TypeDeclaration) {
				return (TypeDeclaration) node;
			}
			node = node.getParent();
		}
		return null;
	}

	private Set<String> getExistingMethods(TypeDeclaration type) {
		Set<String> methods = methodsCache.get(type);
		if (methods == null) {
			methods = Stream.of(type.getMethods()) //
					.map(m -> {
						return m.getName().getIdentifier();
					}).collect(Collectors.toUnmodifiableSet());
			methodsCache.put(type, methods);
		}
		return methods;
	};

}
