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
package org.eclipse.lsp4mp.jdt.internal.config.java;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_NAME;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants.MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValueExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.commons.utils.AntPathMatcher;
import org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProject;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProjectManager;
import org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Collects diagnostics related to the <code>@ConfigProperty</code> annotation
 * in a Java file.
 *
 * Produces diagnostics when:
 * <ul>
 * <li>The <code>defaultValue</code> attribute value cannot be represented by
 * the type of the field being annotated</li>
 * <li>The config property defined by the annotation doesn't have a default
 * value and doesn't have a value assigned to it in any properties file</li>
 * </ul>
 *
 */
public class MicroProfileConfigDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileConfigDiagnosticsParticipant.class.getName());

	private static final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	public boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, CONFIG_PROPERTY_ANNOTATION) != null;
	}

	@Override
	public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		List<Diagnostic> diagnostics = new ArrayList<>();
		collectDiagnosticsInFile(context, diagnostics, monitor);
		return diagnostics;
	}

	private static List<String> getPatternsFromContext(JavaDiagnosticsContext context) {
		return context.getSettings().getPatterns();
	}

	private static void collectDiagnosticsInFile(JavaDiagnosticsContext context, List<Diagnostic> diagnostics,
			IProgressMonitor monitor) throws JavaModelException {
		CompilationUnit ast = context.getASTRoot();
		ast.accept(new AnnotationDiagnosticCollector(context, diagnostics));
	}

	private static class AnnotationDiagnosticCollector extends ASTVisitor {

		private final List<Diagnostic> diagnostics;
		private JavaDiagnosticsContext context;
		private List<String> patterns;

		public AnnotationDiagnosticCollector(JavaDiagnosticsContext context, List<Diagnostic> diagnostics) {
			super();
			this.diagnostics = diagnostics;
			this.context = context;
			this.patterns = getPatternsFromContext(context);
		}

		@Override
		public boolean visit(NormalAnnotation annotation) {
			if (AnnotationUtils.isMatchAnnotation(annotation, MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION)
					&& annotation.getParent() instanceof FieldDeclaration) {
				try {

					Expression defaultValueExpr = AnnotationUtils.getAnnotationMemberValueExpression(annotation,
							CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE);
					validatePropertyDefaultValue(annotation, defaultValueExpr);
					validatePropertyHasValue(annotation, defaultValueExpr);
				} catch (JavaModelException e) {
					LOGGER.log(Level.WARNING,
							"Exception when trying to get defaultValue of a @ConfigProperty annotation while calculating diagnostics for it",
							e);
				}
			}
			return false;
		}

		/**
		 * Validate "defaultValue" attribute of <code>@ConfigProperty</code> and
		 * generate diagnostics if "defaultValue" cannot be represented by the given
		 * field type.
		 *
		 * See
		 * https://github.com/eclipse/microprofile-config/blob/master/spec/src/main/asciidoc/converters.asciidoc
		 * for more details on default converters.
		 *
		 * @param annotation       the annotation to validate the defaultValue of
		 * @param defaultValueExpr the default value expression, or null if no default
		 *                         value is defined
		 */
		private void validatePropertyDefaultValue(Annotation annotation, Expression defaultValueExpr) {
			try {
				FieldDeclaration fieldDeclaration = (FieldDeclaration) annotation.getParent();
				if (defaultValueExpr != null) {
					String defValue = ((StringLiteral) defaultValueExpr).getLiteralValue();
					ITypeBinding fieldBinding = fieldDeclaration.getType().resolveBinding();
					if (fieldBinding != null && !isAssignable(fieldBinding, defValue)) {
						Range range = context.getUtils().toRange(context.getTypeRoot(),
								defaultValueExpr.getStartPosition(), defaultValueExpr.getLength());
						Diagnostic d = new Diagnostic(
								range, "'" + defValue + "'" + " does not match the expected type of '"
										+ fieldBinding.getName() + "'.",
								DiagnosticSeverity.Error, MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE);
						d.setCode(MicroProfileConfigErrorCode.DEFAULT_VALUE_IS_WRONG_TYPE.getCode());
						diagnostics.add(d);
					}
				}
			} catch (JavaModelException e) {
				LOGGER.log(Level.WARNING,
						"Exception while calculating diagnostics for @ConfigProperty (default value has correct type)",
						e);
			}
		}

		/**
		 * Generates diagnostics if the property that this <code>@ConfigProperty</code>
		 * defines does not have a value assigned to it.
		 *
		 * @param annotation       the ConfigProperty annotation
		 * @param defaultValueExpr the default value expression, or null if no default
		 *                         value is defined
		 */
		private void validatePropertyHasValue(Annotation annotation, Expression defaultValueExpr) {
			try {
				String name = null;
				Expression nameExpression = getAnnotationMemberValueExpression(annotation,
						CONFIG_PROPERTY_ANNOTATION_NAME);
				boolean hasDefaultValue = defaultValueExpr != null;

				if (nameExpression != null) {
					name = ((StringLiteral) nameExpression).getLiteralValue();
				}

				if (name != null && !hasDefaultValue && !doesPropertyHaveValue(name, context)
						&& !isPropertyIgnored(name)) {
					String fileUri = context.getUri();
					IOpenable openable = context.getUtils().resolveCompilationUnit(fileUri);
					Range range = context.getUtils().toRange(openable, nameExpression.getStartPosition(),
							nameExpression.getLength());
					Diagnostic d = context.createDiagnostic(fileUri,
							"The property " + name
									+ " is not assigned a value in any config file, and must be assigned at runtime",
							range, MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE,
							MicroProfileConfigErrorCode.NO_VALUE_ASSIGNED_TO_PROPERTY);
					diagnostics.add(d);
				}
			} catch (JavaModelException e) {
				LOGGER.log(Level.WARNING,
						"Exception while calculating diagnostics for @ConfigProperty (property has value)", e);
			}
		}

		private boolean isPropertyIgnored(String propertyName) {
			for (String pattern : patterns) {
				if (pathMatcher.match(pattern, propertyName)) {
					return true;
				}
			}
			return false;
		}

	}

	private static boolean isAssignable(ITypeBinding fieldBinding, String defValue) {
		String fqn = Signature.getTypeErasure(fieldBinding.getQualifiedName());
		try {
			switch (fqn) {
			case "boolean":
			case "java.lang.Boolean":
				return Boolean.valueOf(defValue) != null;
			case "byte":
			case "java.lang.Byte":
				return Byte.valueOf(defValue) != null;
			case "short":
			case "java.lang.Short":
				return Short.valueOf(defValue) != null;
			case "int":
			case "java.lang.Integer":
				return Integer.valueOf(defValue) != null;
			case "long":
			case "java.lang.Long":
				return Long.valueOf(defValue) != null;
			case "float":
			case "java.lang.Float":
				return Float.valueOf(defValue) != null;
			case "double":
			case "java.lang.Double":
				return Double.valueOf(defValue) != null;
			case "char":
			case "java.lang.Character":
				return Character.valueOf(defValue.charAt(0)) != null;
			case "java.lang.Class":
				return Class.forName(defValue) != null;
			case "java.lang.String":
				return true;
			default:
				return false;
			}
		} catch (NumberFormatException | ClassNotFoundException e) {
			return false;
		}
	}

	private static boolean doesPropertyHaveValue(String property, JavaDiagnosticsContext context) {
		IJavaProject javaProject = context.getJavaProject();
		try {
			JDTMicroProfileProject mpProject = JDTMicroProfileProjectManager.getInstance()
					.getJDTMicroProfileProject(javaProject);
			return mpProject.hasProperty(property);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "@ConfigProperty validation attempted in a non MicroProfile project", e);
			return false;
		}
	}

}
