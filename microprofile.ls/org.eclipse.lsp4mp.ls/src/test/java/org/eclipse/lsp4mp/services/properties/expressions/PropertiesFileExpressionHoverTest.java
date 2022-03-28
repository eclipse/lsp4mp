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

import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.assertHoverMarkdown;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.assertNoHover;

import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.junit.Test;

/**
 * Test with property expression hover in 'microprofile-config.properties' file.
 *
 */
public class PropertiesFileExpressionHoverTest {

	@Test
	public void hoverWithEnums() throws BadLocationException {
		String value = "quarkus.log.console.async.overflow=${ENV:BLO|CK}";
		// OverflowAction enum type
		String hoverLabel = "**BLOCK**" + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabel, 41);
	}

	@Test
	public void hoverOnValueForLevelBasedOnRule() throws BadLocationException {
		// quarkus.log.file.level has 'java.util.logging.Level'
		String value = "quarkus.log.file.level=${ENV:OF|F} ";
		String hoverLabel = "**OFF**" + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`OFF` is a special level that can be used to turn off logging.\nThis level is initialized to `Integer.MAX_VALUE`."
				+ //
				System.lineSeparator();
		assertHoverMarkdown(value, hoverLabel, 29);
	}

	@Test
	public void hoverWithEnumsKebabCase() throws BadLocationException {
		String value = "quarkus.datasource.transaction-isolation-level = ${ENV:read-unc|ommitted}";
		// io.agroal.api.configuration.AgroalConnectionFactoryConfiguration.TransactionIsolation
		// enum type
		String hoverLabel = "**READ_UNCOMMITTED**" + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabel, 55);
	}

	@Test
	public void hoverInvalidProperty() throws BadLocationException {
		String value = "aaa=${b|bb}";
		assertNoHover(value);
	}

	@Test
	public void hoverPropertiesFileReference() throws BadLocationException {
		String value = "property.one = hello\n" + //
				"property.two = ${prope|rty.one}";
		String hoverLabel = "hello";
		assertHoverMarkdown(value, hoverLabel, 15);
	}

	@Test
	public void hoverDefaultPropertyValue() throws BadLocationException {
		String value = "property.one = ${mp.openapi.s|can.disable}";
		String hoverLabel = "false";
		assertHoverMarkdown(value, hoverLabel, 15);
	}

	@Test
	public void hoverResolveTwoSteps() throws BadLocationException {
		String value = "property.one = hello\n" + //
				"property.two = ${property.one}\n" + //
				"property.three = ${property.two}\n" + //
				"property.four = ${property.three}\n" + //
				"property.five = ${property|.four}";
		String hoverLabel = "hello";
		assertHoverMarkdown(value, hoverLabel, 16);
	}

	@Test
	public void hoverMixtureOfReferencesAndRawText() throws BadLocationException {
		String value = "property.one = one\n" + //
				"property.two = ${property.one} two\n" + //
				"property.four = ${proper|ty.three} five\n" + //
				"property.three = ${property.one} ${property.two} three";
		String hoverLabel = "one one two three";
		assertHoverMarkdown(value, hoverLabel, 16);
	}

	@Test
	public void hoverSelfReference() throws BadLocationException {
		String value = "property.one = ${prop|erty.one}";
		assertHoverMarkdown(value, "${property.one}", 15);
	}

	@Test
	public void hoverCircularReference() throws BadLocationException {
		String value = "property.one = ${property.two}\n" + //
				"property.two = ${p|roperty.one}";
		assertHoverMarkdown(value, "${property.two}", 15);
	}

	@Test
	public void hoverDependencyTreeAndDefaultValue() throws BadLocationException {
		String value = "property.one = $|{property.two}\n" + //
				"property.two=${property.three} ${mp.openapi.scan.disable}\n" + //
				"property.three = hello";
		assertHoverMarkdown(value, "hello false", 15);
	}

	@Test
	public void hoverAnyCyclePreventsRecursiveResolution() throws BadLocationException {
		String value = "property.one = ${p|roperty.two}\n" + //
				"property.two = ${property.three}\n" + //
				"property.three = hi\n" + //
				"property.four = ${property.five}\n" + //
				"property.five = ${property.four}\n";
		assertHoverMarkdown(value, "${property.three}", 15);
	}

	@Test
	public void hoverEmptyPropertyName() throws BadLocationException {
		String value = "property.one = ${|}";
		assertNoHover(value);
	}

	@Test
	public void hoverBoundsOnPropertyExpression() throws BadLocationException {
		String value = "property.one = |${property.two}\n" + //
				"property.two = hello";
		assertHoverMarkdown(value, "hello", 15);
		value = "property.one = ${property.two}|\n" + //
				"property.two = hello";
		assertHoverMarkdown(value, "hello", 15);
	}

	@Test
	public void hoverMultilineReferencePropertyExpression() throws BadLocationException {
		String value = "property.one = ${prope|rty.two}\n" + //
				"property.two = hello \\\n" + //
				"  there";
		assertHoverMarkdown(value, "hello there", 15);
	}

	@Test
	public void hoverKeyWithReference() throws BadLocationException {
		String value = "value = value\n" + //
				"mp.metri|cs.appName=${value}";
		assertHoverMarkdown(value, //
				"**mp.metrics.appName**" + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						"The app name." + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						" * Type: `java.lang.String`" + //
						System.lineSeparator() + //
						" * Value: `value`" + //
						System.lineSeparator() + //
						" * Extension: `microprofile-metrics-api`",
				0);
	}

	@Test
	public void hoverKeyWithNoReferenceButWithDefaultValue() throws BadLocationException {
		String value = "mp.metri|cs.appName=${value:sa}";
		assertHoverMarkdown(value, //
				"**mp.metrics.appName**" + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						"The app name." + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						" * Type: `java.lang.String`" + //
						System.lineSeparator() + //
						" * Value: `sa`" + //
						System.lineSeparator() + //
						" * Extension: `microprofile-metrics-api`",
				0);
	}

	@Test
	public void hoverKeyUndefinedProperty() throws BadLocationException {
		String value = "pr|operty = value";
		assertNoHover(value);
	}

	@Test
	public void hoverKeyUndefinedPropertyWithReference() throws BadLocationException {
		String value = "value = amount\n" + //
				"pr|operty = ${value}";
		assertNoHover(value);
	}

	@Test
	public void hoverKeyWithReferenceAndSelfLoop() throws BadLocationException {
		String value = "value = ${value}\n" + //
				"mp.metri|cs.appName=${value}";
		assertHoverMarkdown(value, //
				"**mp.metrics.appName**" + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						"The app name." + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						" * Type: `java.lang.String`" + //
						System.lineSeparator() + //
						" * Value: `${value}`" + //
						System.lineSeparator() + //
						" * Extension: `microprofile-metrics-api`",
				0);
	}

	@Test
	public void hoverKeyWithReferenceToEmptyProperty() throws BadLocationException {
		String value = "value =\n" + //
				"mp.metri|cs.appName = ${value}";
		assertHoverMarkdown(value, //
				"**mp.metrics.appName**" + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						"The app name." + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						" * Type: `java.lang.String`" + //
						System.lineSeparator() + //
						" * Extension: `microprofile-metrics-api`",
				0);
	}

	@Test
	public void hoverKeyWithReferenceToBlankProperty() throws BadLocationException {
		String value = "value =      \n" + //
				"mp.metri|cs.appName = ${value}";
		assertHoverMarkdown(value, //
				"**mp.metrics.appName**" + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						"The app name." + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						" * Type: `java.lang.String`" + //
						System.lineSeparator() + //
						" * Extension: `microprofile-metrics-api`",
				0);
	}

	@Test
	public void hoverExistingPropertyReferenceWithoutADefaultValue() throws BadLocationException {
		String value = "my.property = ${quarkus.app|lication.name}";
		assertNoHover(value);
	}

	@Test
	public void hoverExistingPropertyReferenceWithEmptyPropertyValue() throws BadLocationException {
		String value = "quarkus.application.name=\n" + //
				"my.property = ${quarkus.|application.name}";
		assertNoHover(value);
	}

	@Test
	public void hoverExistingPropertyReferenceWithNullPropertyValue() throws BadLocationException {
		String value = "my.property = ${quarkus.|application.name}\n" + //
				"quarkus.application.name=";
		assertNoHover(value);
	}

}
