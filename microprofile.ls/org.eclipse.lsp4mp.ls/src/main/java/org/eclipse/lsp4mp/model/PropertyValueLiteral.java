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
package org.eclipse.lsp4mp.model;

/**
 * Represents text in a property value that should be interpreted literally.
 */
public class PropertyValueLiteral extends Node {

	@Override
	public NodeType getNodeType() {
		return NodeType.PROPERTY_VALUE_LITERAL;
	}

	/**
	 * Returns the text this node contains and null otherwise.
	 *
	 * If this node covers more than one line, the backslashes and newlines are
	 * removed.
	 *
	 * @return the text this node contains and null otherwise
	 */
	public String getValue() {
		String text = getText(true);
		return text != null ? text.trim() : null;
	}

}