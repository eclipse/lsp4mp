/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4mp.services.properties;

import static org.eclipse.lsp4mp.services.PropertiesFileAssert.assertHoverMarkdown;
import static org.eclipse.lsp4mp.services.PropertiesFileAssert.assertHoverPlaintext;
import static org.eclipse.lsp4mp.services.PropertiesFileAssert.assertNoHover;

import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.junit.Test;

/**
 * Test with hover in 'application.properties' file.
 *
 */
public class PropertiesFileHoverTest {

	@Test
	public void unkwownProperty() throws BadLocationException {
		String value = "unkwo|wn";
		String hoverLabel = null;
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testKeyHoverMarkdown() throws BadLocationException {
		String value = "quarkus.applica|tion.name = name";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\nIf not set, defaults to the name of the project (except for tests where it is not set at all)."
				+ System.lineSeparator() + System.lineSeparator() + //
				" * Type: `java.util.Optional<java.lang.String>`" + System.lineSeparator() + //
				" * Value: `name`" + System.lineSeparator() + //
				" * Phase: `buildtime & runtime`" + System.lineSeparator() + //
				" * Extension: `quarkus-core`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testKeyHoverPlaintext() throws BadLocationException {
		String value = "quarkus.applica|tion.name = name";
		String hoverLabel = "quarkus.application.name" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\nIf not set, defaults to the name of the project (except for tests where it is not set at all)."
				+ System.lineSeparator() + System.lineSeparator() + //
				"Type: java.util.Optional<java.lang.String>" + System.lineSeparator() + //
				"Value: name" + System.lineSeparator() + //
				"Phase: buildtime & runtime" + System.lineSeparator() + //
				"Extension: quarkus-core";
		assertHoverPlaintext(value, hoverLabel, 0);
	};

	@Test
	public void testKeyHoverNoSpaces() throws BadLocationException {
		String value = "quarkus.applica|tion.name=name";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\nIf not set, defaults to the name of the project (except for tests where it is not set at all)."
				+ System.lineSeparator() + System.lineSeparator() + //
				" * Type: `java.util.Optional<java.lang.String>`" + System.lineSeparator() + //
				" * Value: `name`" + System.lineSeparator() + //
				" * Phase: `buildtime & runtime`" + System.lineSeparator() + //
				" * Extension: `quarkus-core`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testNoKeyHoverOnEqualsSign() throws BadLocationException {
		assertHoverMarkdown("quarkus.application.name |= name", null, 0);
		assertHoverMarkdown("quarkus.application.name|=name", null, 0);
		assertHoverMarkdown("quarkus.log.syslog.async.overflow|=DISCARD", null, 0);
	};

	@Test
	public void testNoValueHoverOnEqualsSign() throws BadLocationException {
		assertHoverMarkdown("quarkus.log.syslog.async.overflow |= DISCARD", null, 0);
		assertHoverMarkdown("quarkus.log.syslog.async.overflow|=DISCARD", null, 0);
	};

	@Test
	public void testNoHoverOnEqualsWhenNoValue() throws BadLocationException {
		String value = "a=1\n" + //
				"b=|";
		assertHoverMarkdown(value, null, 0);
	};

	@Test
	public void testDefaultProfileHover() throws BadLocationException {
		String value = "%d|ev.quarkus.log.syslog.async.overflow=DISCARD";
		String hoverLabelMarkdown = "**dev**" + System.lineSeparator() + System.lineSeparator()
				+ "Profile activated when in development mode (quarkus:dev)." + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabelMarkdown, 0);
	}

	@Test
	public void testDefaultProfileHoverSpacesInFront() throws BadLocationException {
		String value = "        %d|ev.quarkus.log.syslog.async.overflow=DISCARD";
		String hoverLabelMarkdown = "**dev**" + System.lineSeparator() + System.lineSeparator()
				+ "Profile activated when in development mode (quarkus:dev)." + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabelMarkdown, 8);
	}

	@Test
	public void testOnlyDefaultProfile() throws BadLocationException {
		String value = "%de|v";
		String hoverLabelMarkdown = "**dev**" + System.lineSeparator() + System.lineSeparator()
				+ "Profile activated when in development mode (quarkus:dev)." + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabelMarkdown, 0);

		value = "|%prod";
		hoverLabelMarkdown = "**prod**" + System.lineSeparator() + System.lineSeparator()
				+ "The default profile when not running in development or test mode." + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabelMarkdown, 0);

		value = "%test|";
		hoverLabelMarkdown = "**test**" + System.lineSeparator() + System.lineSeparator()
				+ "Profile activated when running tests." + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabelMarkdown, 0);
	};

	@Test
	public void testOnlyNonDefaultProfile() throws BadLocationException {
		String value = "%hel|lo";
		String hoverLabel = null;
		assertHoverMarkdown(value, hoverLabel, 0);

		value = "%hello|";
		assertHoverMarkdown(value, hoverLabel, 0);

		value = "|%hello";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testKeyWithProfileHoverMarkdown() throws BadLocationException {
		String value = "%dev.quarkus.applica|tion.name = name";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\nIf not set, defaults to the name of the project (except for tests where it is not set at all)."
				+ System.lineSeparator() + System.lineSeparator() + //
				" * Profile: `dev`" + System.lineSeparator() + //
				" * Type: `java.util.Optional<java.lang.String>`" + System.lineSeparator() + //
				" * Value: `name`" + System.lineSeparator() + //
				" * Phase: `buildtime & runtime`" + System.lineSeparator() + //
				" * Extension: `quarkus-core`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testKeyMap() throws BadLocationException {
		String value = "quar|kus.log.category.\"com.lordofthejars\".level=DEBUG";
		String hoverLabel = "**quarkus.log.category.\\{\\*\\}.level**" + System.lineSeparator() + System.lineSeparator()
				+ //
				"The log level level for this category" + System.lineSeparator() + System.lineSeparator() + //
				" * Type: `io.quarkus.runtime.logging.InheritableLevel`" + System.lineSeparator() + //
				" * Default: `inherit`" + System.lineSeparator() + //
				" * Value: `DEBUG`" + System.lineSeparator() + //
				" * Phase: `runtime`" + System.lineSeparator() + //
				" * Extension: `quarkus-core`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void hoverWithEnums() throws BadLocationException {
		String value = "quarkus.log.console.async.overflow=BLO|CK";
		// OverflowAction enum type
		String hoverLabel = "**BLOCK**" + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabel, 35);
	}

	@Test
	public void hoverOnValueForLevelBasedOnRule() throws BadLocationException {
		// quarkus.log.file.level has 'java.util.logging.Level'
		String value = "quarkus.log.file.level=OF|F ";
		String hoverLabel = "**OFF**" + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`OFF` is a special level that can be used to turn off logging.\nThis level is initialized to `Integer.MAX_VALUE`."
				+ //
				System.lineSeparator();
		assertHoverMarkdown(value, hoverLabel, 23);
	}

	@Test
	public void hoverWithEnumsKebabCase() throws BadLocationException {
		String value = "quarkus.datasource.transaction-isolation-level = read-unc|ommitted";
		// io.agroal.api.configuration.AgroalConnectionFactoryConfiguration.TransactionIsolation
		// enum type
		String hoverLabel = "**READ_UNCOMMITTED**" + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabel, 49);
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
