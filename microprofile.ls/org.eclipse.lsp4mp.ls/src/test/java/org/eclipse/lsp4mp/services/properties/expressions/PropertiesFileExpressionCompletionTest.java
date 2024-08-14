/*******************************************************************************
 * Copyright (c) 2022 Red Hat Inc. and others.
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

package org.eclipse.lsp4mp.services.properties.expressions;

import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.c;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.r;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.testCompletionFor;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.junit.Test;

/**
 * Test with property expression completion in 'microprofile-config.properties'
 * file.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesFileExpressionCompletionTest {

	@Test
	public void justDollarSignNoNewline() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = $|";
		testCompletionFor(text, generateInfoFor("test.property", "other.test.property"),
				c("${test.property}", r(1, 22, 23)));
	}

	@Test
	public void justDollarSignNoNewlineItemDefaults() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = $|";
		testCompletionFor(text, true, false, true, null, null, generateInfoFor("test.property", "other.test.property"),
				c("${test.property}", r(1, 22, 23)));
	}

	@Test
	public void justDollarSignNewline() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = $|\n";
		testCompletionFor(text, generateInfoFor("test.property", "other.test.property"),
				c("${test.property}", r(1, 22, 23)));
	}

	@Test
	public void beforeDollarSign() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = |$\n";
		testCompletionFor(text, generateInfoFor("test.property", "other.test.property"), 0);
	}

	@Test
	public void noCloseBraceNoNewline() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = ${|";
		testCompletionFor(text, generateInfoFor("test.property", "other.test.property"),
				c("${test.property}", r(1, 22, 24)));
	}

	@Test
	public void noCloseBraceNewline() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = ${|\n";
		testCompletionFor(text, generateInfoFor("test.property", "other.test.property"),
				c("${test.property}", r(1, 22, 24)));
	}

	@Test
	public void closeBraceNoNewline() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = ${|}";
		testCompletionFor(text, generateInfoFor("test.property", "other.test.property"),
				c("${test.property}", r(1, 22, 25)));
	}

	@Test
	public void closeBraceNewline() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = ${|}\n";
		testCompletionFor(text, generateInfoFor("test.property", "other.test.property"),
				c("${test.property}", r(1, 22, 25)));
	}

	@Test
	public void afterCloseBraceNoNewline() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = ${}|";
		testCompletionFor(text, generateInfoFor("test.property", "other.test.property"), 0);
	}

	@Test
	public void afterCloseBraceNewline() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = ${}|n";
		testCompletionFor(text, generateInfoFor("test.property", "other.test.property"), 0);
	}

	@Test
	public void partiallyFilledCompletionBefore() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = hi|${}";
		testCompletionFor(text, generateInfoFor("test.property", "other.test.property"), 0);
	}

	@Test
	public void partiallyFilledJustDollarSign() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = hi$|";
		testCompletionFor(text, generateInfoFor("test.property", "other.test.property"),
				c("${test.property}", r(1, 24, 25)));
	}

	@Test
	public void afterClosingBraceBetweenTwoLiterals() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.test.property = hello${}|there";
		testCompletionFor(text, generateInfoFor("test.property", "other.test.property"), 0);
	}

	@Test
	public void pickUpPropertiesAfter() throws BadLocationException {
		String text = //
				"property.one = ${|}\n" + //
						"property.two = hi\n" + //
						"property.three = hello\n";
		testCompletionFor(text, generateInfoFor("property.one", "property.two", "property.three"), //
				c("${property.two}", r(0, 15, 18)), //
				c("${property.three}", r(0, 15, 18)));
	}

	@Test
	public void completionAfterNewline() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.property = ${\\\n" + //
						"    |";
		testCompletionFor(text, generateInfoFor("test.property", "other.property"),
				c("${test.property}", r(1, 17, 2, 4)));
	}

	@Test
	public void completionAfterNewlineClosed() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.property = ${\\\n" + //
						"    \\\n" + //
						"|}\n";
		testCompletionFor(text, generateInfoFor("test.property", "other.property"),
				c("${test.property}", r(1, 17, 3, 1)));
	}

	@Test
	public void completionSpaceAfterDollar() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.property = $ |";
		testCompletionFor(text, generateInfoFor("test.property", "other.property"), 0);
	}

	@Test
	public void completionNewlineAfterDollar() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.property = $\\\n" + //
						"|";
		testCompletionFor(text, generateInfoFor("test.property", "other.property"), 0);
	}

	@Test
	public void partiallyCompletedReference() throws BadLocationException {
		String text = //
				"test.property = hello\n" + //
						"other.property = ${te|";
		testCompletionFor(text, generateInfoFor("test.property", "other.property"),
				c("${test.property}", r(1, 17, 21)));
	}

	@Test
	public void hasCommentInIt() throws BadLocationException {
		String text = //
				"# this is an interesting property file\n" + //
						"test.property = hello\n" + //
						"other.property = ${|}";
		testCompletionFor(text, generateInfoFor("test.property", "other.property"),
				c("${test.property}", r(2, 17, 20)));
	}

	@Test
	public void manyUndefinedProperties() throws BadLocationException {
		String text = "test.property = ${|}\n";
		testCompletionFor(text, generateInfoFor("test.property", "http.port", "http.ip"),
				c("${http.port}", r(0, 16, 19)), //
				c("${http.ip}", r(0, 16, 19)));
	}

	@Test
	public void nonExistingProperties() throws BadLocationException {
		String text = "test.property = hi\n" + //
				"other.property = hello\n" + //
				"yet.another.property = ${|";
		testCompletionFor(text, generateInfoFor(), //
				c("${test.property}", r(2, 23, 25)), //
				c("${other.property}", r(2, 23, 25)));
	}

	// Tests for proper cyclic dependency prevention

	@Test
	public void simpleCyclePrevention() throws BadLocationException {
		String text = //
				"test.property = ${other.property}\n" + //
						"other.property = ${|}";
		testCompletionFor(text, generateInfoFor("test.property", "other.property"), 0);
	}

	@Test
	public void multiStepCyclePrevention() throws BadLocationException {
		String text = //
				"property.one = ${property.five}\n" + //
						"property.two = ${property.one}\n" + //
						"property.three = ${property.two}\n" + //
						"property.four = ${property.three}\n" + //
						"property.five = ${|}\n";
		testCompletionFor(text,
				generateInfoFor("property.one", "property.two", "property.three", "property.four", "property.five"), 0);
	}

	@Test
	public void complexDependencies() throws BadLocationException {
		String text = //
				"property.one = ${property.two}${property.three}\n" + //
						"property.two = hello${property.four}\n" + //
						"property.three = hi\n" + //
						"property.four = ${|}";
		testCompletionFor(text, generateInfoFor("property.one", "property.two", "property.three", "property.four"),
				c("${property.three}", r(3, 16, 19)));
	}

	@Test
	public void cyclicPreventionNonExistingProperties() throws BadLocationException {
		String text = "test.property = hi ${other.property}\n" + //
				"other.property = ${yet.another.property}\n" + //
				"yet.another.property = $|";
		testCompletionFor(text, generateInfoFor(), 0);
	}

	@Test
	public void complexDependencies2() throws BadLocationException {
		String text = "A=${B}\n" + //
				"B=${C}${F}\n" + //
				"C=${|\n" + //
				"D=hi\n" + //
				"E=${D}${C}\n" + //
				"F=${D}";
		testCompletionFor(text, generateInfoFor(), //
				c("${D}", r(2, 2, 4)), //
				c("${F}", r(2, 2, 4)));
	}

	@Test
	public void cyclicPreventionNonExistingAndProjectProperties() throws BadLocationException {
		String text = "quarkus.http.port=${port}\n" + //
				"port=8080${|}\n" + //
				"url=localhost:${port}";
		testCompletionFor(text, generateInfoFor("quarkus.http.port"), 0);
	}

	@Test
	public void selfLoopsDontPreventCompletion() throws BadLocationException {
		String text = "a = ${a}\n" + //
				"b = $|";
		testCompletionFor(text, generateInfoFor(), c("${a}", r(1, 4, 5)));
	}

	@Test
	public void referencedJavadPropertyWithoutDefaultValue() throws BadLocationException {
		String text = "a = ${|}\n" + //
				"b = c";
		testCompletionFor(text, generateInfoFor("quarkus.http.port"), //
				c("${b}", r(0, 4, 7)), //
				c("${quarkus.http.port}", r(0, 4, 7)));
	}

	@Test
	public void referencedJavadPropertyWithDefaultValue() throws BadLocationException {
		String text = "a = ${|}\n" + //
				"b = c";
		MicroProfileProjectInfo info = generateInfoFor("quarkus.http.port");
		info.getProperties().get(0).setDefaultValue("8080");
		testCompletionFor(text, info, //
				c("${b}", r(0, 4, 7)));
	}

	@Test
	public void expressionDefaultValue() throws BadLocationException {
		String text = "quarkus.log.level = ${ENV_LEVEL:|}";
		testCompletionFor(text, true, //
				c("OFF", "OFF", r(0, 32, 32)), //
				c("SEVERE", "SEVERE", r(0, 32, 32)));
	}

	@Test
	public void complexExpressions1() throws BadLocationException {
		String text = "asdf = ${${hjkl}}\n" + //
				"hjkl = ${qwerty}\n" + //
				"foo = bar\n" + //
				"qwerty = ${|}\n";
		testCompletionFor(text, generateInfoFor("asdf", "hjkl", "foo", "qwerty"), //
				c("${foo}", r(3, 9, 12)));
	}

	@Test
	public void complexExpressions2() throws BadLocationException {
		String text = "asdf = ${hjkl:${qwerty}}\n" + //
				"foo = bar\n" + //
				"qwerty = ${|}\n";
		testCompletionFor(text, generateInfoFor("asdf", "hjkl", "foo", "qwerty"), //
				c("${foo}", r(2, 9, 12)), c("${hjkl}", r(2, 9, 12)));
	}

	@Test
	public void complexExpressions3() throws BadLocationException {
		String text = "asdf = ${${hjkl}}\n" + //
				"hjkl = ${asdf}\n" + //
				"qwerty = ${|}\n";
		testCompletionFor(text, generateInfoFor("asdf", "hjkl", "qwerty"), //
				c("${asdf}", r(2, 9, 12)), c("${hjkl}", r(2, 9, 12)));
	}

	// Utility functions

	private static MicroProfileProjectInfo generateInfoFor(String... properties) {
		MicroProfileProjectInfo projectInfo = new MicroProfileProjectInfo();
		projectInfo.setProperties(Arrays.asList(properties).stream().map(p -> {
			return item(p);
		}).collect(Collectors.toList()));
		return projectInfo;
	}

	private static ItemMetadata item(String name) {
		ItemMetadata itemMetadata = new ItemMetadata();
		itemMetadata.setName(name);
		itemMetadata.setRequired(false);
		return itemMetadata;
	}

}