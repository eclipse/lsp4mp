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
package org.eclipse.lsp4mp.jdt.internal.core.java.validators.annotations;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.jdt.core.java.validators.JavaASTValidator;
import org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils;
import org.eclipse.lsp4mp.jdt.internal.core.java.validators.JavaASTValidatorRegistry;

/**
 * JDT Java AST visitor which validate annotation attributes by using annotation
 * rules registered.
 * 
 * @author Angelo ZERR
 *
 */
public class AnnotationRulesJavaASTValidator extends JavaASTValidator {

	private static final Logger LOGGER = Logger.getLogger(AnnotationRulesJavaASTValidator.class.getName());

	private final Collection<AnnotationRule> rules;

	public AnnotationRulesJavaASTValidator(Collection<AnnotationRule> rules) {
		this.rules = rules;
	}

	@Override
	public boolean visit(SingleMemberAnnotation annotation) {
		validateAnnotation(annotation);
		return false;
	}

	@Override
	public boolean visit(NormalAnnotation annotation) {
		validateAnnotation(annotation);
		return false;
	}

	private void validateAnnotation(Annotation annotation) {
		// Loop for rules
		for (AnnotationRule annotationRule : rules) {
			if (AnnotationUtils.isMatchAnnotation(annotation, annotationRule.getAnnotation())) {
				// The AST annotation matches a rule
				List<AnnotationAttributeRule> attributeRules = annotationRule.getRules();
				// Validate attributes of the AST annotation
				for (AnnotationAttributeRule attributeRule : attributeRules) {

					try {
						Expression attributeValueExpr = AnnotationUtils.getAnnotationMemberValueExpression(annotation,
								attributeRule.getAttribute());
						if (attributeValueExpr != null) {
							validateAnnotationAttributeValue(attributeValueExpr, attributeRule);
						}
					} catch (JavaModelException e) {
						LOGGER.log(Level.WARNING, "Exception when trying to validate annotation attribute value", e);
					}
				}

			}
		}
	}

	/**
	 * Validate the given AST attribute value expression
	 * <code>attributeValueExpr</code> by using the given rule
	 * <code>attributeValue</code> and create a diagnostic if there is an error.
	 * 
	 * @param attributeValueExpr
	 * @param attributeRule
	 * @throws JavaModelException
	 */
	private void validateAnnotationAttributeValue(Expression attributeValueExpr, AnnotationAttributeRule attributeRule)
			throws JavaModelException {
		if (attributeValueExpr == null) {
			return;
		}
		// Ensure value of AST attribute is a valid integer or an expression that can be evaluated
		if (!isInteger(attributeValueExpr) && !isInfixIntegerExpression(attributeValueExpr)) {
			return;
		}

		// Resolve the value of the AST attribute
		String valueAsString = attributeValueExpr.resolveConstantExpressionValue().toString();
		if (StringUtils.isEmpty(valueAsString)) {
			return;
		}

		// Validate the value with the rule
		String validationResult = JavaASTValidatorRegistry.getInstance().validate(valueAsString, attributeRule);
		if (validationResult != null) {
			// There is an error, report a diagnostic
			super.addDiagnostic(validationResult, attributeRule.getSource(), attributeValueExpr, null,
					DiagnosticSeverity.Error);
		}
	}

	private static boolean isInteger(Expression attributeValueExpr) {
		if (attributeValueExpr instanceof NumberLiteral || (attributeValueExpr instanceof PrefixExpression
				&& (((PrefixExpression) attributeValueExpr).getOperator() == PrefixExpression.Operator.MINUS
				|| ((PrefixExpression) attributeValueExpr).getOperator() == PrefixExpression.Operator.PLUS))) {
			return true;
		}
		return false;
	}

	private static boolean isInfixIntegerExpression(Expression attributeValueExpr) {
		if (attributeValueExpr instanceof InfixExpression
				&& (((InfixExpression) attributeValueExpr).getOperator() == InfixExpression.Operator.TIMES
				|| ((InfixExpression) attributeValueExpr).getOperator() == InfixExpression.Operator.DIVIDE
				|| ((InfixExpression) attributeValueExpr).getOperator() == InfixExpression.Operator.REMAINDER
				|| ((InfixExpression) attributeValueExpr).getOperator() == InfixExpression.Operator.PLUS
				|| ((InfixExpression) attributeValueExpr).getOperator() == InfixExpression.Operator.MINUS)) {
			return true;
		}
		return false;
	}
}
