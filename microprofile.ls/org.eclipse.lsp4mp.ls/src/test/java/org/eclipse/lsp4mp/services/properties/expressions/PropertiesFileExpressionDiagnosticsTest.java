/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.services.properties.expressions;

import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.d;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.getDefaultMicroProfileProjectInfo;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.services.properties.ValidationType;
import org.eclipse.lsp4mp.settings.MicroProfileValidationSettings;
import org.junit.Test;

/**
 * Test with property expression diagnostics in 'microprofile-config.properties'
 * file.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesFileExpressionDiagnosticsTest {

	@Test
	public void validateBuildTimeInjectValues() throws BadLocationException {
		String value = "quarkus.http.cors = ${value.one}\n" + //
				"quarkus.http.port=${value_two}\n" + //
				"quarkus.ssl.native=    ${value-three}";
		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings, //
				d(0, 22, 31, "Unknown referenced property 'value.one'", DiagnosticSeverity.Error,
						ValidationType.expression), //
				d(1, 20, 29, "Unknown referenced property 'value_two'", DiagnosticSeverity.Error,
						ValidationType.expression), //
				d(2, 25, 36, "Unknown referenced property 'value-three'", DiagnosticSeverity.Error,
						ValidationType.expression));
	}

	@Test
	public void undefinedPropertyInPropertyExpression() {
		String value = "test.property = ${doesnt.exist.property}";
		testDiagnosticsFor(value, //
				d(0, 0, 13, "Unknown property 'test.property'", DiagnosticSeverity.Warning, ValidationType.unknown), //
				d(0, 18, 39, "Unknown referenced property 'doesnt.exist.property'", DiagnosticSeverity.Error,
						ValidationType.expression));
	}

	@Test
	public void validateMicroProfilePropertyInPropertyExpression() {
		String value = "test.property = ${mp.opentracing.server.skip-pattern}";
		testDiagnosticsFor(value, //
				d(0, 0, 13, "Unknown property 'test.property'", DiagnosticSeverity.Warning, ValidationType.unknown), //
				d(0, 18, 52, "The referenced property 'mp.opentracing.server.skip-pattern' has no default value.",
						DiagnosticSeverity.Error, ValidationType.expression));
	}

	@Test
	public void validateUnclosedPropertyExpression() {
		String value = "test.property = hello\n" + //
				"other.property = ${test.property";
		testDiagnosticsFor(value, //
				d(0, 0, 13, "Unknown property 'test.property'", DiagnosticSeverity.Warning, ValidationType.unknown), //
				d(1, 0, 14, "Unknown property 'other.property'", DiagnosticSeverity.Warning, ValidationType.unknown), //
				d(1, 17, 32, "Missing '}'", DiagnosticSeverity.Error, ValidationType.syntax));
	}

	@Test
	public void validateUnknownPropertyInPropertyExpressionAndMissingBrace() {
		String value = "test.property = ${other.property";
		testDiagnosticsFor(value, //
				d(0, 0, 13, "Unknown property 'test.property'", DiagnosticSeverity.Warning, ValidationType.unknown), //
				d(0, 18, 32, "Unknown referenced property 'other.property'", DiagnosticSeverity.Error,
						ValidationType.expression), //
				d(0, 16, 32, "Missing '}'", DiagnosticSeverity.Error, ValidationType.syntax));
	}

	@Test
	public void validateMultipleInvalidPropertyExpressions() {
		String value = "property.one = hello\n" + //
				"property.two = ${property.on}\n" + //
				"property.three = ${property.tw}\n" + //
				"property.four = ${property.th}\n";
		testDiagnosticsFor(value, //
				d(0, 0, 12, "Unknown property 'property.one'", DiagnosticSeverity.Warning, ValidationType.unknown), //
				d(1, 0, 12, "Unknown property 'property.two'", DiagnosticSeverity.Warning, ValidationType.unknown), //
				d(1, 17, 28, "Unknown referenced property 'property.on'", DiagnosticSeverity.Error,
						ValidationType.expression), //
				d(2, 0, 14, "Unknown property 'property.three'", DiagnosticSeverity.Warning, ValidationType.unknown), //
				d(2, 19, 30, "Unknown referenced property 'property.tw'", DiagnosticSeverity.Error,
						ValidationType.expression), //
				d(3, 0, 13, "Unknown property 'property.four'", DiagnosticSeverity.Warning, ValidationType.unknown), //
				d(3, 18, 29, "Unknown referenced property 'property.th'", DiagnosticSeverity.Error,
						ValidationType.expression));
	}

	@Test
	public void ignoreErrorForPropertyExpressionWithDefaultValue() {
		String value = "quarkus.datasource.username = ${DBUSER:sa}";
		testDiagnosticsFor(value);

		value = "quarkus.datasource.username = ${DBUSER:}";
		testDiagnosticsFor(value, //
				d(0, 32, 38, "Unknown referenced property 'DBUSER'", DiagnosticSeverity.Error,
						ValidationType.expression));
	}

	@Test
	public void referencePropertyFromJavaWithoutDefaultValue() {

		// "name": "quarkus.application.name",
		String value = "quarkus.datasource.username = ${quarkus.application.name}";
		testDiagnosticsFor(value, //
				d(0, 32, 56, "The referenced property 'quarkus.application.name' has no default value.",
						DiagnosticSeverity.Error, ValidationType.expression));

		value = "quarkus.datasource.username = ${quarkus.application.name:sa}";
		testDiagnosticsFor(value);
	}

	@Test
	public void referencePropertyFromJavaWithDefaultValue() {
		// "name": "quarkus.platform.group-id",
		// "defaultValue": "io.quarkus",
		String value = "quarkus.datasource.username = ${quarkus.platform.group-id}";
		testDiagnosticsFor(value, //
				d(0, 32, 57,
						"Cannot reference the property 'quarkus.platform.group-id'. A default value defined via annotation like ConfigProperty is not eligible to be expanded since multiple candidates may be available.",
						DiagnosticSeverity.Error, ValidationType.expression));

		value = "quarkus.datasource.username = ${quarkus.platform.group-id:sa}";
		testDiagnosticsFor(value, //
				d(0, 32, 57,
						"Cannot reference the property 'quarkus.platform.group-id'. A default value defined via annotation like ConfigProperty is not eligible to be expanded since multiple candidates may be available.",
						DiagnosticSeverity.Error, ValidationType.expression));
	}

	@Test
	public void validatePropertyFilePropertyInPropertyExpression() {
		String value = "test.property = hello\n" + //
				"other.property = ${test.property}";
		testDiagnosticsFor(value, //
				d(0, 0, 13, "Unknown property 'test.property'", DiagnosticSeverity.Warning, ValidationType.unknown), //
				d(1, 0, 14, "Unknown property 'other.property'", DiagnosticSeverity.Warning, ValidationType.unknown));
	}

	@Test
	public void validateValueForLevelBasedOnRuleWithExpression() throws BadLocationException {
		// quarkus.log.file.level has 'java.util.logging.Level' which has no
		// enumeration
		// to fix it, quarkus-values-rules.json defines the Level enumerations
		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		
		// Valid level
		String value = "quarkus.log.file.level=${ENV_LEVEL:SEVERE} ";
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);
		
		// Invalid level
		value = "quarkus.log.file.level=${ENV_LEVEL:XXX} ";
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
				d(0, 35, 38, "Invalid enum value: 'XXX' is invalid for type java.util.logging.Level",
						DiagnosticSeverity.Error, ValidationType.value));

		
	}
}
