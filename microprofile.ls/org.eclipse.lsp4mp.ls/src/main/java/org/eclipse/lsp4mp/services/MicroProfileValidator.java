/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.services;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.metadata.ConfigurationMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.Node.NodeType;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.model.PropertyValueExpression;
import org.eclipse.lsp4mp.settings.MicroProfileValidationSettings;
import org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils;
import org.eclipse.lsp4mp.utils.PositionUtils;
import org.eclipse.lsp4mp.utils.StringUtils;

/**
 * MicroProfile validator to validate properties declared in
 * application.properties.
 *
 * @author Angelo ZERR
 *
 */
class MicroProfileValidator {

	private static final String MICROPROFILE_DIAGNOSTIC_SOURCE = "microprofile";

	private final MicroProfileProjectInfo projectInfo;

	private final List<Diagnostic> diagnostics;

	private final MicroProfileValidationSettings validationSettings;
	private final Map<String, List<Property>> existingProperties;
	private Set<String> allProperties;

	public MicroProfileValidator(MicroProfileProjectInfo projectInfo, List<Diagnostic> diagnostics,
			MicroProfileValidationSettings validationSettings) {
		this.projectInfo = projectInfo;
		this.diagnostics = diagnostics;
		this.validationSettings = validationSettings;
		this.existingProperties = new HashMap<String, List<Property>>();
		this.allProperties = null; // to be lazily init
	}

	public void validate(PropertiesModel document, CancelChecker cancelChecker) {
		List<Node> nodes = document.getChildren();

		for (Node node : nodes) {
			if (cancelChecker != null) {
				cancelChecker.checkCanceled();
			}
			if (node.getNodeType() == NodeType.PROPERTY) {
				validateProperty((Property) node);
			}
		}

		addDiagnosticsForDuplicates();
		addDiagnosticsForMissingRequired(document);
	}

