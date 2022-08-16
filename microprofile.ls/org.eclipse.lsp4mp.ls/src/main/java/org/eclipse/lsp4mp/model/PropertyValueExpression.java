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
 *
 * @see https://download.eclipse.org/microprofile/microprofile-config-2.0/microprofile-config-spec-2.0.html#property-expressions
 */
public class PropertyValueExpression extends BasePropertyValue {

	private boolean parsed;

	private int referenceNameStartOffset = -1;

	private int referenceNameEndOffset = -1;

	private int defaultValueStartOffset = -1;

	private int defaultValueEndOffset = -1;

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
	@Override
	public String getValue() {
		if (hasDefaultValue()) {
			// ${ENV:SEVERE} --> SEVERE
			return getDefaultValue();
		}
		// ${ENV}
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
		parseExpressionIfNeeded();
		if (referenceNameStartOffset != -1 && referenceNameEndOffset != -1
				&& referenceNameStartOffset != referenceNameEndOffset) {
			return super.getOwnerModel().getText(referenceNameStartOffset, referenceNameEndOffset, true);
		}
		return null;
	}

	/**
	 * Returns true if the last character in this node is a '}', and false
	 * otherwise.
	 *
	 * @return true if the last character in this node is a '}', and false
	 *         otherwise.
	 */
	public boolean isClosed() {
		int end = super.getEnd();
		if (end == -1) {
			return false;
		}
		return super.getOwnerModel().getText().charAt(end - 1) == '}';
	}

	/**
	 * Returns the offset of the start of the referenced property name, or the
	 * offset after the `$` if no property is referenced.
	 *
	 * @return the offset of the start of the referenced property name, or the
	 *         offset after the `$` if no property is referenced.
	 */
	public int getReferenceStartOffset() {
		parseExpressionIfNeeded();
		return referenceNameStartOffset;
	}

	/**
	 * Returns the offset of the end of the referenced property name, or the offset
	 * after the `$` if no property is referenced.
	 *
	 * @return the offset of the end of the referenced property name, or the offset
	 *         after the `$` if no property is referenced.
	 */
	public int getReferenceEndOffset() {
		parseExpressionIfNeeded();
		return referenceNameEndOffset;
	}

	/**
	 * Return the default value and null otherwise.
	 *
	 * <p>
	 * ${ENV:DEFAULT_VALUE}
	 * </p>
	 *
	 * @return the default value and null otherwise.
	 */
	public String getDefaultValue() {
		parseExpressionIfNeeded();
		if (hasDefaultValue()) {
			return super.getOwnerModel().getText(defaultValueStartOffset, defaultValueEndOffset, true);
		}
		return null;
	}

	/**
	 * Return true if the expression has a default value and false otherwise.
	 *
	 * <p>
	 * ${ENV:DEFAULT_VALUE}
	 * </p>
	 *
	 * @return true if the expression has a default value and false otherwise.
	 */

	public boolean hasDefaultValue() {
		parseExpressionIfNeeded();
		return defaultValueStartOffset != -1 && defaultValueEndOffset != -1
				&& defaultValueStartOffset != defaultValueEndOffset;
	}

	/**
	 * Returns the start offset of the default value and -1 otherwise.
	 *
	 * <p>
	 * ${ENV:|DEFAULT_VALUE}
	 * </p>
	 *
	 * @return the start offset of the default value and -1 otherwise.
	 */
	public int getDefaultValueStartOffset() {
		parseExpressionIfNeeded();
		return defaultValueStartOffset;
	}

	/**
	 * Returns the end offset of the default value and -1 otherwise.
	 *
	 * <p>
	 * ${ENV:DEFAULT_VALU|E}
	 * </p>
	 *
	 * @return the end offset of the default value and -1 otherwise.
	 */
	public int getDefaultValueEndOffset() {
		parseExpressionIfNeeded();
		return defaultValueEndOffset;
	}

	/**
	 * Returns true if the given offset is in the default value and false otherwise.
	 *
	 * @param offset the offset.
	 *
	 * @return true if the given offset is in the default value and false otherwise.
	 */
	public boolean isInDefaultValue(int offset) {
		parseExpressionIfNeeded();
		return isIncluded(defaultValueStartOffset, defaultValueEndOffset, offset);
	}

	private void parseExpressionIfNeeded() {
		if (parsed) {
			return;
		}
		parseExpression();
	}

	private synchronized void parseExpression() {
		if (parsed) {
			return;
		}

		int start = super.getStart();
		int end = super.getEnd();
		if (start == -1 || end == -1) {
			return;
		}
		if (start >= end) {
			return;
		}
		boolean nameParsing = true;
		referenceNameStartOffset = start + 2;
		String text = super.getOwnerModel().getText();
		for (int i = referenceNameStartOffset; i < end; i++) {
			char c = text.charAt(i);
			switch (c) {
			case ':':
				if (nameParsing) {
					referenceNameEndOffset = i;
					defaultValueStartOffset = i + 1;
					nameParsing = false;
				}
				break;
			case '}':
				if (nameParsing) {
					referenceNameEndOffset = i;
				} else {
					defaultValueEndOffset = i;
				}
				break;
			}
		}
		if (nameParsing) {
			if (referenceNameEndOffset == -1) {
				referenceNameEndOffset = end;
			}
		} else {
			if (defaultValueEndOffset == -1) {
				defaultValueEndOffset = end;
			}
		}
	}

	@Override
	public PropertyValue getParent() {
		return (PropertyValue) super.getParent();
	}

	@Override
	public Property getProperty() {
		return getParent().getProperty();
	}

}