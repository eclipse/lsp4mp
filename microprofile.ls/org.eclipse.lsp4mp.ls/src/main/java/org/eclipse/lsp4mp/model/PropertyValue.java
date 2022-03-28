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

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;

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

	/**
	 * Returns the property value with the property expressions resolved,
	 * or null if a circular dependency between properties exists.
	 * 
	 * @param graph The dependencies between properties
	 * @param projectInfo the project information
	 * @return The property value with the property expressions resolved,
	 * or null if a circular dependency between properties exists.
	 */
	public String getResolvedValue(PropertyGraph graph, MicroProfileProjectInfo projectInfo) {
		if (!graph.isAcyclic()) {
			return null;
		}
		StringBuilder resolvedValue = new StringBuilder();
		for (Node child : getChildren()) {
			switch (child.getNodeType()) {
				case PROPERTY_VALUE_LITERAL:
					resolvedValue.append(child.getText(true));
					break;
				case PROPERTY_VALUE_EXPRESSION:
					PropertyValueExpression propValExpr = (PropertyValueExpression) child;
					String resolvedVal = propValExpr.getResolvedValue(graph, projectInfo);
					if (resolvedVal == null) {
						return null;
					}
					resolvedValue.append(propValExpr.getResolvedValue(graph, projectInfo));
					break;
				default:
					assert false;
			}
		}
		return resolvedValue.toString();
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

}