	private void validateProperty(Property property) {
		String propertyNameWithProfile = property.getPropertyNameWithProfile();
		if (!StringUtils.isEmpty(propertyNameWithProfile)) {
			// Validate Syntax property
			validateSyntaxProperty(propertyNameWithProfile, property);
			// Validate Duplicate property
			validateDuplicateProperty(propertyNameWithProfile, property);
		}

		String propertyName = property.getPropertyName();
		if (!StringUtils.isEmpty(propertyName)) {
			ItemMetadata metadata = MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo);
			if (metadata == null) {
				// Validate Unknown property
				validateUnknownProperty(propertyName, property);
			} else {
				// Validate property Value
				validatePropertyValue(propertyNameWithProfile, metadata, property);
			}
			validatePropertyValueExpressions(property);
		}
	}

	private void validateSyntaxProperty(String propertyName, Property property) {
		DiagnosticSeverity severity = validationSettings.getSyntax().getDiagnosticSeverity(propertyName);
		if (severity == null) {
			// The syntax validation must be ignored for this property name
			return;
		}
		if (property.getDelimiterAssign() == null) {
			addDiagnostic("Missing equals sign after '" + propertyName + "'", property.getKey(), severity,
					ValidationType.syntax.name());
		}
	}

	private void validateDuplicateProperty(String propertyName, Property property) {
		DiagnosticSeverity severity = validationSettings.getDuplicate().getDiagnosticSeverity(propertyName);
		if (severity == null) {
			// The duplicate validation must be ignored for this property name
			return;
		}

		if (!existingProperties.containsKey(propertyName)) {
			existingProperties.put(propertyName, new ArrayList<Property>());
		}

		existingProperties.get(propertyName).add(property);
	}

	private void validateUnknownProperty(String propertyName, Property property) {
		DiagnosticSeverity severity = validationSettings.getUnknown().getDiagnosticSeverity(propertyName);
		if (severity == null) {
			// The unknown validation must be ignored for this property name
			return;
		}
		addDiagnostic("Unknown property '" + propertyName + "'", property.getKey(), severity,
				ValidationType.unknown.name());
	}

	private void validatePropertyValue(String propertyName, ItemMetadata metadata, Property property) {

		if (property.getValue() == null) {
			return;
		}

		DiagnosticSeverity severity = validationSettings.getValue().getDiagnosticSeverity(propertyName);
		if (severity == null) {
			// The value validation must be ignored for this property name
			return;
		}

		String value = property.getPropertyValue();
		if (StringUtils.isEmpty(value)) {
			return;
		}

		String errorMessage = getErrorIfInvalidEnum(metadata, projectInfo, property.getOwnerModel(), value);
		if (errorMessage == null) {
			errorMessage = getErrorIfValueTypeMismatch(metadata, value);
		}

		if (errorMessage != null) {
			addDiagnostic(errorMessage, property.getValue(), severity, ValidationType.value.name());
		}
	}

	/**
	 * Validates the property value expressions (${other.property}) of the given
	 * property.
	 *
	 * Checks if the property expression is closed, and if the referenced property
	 * exists.
	 *
	 * @param property      The property to validate
	 * @param allProperties A list of all the properties defined in the property
	 *                      file and in the project info
	 */
	private void validatePropertyValueExpressions(Property property) {
		if (property.getValue() == null) {
			return;
		}
		DiagnosticSeverity expressionSeverity = validationSettings.getExpression()
				.getDiagnosticSeverity(property.getPropertyName());
		DiagnosticSeverity syntaxSeverity = validationSettings.getSyntax()
				.getDiagnosticSeverity(property.getPropertyName());
		if (expressionSeverity == null || syntaxSeverity == null) {
			return;
		}
		for (Node child : property.getValue().getChildren()) {
			if (child != null && child.getNodeType() == NodeType.PROPERTY_VALUE_EXPRESSION) {
				PropertyValueExpression propValExpr = (PropertyValueExpression) child;
				if (expressionSeverity != null) {
					if (allProperties == null) {
						// Collect names of all properties defined in the configuration file and the
						// project information
						allProperties = new HashSet<>();
						allProperties.addAll(projectInfo.getProperties().stream().map((ItemMetadata info) -> {
							return info.getName();
						}).collect(Collectors.toList()));
						allProperties.addAll(property.getOwnerModel().getChildren().stream().filter(n -> {
							return n.getNodeType() == NodeType.PROPERTY;
						}).map(prop -> {
							return ((Property) prop).getPropertyNameWithProfile();
						}).collect(Collectors.toList()));
					}
					String refdProp = propValExpr.getReferencedPropertyName();
					if (!allProperties.contains(refdProp)) {
						Range range = PositionUtils.createAdjustedRange(propValExpr, 2,
								propValExpr.isClosed() ? -1 : 0);
						if (range != null) {
							addDiagnostic("Unknown referenced property '" + refdProp + "'", range, expressionSeverity,
									ValidationType.expression.name());
						}
					}
				}
				if (syntaxSeverity != null && !propValExpr.isClosed()) {
					addDiagnostic("Missing '}'", propValExpr, syntaxSeverity, ValidationType.syntax.name());
				}
			}
		}
	}

	/**
	 * Returns an error message only if <code>value</code> is an invalid enum for
	 * the property defined by <code>metadata</code>
	 *
	 * @param metadata metadata defining a property
	 * @param value    value to check
	 * @return error message only if <code>value</code> is an invalid enum for the
	 *         property defined by <code>metadata</code>
	 */
	private String getErrorIfInvalidEnum(ItemMetadata metadata, ConfigurationMetadata configuration,
			PropertiesModel model, String value) {
		if (!MicroProfilePropertiesUtils.isValidEnum(metadata, configuration, value)) {
			return "Invalid enum value: '" + value + "' is invalid for type " + metadata.getType();
		}
		return null;
	}

	/**
	 * Returns an error message only if <code>value</code> is an invalid value type
	 * for the property defined by <code>metadata</code>
	 *
	 * @param metadata metadata defining a property
	 * @param value    value to check
	 * @return error message only if <code>value</code> is an invalid value type for
	 *         the property defined by <code>metadata</code>
	 */
	private static String getErrorIfValueTypeMismatch(ItemMetadata metadata, String value) {

		if (isBuildtimePlaceholder(value)) {
			return null;
		}

		if (metadata.isRegexType()) {
			try {
				Pattern.compile(value);
				return null;
			} catch (PatternSyntaxException e) {
				return e.getMessage() + System.lineSeparator();
			}
		}

		if (metadata.isBooleanType() && !isBooleanString(value)) {
			return "Type mismatch: " + metadata.getType()
					+ " expected. By default, this value will be interpreted as 'false'";
		}

		if ((metadata.isIntegerType() && !isIntegerString(value) || (metadata.isFloatType() && !isFloatString(value))
				|| (metadata.isDoubleType() && !isDoubleString(value))
				|| (metadata.isLongType() && !isLongString(value)) || (metadata.isShortType() && !isShortString(value))
				|| (metadata.isBigDecimalType() && !isBigDecimalString(value))
				|| (metadata.isBigIntegerType() && !isBigIntegerString(value)))) {
			return "Type mismatch: " + metadata.getType() + " expected";
		}
		return null;
	}

	private static boolean isBooleanString(String str) {
		if (str == null) {
			return false;
		}
		String strUpper = str.toUpperCase();
		return "TRUE".equals(strUpper) || "FALSE".equals(strUpper) || "Y".equals(strUpper) || "YES".equals(strUpper)
				|| "1".equals(strUpper) || "ON".equals(strUpper);
	}

	private static boolean isIntegerString(String str) {
		if (!StringUtils.hasText(str)) {
			return false;
		}
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isFloatString(String str) {
		if (!StringUtils.hasText(str)) {
			return false;
		}
		try {
			Float.parseFloat(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isLongString(String str) {
		if (!StringUtils.hasText(str)) {
			return false;
		}
		try {
			Long.parseLong(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isDoubleString(String str) {
		if (!StringUtils.hasText(str)) {
			return false;
		}
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isShortString(String str) {
		if (!StringUtils.hasText(str)) {
			return false;
		}
		try {
			Short.parseShort(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isBigDecimalString(String str) {
		if (!StringUtils.hasText(str)) {
			return false;
		}
		try {
			new BigDecimal(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isBigIntegerString(String str) {
		if (!StringUtils.hasText(str)) {
			return false;
		}
		try {
			new BigInteger(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isBuildtimePlaceholder(String str) {
		return str.startsWith("${") && str.endsWith("}");
	}

	private void addDiagnosticsForDuplicates() {
		existingProperties.forEach((propertyName, propertyList) -> {
			if (propertyList.size() <= 1) {
				return;
			}

			DiagnosticSeverity severity = validationSettings.getDuplicate().getDiagnosticSeverity(propertyName);

			for (Property property : propertyList) {
				addDiagnostic("Duplicate property '" + propertyName + "'", property.getKey(), severity,
						ValidationType.duplicate.name());
			}
		});
	}

	private void addDiagnosticsForMissingRequired(PropertiesModel document) {
		for (ItemMetadata property : projectInfo.getProperties()) {

			String propertyName = property.getName();

			DiagnosticSeverity severity = validationSettings.getRequired().getDiagnosticSeverity(propertyName);

			if (severity != null && property.isRequired()) {
				if (!existingProperties.containsKey(propertyName)) {
					addDiagnostic("Missing required property '" + propertyName + "'", document, severity,
							ValidationType.required.name());
				} else {
					addDiagnosticsForRequiredIfNoValue(propertyName, severity);
				}
			}
		}
	}

	private void addDiagnosticsForRequiredIfNoValue(String propertyName, DiagnosticSeverity severity) {
		List<Property> propertyList = existingProperties.get(propertyName);

		for (Property property : propertyList) {
			if (property.getValue() != null && !property.getValue().getValue().isEmpty()) {
				return;
			}
		}

		for (Property property : propertyList) {
			addDiagnostic("Missing required property value for '" + propertyName + "'", property, severity,
					ValidationType.requiredValue.name());
		}
	}

	private void addDiagnostic(String message, Node node, DiagnosticSeverity severity, String code) {
		Range range = PositionUtils.createRange(node);
		addDiagnostic(message, range, severity, code);
	}

	private void addDiagnostic(String message, Range range, DiagnosticSeverity severity, String code) {
		diagnostics.add(new Diagnostic(range, message, severity, MICROPROFILE_DIAGNOSTIC_SOURCE, code));
	}

	public MicroProfileValidationSettings getValidationSettings() {
		return validationSettings;
	}

}
