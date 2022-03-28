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
import org.eclipse.lsp4mp.model.PropertyValueExpression;

/**
 * Properties file position utilities.
 * 
 * @author Angelo ZERR
 *
 */
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
	 * Select the referenced property from the given expression.
	 * 
	 * <p>
	 * 
	 * ${|referenced.propert|y:default_value}
	 * 
	 * </p>
	 * 
	 * @param expression the property expression.
	 * 
	 * @return the referenced property range from the given expression.
	 */
	public static Range selectReferencedProperty(PropertyValueExpression expression) {
		int startOffset = expression.getReferenceStartOffset();
		int endOffset = expression.getReferenceEndOffset();
		return createRange(startOffset, endOffset, expression.getDocument());
	}

	/**
	 * Select the default value from the given expression.
	 * 
	 * <p>
	 * 
	 * ${referenced.property:|default_valu|e}
	 * 
	 * </p>
	 * 
	 * @param expression the property expression.
	 * 
	 * @return the default value range from the given expression.
	 */
	public static Range selectDefaultValue(PropertyValueExpression expression) {
		int startOffset = expression.getDefaultValueStartOffset();
		int endOffset = expression.getDefaultValueEndOffset();
		return createRange(startOffset, endOffset, expression.getDocument());
	}

}