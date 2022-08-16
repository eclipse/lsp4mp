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
package org.eclipse.lsp4mp.model;

/**
 * The property value node
 *
 * @author Angelo ZERR
 *
 */
public class PropertyValue extends BasePropertyValue {

	@Override
	public NodeType getNodeType() {
		return NodeType.PROPERTY_VALUE;
	}

	/**
	 * Returns the property value and null otherwise.
	 *
	 * For multiline property values, this method returns the property value with
	 * backslashes and newlines removed.
	 * @return the property value and null otherwise
	 */
	public String getValue() {
		String text = getText(true);
		return text != null ? text.trim() : null;
	}

	@Override
	public Property getParent() {
		return (Property) super.getParent();
	}

	/**
	 * Returns the owner property.
	 *
	 * @return the owner property.
	 */
	public Property getProperty() {
		return getParent();
	}

	public boolean hasExpression() {
		for (Node child : getChildren()) {
			if (child.getNodeType() == NodeType.PROPERTY_VALUE_EXPRESSION) {
				return true;
			}
		}
		return false;
	}

}
