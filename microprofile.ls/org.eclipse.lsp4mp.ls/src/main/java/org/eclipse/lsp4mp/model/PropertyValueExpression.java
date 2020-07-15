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
 * Represents a portion of the property value that refers to the value of
 * another property.
 *
 * When properties file is processed, the reference is replaced with the value
 * of the other property. In the properties file, it has the form:
 * <code>${other.property.name}</code>
 */
public class PropertyValueExpression extends Node {

	@Override
	public NodeType getNodeType() {
		return NodeType.PROPERTY_VALUE_EXPRESSION;
	}

	/**
	 * Returns the text that this Node contains.
	 *
	 * Removes backslashes, and newlines. Doesn't not resolve the reference to
	 * another property.
	 */
	public String getValue() {
		String text = getText(true);
		return text != null ? text.trim() : null;
	}

	/**
	 * Get the name of the referenced property, or null if the opening bracket is
	 * missing in the property expression.
	 *
	 * Does not check if the referenced property exists.
	 *
	 * @return the name of the referenced property, or null if brackets were missing
	 *         in the property expression.
	 */
	public String getReferencedPropertyName() {
		String value = getValue();
		if (value.length() < 2 || !"${".equals(value.substring(0, 2))) {
			return null;
		}
		int end = value.indexOf("}");
		end = end == -1 ? value.length() : end;
		return value.substring(2, end);
	}

	/**
	 * Returns true if the last character in this node is a '}', and false
	 * otherwise.
	 *
	 * @return true if the last character in this node is a '}', and false
	 *         otherwise.
	 */
	public boolean isClosed() {
		String text = getText();
		return text.charAt(text.length() - 1) == '}';
	}

}