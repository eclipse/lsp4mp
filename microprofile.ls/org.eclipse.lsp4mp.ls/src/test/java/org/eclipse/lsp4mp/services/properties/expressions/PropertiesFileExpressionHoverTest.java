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

import org.junit.Test;

/**
 * Test with property expression hover in 'microprofile-config.properties' file.
 *
 */
public class PropertiesFileExpressionHoverTest {

	@Test
	public void hoverWithEnums() throws Exception {
		String value = "quarkus.log.console.async.overflow=${ENV:BLO|CK}";
		// OverflowAction enum type
		String hoverLabel = "**BLOCK**" + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabel, 41);
	}

	@Test
	public void hoverOnValueForLevelBasedOnRule() throws Exception {
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
	public void hoverWithEnumsKebabCase() throws Exception {
		String value = "quarkus.datasource.transaction-isolation-level = ${ENV:read-unc|ommitted}";
		// io.agroal.api.configuration.AgroalConnectionFactoryConfiguration.TransactionIsolation
		// enum type
		String hoverLabel = "**READ_UNCOMMITTED**" + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabel, 55);
	}

	@Test
	public void hoverInvalidProperty() throws Exception {
		String value = "aaa=${b|bb}";
		assertNoHover(value);
	}

	@Test
	public void hoverPropertiesFileReference() throws Exception {
		String value = "property.one = hello\n" + //
				"property.two = ${prope|rty.one}";
		String hoverLabel = "hello";
		assertHoverMarkdown(value, hoverLabel, 15);
	}

	@Test
	public void hoverDefaultPropertyValue() throws Exception {
		String value = "property.one = ${mp.openapi.s|can.disable}";
		String hoverLabel = "false";
		assertHoverMarkdown(value, hoverLabel, 15);
	}

	@Test
	public void hoverResolveTwoSteps() throws Exception {
		String value = "property.one = hello\n" + //
				"property.two = ${property.one}\n" + //
				"property.three = ${property.two}\n" + //
				"property.four = ${property.three}\n" + //
				"property.five = ${property|.four}";
		String hoverLabel = "hello";
		assertHoverMarkdown(value, hoverLabel, 16);
	}

	@Test
	public void hoverMixtureOfReferencesAndRawText() throws Exception {
		String value = "property.one = one\n" + //
				"property.two = ${property.one} two\n" + //
				"property.four = ${proper|ty.three} five\n" + //
				"property.three = ${property.one} ${property.two} three";
		String hoverLabel = "one one two three";
		assertHoverMarkdown(value, hoverLabel, 16);
	}

	@Test
	public void hoverSelfReference() throws Exception {
		String value = "property.one = ${prop|erty.one}";
		assertHoverMarkdown(value, "${property.one}", 15);
	}

	@Test
	public void hoverCircularReference() throws Exception {
		String value = "property.one = ${property.two}\n" + //
				"property.two = ${p|roperty.one}";
		assertHoverMarkdown(value, "${property.two}", 15);
	}

	@Test
	public void hoverDependencyTreeAndDefaultValue() throws Exception {
		String value = "property.one = $|{property.two}\n" + //
				"property.two=${property.three} ${mp.openapi.scan.disable}\n" + //
				"property.three = hello";
		assertHoverMarkdown(value, "hello false", 15);
	}

	@Test
	public void hoverAnyCyclePreventsRecursiveResolution() throws Exception {
		String value = "property.one = ${p|roperty.two}\n" + //
				"property.two = ${property.three}\n" + //
				"property.three = hi\n" + //
				"property.four = ${property.five}\n" + //
				"property.five = ${property.four}\n";
		assertHoverMarkdown(value, "${property.three}", 15);
	}

	@Test
	public void hoverEmptyPropertyName() throws Exception {
		String value = "property.one = ${|}";
		assertNoHover(value);
	}

	@Test
	public void hoverBoundsOnPropertyExpression() throws Exception {
		String value = "property.one = |${property.two}\n" + //
				"property.two = hello";
		assertHoverMarkdown(value, "hello", 15);
		value = "property.one = ${property.two}|\n" + //
				"property.two = hello";
		assertHoverMarkdown(value, "hello", 15);
	}

	@Test
	public void hoverMultilineReferencePropertyExpression() throws Exception {
		String value = "property.one = ${prope|rty.two}\n" + //
				"property.two = hello \\\n" + //
				"  there";
		assertHoverMarkdown(value, "hello there", 15);
	}

	@Test
	public void hoverKeyWithReference() throws Exception {
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
	public void hoverKeyWithNoReferenceButWithDefaultValue() throws Exception {
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
	public void hoverKeyUndefinedProperty() throws Exception {
		String value = "pr|operty = value";
		assertHoverMarkdown(value, "**property**" + System.lineSeparator() + System.lineSeparator() + //
				" * Value: `value`", 0);
	}

	@Test
	public void hoverKeyUndefinedPropertyWithReference() throws Exception {
		String value = "value = amount\n" + //
				"pr|operty = ${value}";
		assertHoverMarkdown(value, "**property**" + System.lineSeparator() + System.lineSeparator() + //
				" * Value: `amount`",
				0);
	}

	@Test
	public void hoverKeyUndefinedPropertyWithReferenceAndProfile() throws Exception {
		String value = "value = amount\n" + //
				"%dev.pr|operty = ${value}";
		assertHoverMarkdown(value, "**property**" + System.lineSeparator() + System.lineSeparator() + //
				" * Profile: `dev`" + System.lineSeparator() + //
				" * Value: `amount`",
				0);
	}

	@Test
	public void hoverKeyWithReferenceAndSelfLoop() throws Exception {
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
	public void hoverKeyWithReferenceToEmptyProperty() throws Exception {
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
						" * Value: `${value}`" + //
						System.lineSeparator() + //
						" * Extension: `microprofile-metrics-api`",
				0);
	}

	@Test
	public void hoverKeyWithReferenceToBlankProperty() throws Exception {
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
						" * Value: `${value}`" + //
						System.lineSeparator() + //
						" * Extension: `microprofile-metrics-api`",
				0);
	}

	@Test
	public void hoverExistingPropertyReferenceWithoutADefaultValue() throws Exception {
		String value = "my.property = ${quarkus.app|lication.name}";
		assertNoHover(value);
	}

	@Test
	public void hoverExistingPropertyReferenceWithEmptyPropertyValue() throws Exception {
		String value = "quarkus.application.name=\n" + //
				"my.property = ${quarkus.|application.name}";
		assertNoHover(value);
	}

	@Test
	public void hoverExistingPropertyReferenceWithNullPropertyValue() throws Exception {
		String value = "my.property = ${quarkus.|application.name}\n" + //
				"quarkus.application.name=";
		assertNoHover(value);
	}

	@Test
	public void hoverWithNestedPropertyExpression1() throws Exception {
		String value = "asdf=${hj|kl}\n" + //
				"hjkl = ${${foo}}\n" + //
				"foo = bar\n" + //
				"bar = qwerty";
		assertHoverMarkdown(value, "qwerty", 5);
	}

	@Test
	public void hoverWithNestedPropertyExpression2() throws Exception {
		String value = "asdf=${hj|kl}\n" + //
				"hjkl = ${${foo}}\n" + //
				"foo = bar\n" + //
				"bar = ";
		assertHoverMarkdown(value, "${bar}", 5);
	}

	@Test
	public void hoverWithNestedPropertyExpression3() throws Exception {
		String value = "asdf=${hj|kl}\n" + //
				"hjkl = ${${asdf}}\n";
		assertHoverMarkdown(value, "${${asdf}}", 5);
	}

	@Test
	public void hoverWithNestedPropertyExpression4() throws Exception {
		String value = "asdf=${hj|kl}\n" + //
				"hjkl = ${}\n";
		assertHoverMarkdown(value, "${}", 5);
	}

	@Test
	public void hoverWithNestedPropertyExpression5() throws Exception {
		String value = "asdf=${hj|kl}\n" + //
				"hjkl = ${${foo}:ASDF}\n";
		assertHoverMarkdown(value, "ASDF", 5);
	}

	@Test
	public void hoverWithNestedPropertyExpression6() throws Exception {
		String value = "asdf=${hj|kl}\n" + //
				"hjkl = ${${foo}:${foo:}\n";
		assertNoHover(value);
	}

	@Test
	public void hoverWithNestedPropertyExpression7() throws Exception {
		String value = "asdf=${hj|kl}\n" + //
				"hjkl = ${${foo}:${foo:ASDF}\n";
		assertHoverMarkdown(value, "ASDF", 5);
	}

	@Test
	public void hoverWithBillionLaughs() throws Exception {
		String value = "asdf=${lu|lz}\n" + //
				"lulz=${lol9}${lol9}${lol9}${lol9}${lol9}${lol9}${lol9}${lol9}${lol9}\n" //
				+ "lol9=${lol8}${lol8}${lol8}${lol8}${lol8}${lol8}${lol8}${lol8}${lol8}\n" //
				+ "lol8=${lol7}${lol7}${lol7}${lol7}${lol7}${lol7}${lol7}${lol7}${lol7}\n" //
				+ "lol7=${lol6}${lol6}${lol6}${lol6}${lol6}${lol6}${lol6}${lol6}${lol6}\n" //
				+ "lol6=${lol5}${lol5}${lol5}${lol5}${lol5}${lol5}${lol5}${lol5}${lol5}\n" //
				+ "lol5=${lol4}${lol4}${lol4}${lol4}${lol4}${lol4}${lol4}${lol4}${lol4}\n" //
				+ "lol4=${lol3}${lol3}${lol3}${lol3}${lol3}${lol3}${lol3}${lol3}${lol3}\n" //
				+ "lol3=${lol2}${lol2}${lol2}${lol2}${lol2}${lol2}${lol2}${lol2}${lol2}\n" //
				+ "lol2=${lol1}${lol1}${lol1}${lol1}${lol1}${lol1}${lol1}${lol1}${lol1}\n" //
				+ "lol1=${lol}${lol}${lol}${lol}${lol}${lol}${lol}${lol}${lol}${lol}\n" //
				+ "lol=lol";
		assertHoverMarkdown(value, "${lol9}${lol9}${lol9}${lol9}${lol9}${lol9}${lol9}${lol9}${lol9}", 5);
	}

}
