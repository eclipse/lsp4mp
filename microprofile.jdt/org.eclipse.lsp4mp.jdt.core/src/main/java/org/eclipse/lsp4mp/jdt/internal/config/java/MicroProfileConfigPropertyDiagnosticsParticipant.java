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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils;
import org.eclipse.lsp4mp.jdt.internal.metrics.MicroProfileMetricsConstants;

/**
 * Validate "defaultValue" attribute of @ConfigProperty and generate
 * diagnostics if "defaultValue" cannot be represented by the given
 * field type.
 *
 * See https://github.com/eclipse/microprofile-config/blob/master/spec/src/main/asciidoc/converters.asciidoc
 * for more details on default converters.
 */
public class MicroProfileConfigPropertyDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

	@Override
	public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		List<Diagnostic> diagnostics = new ArrayList<>();
		validateClass(diagnostics, context, monitor);
		return diagnostics;
	}

	private void validateClass(List<Diagnostic> diagnostics, JavaDiagnosticsContext context,
			IProgressMonitor monitor) {
		CompilationUnit ast = context.getASTRoot();
		ast.accept(new ConfigPropertiesAnnotationValidator(diagnostics, context));
	}

	public class ConfigPropertiesAnnotationValidator extends ASTVisitor {
		private List<Diagnostic> diagnostics;
		private JavaDiagnosticsContext context;

		public ConfigPropertiesAnnotationValidator(List<Diagnostic> diagnostics, JavaDiagnosticsContext context) {
			this.diagnostics = diagnostics;
			this.context = context;
		}

		@Override
		public boolean visit(NormalAnnotation node) {
			if (AnnotationUtils.isMatchAnnotation(node, MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION)) {
				try {
					Expression defValueExpr = AnnotationUtils.getAnnotationMemberValueExpression(node, MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE);
					ASTNode parent = node.getParent();
					if (defValueExpr != null && parent instanceof FieldDeclaration) {
						String defValue = ((StringLiteral) defValueExpr).getLiteralValue();
						FieldDeclaration field = (FieldDeclaration) parent;
						ITypeBinding fieldBinding = field.getType().resolveBinding();
						if (fieldBinding != null && !isAssignable(fieldBinding, defValue)) {
							Range range = context.getUtils().toRange(context.getTypeRoot(), defValueExpr.getStartPosition(), defValueExpr.getLength());
							diagnostics.add(new Diagnostic(range,
									"'" + defValue + "'" + " does not match the expected type of '" + fieldBinding.getName() + "'.",
									DiagnosticSeverity.Error, MicroProfileMetricsConstants.DIAGNOSTIC_SOURCE));
						}
					}
				} catch (JavaModelException e) {
					return false;
				}
			}
			return false;
		}

		private boolean isAssignable(ITypeBinding fieldBinding, String defValue) {
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
					return  Class.forName(defValue) != null;
				case "java.lang.String":
					return true;
				default:
					return false;
				}
			} catch (NumberFormatException | ClassNotFoundException e) {
				return false;
			}
		}
	}
}
