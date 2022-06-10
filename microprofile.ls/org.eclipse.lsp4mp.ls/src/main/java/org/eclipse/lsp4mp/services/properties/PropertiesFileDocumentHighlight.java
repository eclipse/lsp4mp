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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4mp.commons.utils.StringUtils;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.Node.NodeType;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.model.PropertyKey;
import org.eclipse.lsp4mp.model.PropertyValueExpression;
import org.eclipse.lsp4mp.utils.PositionUtils;

/**
 * The properties file highlighting support.
 */
public class PropertiesFileDocumentHighlight {

	/**
	 * Returns a list of highlights for a MicroProfile properties document
	 *
	 * @param document The MicroProfile properties document
	 * @param position The position of the cursor
	 * @return a list of highlights for a MicroProfile properties document
	 */
	public List<? extends DocumentHighlight> findDocumentHighlight(PropertiesModel document, Position position, CancelChecker cancelChecker) {
		try {
			Node node = document.findNodeAt(position);
			cancelChecker.checkCanceled();
			switch (node.getNodeType()) {
			case PROPERTY_KEY:
				return getPropertyKeyHighlight(document, (PropertyKey) node);
			case PROPERTY_VALUE_EXPRESSION:
				return getPropertyValueExpressionHighlight(document, (PropertyValueExpression) node);
			default:
				return Collections.emptyList();
			}
		} catch (BadLocationException e) {
			return Collections.emptyList();
		}
	}

	/**
	 * Highlight the key portion of the assignment statement for the property that
	 * the property expression references, or nothing
	 *
	 * @param document The properties model that is being worked with
	 * @param node     The property value expression that the cursor is on
	 * @return A singleton list that contains a highlight for the key in the
	 *         assignment statement for the reference property, or an empty list
	 */
	private List<? extends DocumentHighlight> getPropertyValueExpressionHighlight(PropertiesModel document,
			PropertyValueExpression node) {
		String otherProp = node.getReferencedPropertyName();
		if (!StringUtils.hasText(otherProp)) {
			return Collections.emptyList();
		}
		List<DocumentHighlight> highlights = new ArrayList<>(2);
		highlights.add(createHighlight(node, DocumentHighlightKind.Read));
		for (Node child : document.getChildren()) {
			if (child.getNodeType() == NodeType.PROPERTY) {
				Property property = (Property) child;
				if (otherProp.equals(property.getPropertyName())) {
					highlights.add(createHighlight(property.getKey(), DocumentHighlightKind.Write));
					break;
				}
			}
		}
		return highlights;
	}

	/**
	 * Highlight all the property expressions that reference the property that the
	 * given property key represents
	 *
	 * @param document The properties model that is being worked with
	 * @param node     The property key that the cursor is on
	 * @return A list containing highlights for all property expressions in the
	 *         document that reference the expression that the property key
	 *         represents
	 */
	private List<? extends DocumentHighlight> getPropertyKeyHighlight(PropertiesModel document, PropertyKey node) {
		String propertyName = node.getPropertyName();
		if (!StringUtils.hasText(propertyName)) {
			return Collections.emptyList();
		}
		List<DocumentHighlight> highlights = new ArrayList<>();
		highlights.add(createHighlight(node, DocumentHighlightKind.Write));
		for (Node child : document.getChildren()) {
			if (child.getNodeType() == NodeType.PROPERTY) {
				Property property = (Property) child;
				if (property.getValue() != null) {
					for (Node valueSegment : property.getValue().getChildren()) {
						if (valueSegment.getNodeType() == NodeType.PROPERTY_VALUE_EXPRESSION && propertyName
								.equals(((PropertyValueExpression) valueSegment).getReferencedPropertyName())) {
							highlights.add(createHighlight(valueSegment, DocumentHighlightKind.Read));
						}
					}
				}
			}
		}
		return highlights;
	}

	/**
	 * Returns a DocumentHighlight that highlights the given node
	 *
	 * @param n    the node to highlight
	 * @param kind the kind of the DocumentHighlight
	 * @return a DocumentHighlight that highlights the given node
	 */
	private static DocumentHighlight createHighlight(Node n, DocumentHighlightKind kind) {
		return new DocumentHighlight(PositionUtils.createRange(n), kind);
	}

}