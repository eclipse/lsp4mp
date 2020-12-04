/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
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

package org.eclipse.lsp4mp.services.properties;

import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.assertDocumentHighlight;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.r;

import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.junit.Test;

/**
 * Tests for the document highlight in MicroProfile properties files
 */
public class PropertiesFileDocumentHighlightTest {

	@Test
	public void keyHighlightOnePropertyExpression() throws BadLocationException {
		String text = "ke|y = value\n" + //
				"other.key = ${key}";
		assertDocumentHighlight(text, r(0, 0, 3), r(1, 12, 18));
	}

	@Test
	public void keyHighlightMultiplePropertyExpression() throws BadLocationException {
		String text = "ke|y.one = value\n" + //
				"key.two = ${key.one}\n" + //
				"key.three = ${key.one}";
		assertDocumentHighlight(text, //
				r(0, 0, 7), //
				r(1, 10, 20), //
				r(2, 12, 22));
	}

	@Test
	public void keyHighlightNoPropertyExpression() throws BadLocationException {
		String text = "ke|y = value\n" + //
				"other.key = ${cilantro}";
		assertDocumentHighlight(text, r(0, 0, 3));
	}

	@Test
	public void keyHighlightUnclosedPropertyExpression() throws BadLocationException {
		String text = "ke|y = value\n" + //
				"other.key = ${key";
		assertDocumentHighlight(text, //
				r(0, 0, 3), //
				r(1, 12, 17));
	}

	@Test
	public void keyHighlightUnaffectedByComment() throws BadLocationException {
		String text = "ke|y = value\n" + //
				"# hello this is a comment\n" + //
				"# This is another comment\n" + //
				"other.key = ${key";
		assertDocumentHighlight(text, //
				r(0, 0, 3), //
				r(3, 12, 17));
	}

	@Test
	public void keyHighlightCircularReference() throws BadLocationException {
		String text = "|key = ${key}";
		assertDocumentHighlight(text, //
				r(0, 0, 3), //
				r(0, 6, 12));
	}

	@Test
	public void keyHighlightMultipleOnSameRow() throws BadLocationException {
		String text = "key|.one = cool value\n" + //
				"key.two = - ${key.one} - ${key.one}";
		assertDocumentHighlight(text, //
				r(0, 0, 7), //
				r(1, 12, 22), //
				r(1, 25, 35));
	}

	@Test
	public void propertyExpressionHighlightKey() throws BadLocationException {
		String text = "key = value\n" + //
				"other.key = ${k|ey}";
		assertDocumentHighlight(text, //
				r(1, 12, 18), //
				r(0, 0, 3));
	}

	@Test
	public void propertyExpressionHighlightOnlyFirstKey() throws BadLocationException {
		String text = "key = value\n" + //
				"other.key = ${k|ey}" + //
				"key = a different value";
		assertDocumentHighlight(text, //
				r(1, 12, 18), //
				r(0, 0, 3));
	}

	@Test
	public void expressionHighlightUnaffectedByComment() throws BadLocationException {
		String text = "key = value\n" + //
				"# hello this is a comment\n" + //
				"# This is another comment\n" + //
				"other.key = ${k|ey";
		assertDocumentHighlight(text, //
				r(3, 12, 17), //
				r(0, 0, 3));
	}

	@Test
	public void propertyExpressionHighlightCircularReference() throws BadLocationException {
		String text = "key = ${ke|y}";
		assertDocumentHighlight(text, //
				r(0, 6, 12), //
				r(0, 0, 3));
	}

}