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
package org.eclipse.lsp4mp.utils;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.ls.commons.TextDocument;
import org.eclipse.lsp4mp.model.Node;

public class PositionUtils {

	public static Range createRange(int startOffset, int endOffset, TextDocument document) {
		try {
			return new Range(document.positionAt(startOffset), document.positionAt(endOffset));
		} catch (BadLocationException e) {
			return null;
		}
	}

	public static Range createRange(Node node) {
		return PositionUtils.createRange(node.getStart(), node.getEnd(), node.getDocument());
	}

	/**
	 * Create a range for the given node and adjust the beginning and ending by the indicated amount of characters
	 *
	 * @param beginAdjust Amount of characters to move the start of the range forward by (negative values supported for going backwards)
	 * @param endAdjust Amount of characters to move the end of the range forward by (negative values supported for going backwards)
	 * @return A range for the given node where the beginning and ending of the range are adjusted by the given amount of characters
	 */
	public static Range createAdjustedRange(Node node, int beginAdjust, int endAdjust) {
		Range range = createRange(node);
		TextDocument doc = node.getDocument();
		try {
			return createRange(doc.offsetAt(range.getStart()) + beginAdjust, doc.offsetAt(range.getEnd()) + endAdjust,
					doc);
		} catch (BadLocationException e) {
			return null;
		}
	}

}