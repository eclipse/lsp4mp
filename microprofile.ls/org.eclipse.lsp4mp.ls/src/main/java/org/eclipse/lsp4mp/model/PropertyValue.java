/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v2.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
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
public class PropertyValue extends Node {

	@Override
	public NodeType getNodeType() {
		return NodeType.PROPERTY_VALUE;
	}

	/**
	 * Returns the property value and null otherwise.
	 * 
	 * For multiline property values, this method returns the property value with
	 * backslashes and newlines removed.
	 * 
	 * @return the property value and null otherwise
	 */
	public String getValue() {
		String text = getText(true);
		return text != null ? text.trim() : null;
	}

}
