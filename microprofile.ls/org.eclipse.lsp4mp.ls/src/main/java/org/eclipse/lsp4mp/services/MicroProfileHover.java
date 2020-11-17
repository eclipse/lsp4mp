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
package org.eclipse.lsp4mp.services;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.metadata.ConfigurationMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ValueHint;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.Node.NodeType;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.model.PropertyGraph;
import org.eclipse.lsp4mp.model.PropertyKey;
import org.eclipse.lsp4mp.model.PropertyValue;
import org.eclipse.lsp4mp.model.PropertyValueExpression;
import org.eclipse.lsp4mp.settings.MicroProfileHoverSettings;
import org.eclipse.lsp4mp.utils.DocumentationUtils;
import org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils;
import org.eclipse.lsp4mp.utils.PositionUtils;
import org.eclipse.lsp4mp.utils.StringUtils;

/**
 * Retrieves hover documentation and creating Hover object
 */
class MicroProfileHover {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileHover.class.getName());

	/**
	 * Returns Hover object for the currently hovered token
	 *
	 * @param document      the properties model document
	 * @param position      the hover position
	 * @param projectInfo   the MicroProfile project information
	 * @param hoverSettings the hover settings
	 * @return Hover object for the currently hovered token
	 */
	public Hover doHover(PropertiesModel document, Position position, MicroProfileProjectInfo projectInfo,
			MicroProfileHoverSettings hoverSettings) {

		Node node = null;
		int offset = -1;
		try {
			node = document.findNodeAt(position);
			offset = document.offsetAt(position);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "MicroProfileHover, position error", e);
			return null;
		}
		if (node == null) {
			return null;
		}

		switch (node.getNodeType()) {
		case COMMENTS:
			// no hover documentation
			return null;
		case PROPERTY_VALUE_EXPRESSION:
			return getPropertyValueExpressionHover(node, projectInfo, hoverSettings);
		case PROPERTY_VALUE_LITERAL:
			// no hover documentation
			return getPropertyValueHover(node.getParent(), projectInfo, hoverSettings);
		case PROPERTY_VALUE:
			// no hover documentation
			return getPropertyValueHover(node, projectInfo, hoverSettings);
		case PROPERTY_KEY:
			PropertyKey key = (PropertyKey) node;
			if (key.isBeforeProfile(offset)) {
				// hover documentation on profile
				return getProfileHover(key, hoverSettings);
			} else {
				// hover documentation on property key
				return getPropertyKeyHover(key, projectInfo, hoverSettings);
			}

		default:
			return null;
		}
	}

	/**
	 * Returns the documentation hover for the property key's profile, for the
	 * property key represented by <code>key</code>
	 *
	 * Returns null if property key represented by <code>key</code> does not have a
	 * profile
	 *
	 * @param key           the property key
	 * @param hoverSettings the hover settings
	 * @return the documentation hover for the property key's profile
	 */
	private static Hover getProfileHover(PropertyKey key, MicroProfileHoverSettings hoverSettings) {
		boolean markdownSupported = hoverSettings.isContentFormatSupported(MarkupKind.MARKDOWN);
		for (ValueHint profile : QuarkusModel.DEFAULT_PROFILES.getValues()) {
			if (profile.getValue().equals(key.getProfile())) {
				MarkupContent markupContent = DocumentationUtils.getDocumentation(profile, markdownSupported);
				Hover hover = new Hover();
				hover.setContents(markupContent);
				hover.setRange(getProfileHoverRange(key));
				return hover;
			}
		}
		return null;
	}

	/**
	 * Returns the documentation hover for property key represented by the property
	 * key <code>key</code>
	 *
	 * @param key           the property key
	 * @param offset        the hover offset
	 * @param projectInfo   the MicroProfile project information
	 * @param hoverSettings the hover settings
	 * @return the documentation hover for property key represented by token
	 */
	private static Hover getPropertyKeyHover(PropertyKey key, MicroProfileProjectInfo projectInfo,
			MicroProfileHoverSettings hoverSettings) {
		boolean markdownSupported = hoverSettings.isContentFormatSupported(MarkupKind.MARKDOWN);
		// retrieve MicroProfile property from the project information
		String propertyName = key.getPropertyName();

		ItemMetadata item = MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo);
		PropertyValue valueNode = ((Property) key.getParent()).getValue();
		String propertyValue = null;

		if (valueNode != null) {
			PropertyGraph graph = new PropertyGraph(key.getOwnerModel());
			String resolved = valueNode.getResolvedValue(graph, projectInfo);
			if (resolved != null) {
				propertyValue = resolved;
			} else {
				propertyValue = valueNode.getValue();
			}
			if (!StringUtils.hasText(propertyValue)) {
				propertyValue = null;
			}
		}

		if (item != null) {
			// MicroProfile property found, display the documentation as hover
			MarkupContent markupContent = DocumentationUtils.getDocumentation(item, key.getProfile(), propertyValue,
					markdownSupported);
			Hover hover = new Hover();
			hover.setContents(markupContent);
			hover.setRange(PositionUtils.createRange(key));
			return hover;
		}

		return null;
	}

	/**
	 * Returns the documentation hover for property key represented by the property
	 * key <code>node</code>
	 *
	 * @param node          the property key node
	 * @param projectInfo   the MicroProfile project information
	 * @param hoverSettings the hover settings
	 * @return the documentation hover for property key represented by token
	 */
	private static Hover getPropertyValueHover(Node node, MicroProfileProjectInfo projectInfo,
			MicroProfileHoverSettings hoverSettings) {
		PropertyValue value = ((PropertyValue) node);
		boolean markdownSupported = hoverSettings.isContentFormatSupported(MarkupKind.MARKDOWN);
		// retrieve MicroProfile property from the project information
		String propertyValue = value.getValue();
		if (StringUtils.isEmpty(propertyValue)) {
			return null;
		}
		String propertyName = ((Property) (value.getParent())).getPropertyName();
		ItemMetadata item = MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo);
		ValueHint enumItem = getValueHint(propertyValue, item, projectInfo, value.getOwnerModel());
		if (enumItem != null) {
			// MicroProfile property enumeration item, found, display its documentation as
			// hover
			MarkupContent markupContent = DocumentationUtils.getDocumentation(enumItem, markdownSupported);
			Hover hover = new Hover();
			hover.setContents(markupContent);
			hover.setRange(PositionUtils.createRange(node));
			return hover;
		}
		return null;
	}

	private static Hover getPropertyValueExpressionHover(Node node, MicroProfileProjectInfo projectInfo,
			MicroProfileHoverSettings hoverSettings) {
		PropertyValueExpression propValExpr = (PropertyValueExpression) node;
		String referencedProp = propValExpr.getReferencedPropertyName();
		if (referencedProp == null) {
			return null;
		}

		PropertyGraph graph = new PropertyGraph(node.getOwnerModel());

		if (graph.isAcyclic()) {
			String resolvedValue = propValExpr.getResolvedValue(graph, projectInfo);
			if (StringUtils.hasText(resolvedValue)) {
				return createHover(resolvedValue, node);
			}
		}

		// Check property file for the value
		for (Node modelChild : node.getOwnerModel().getChildren()) {
			if (modelChild.getNodeType() == NodeType.PROPERTY) {
				Property otherProperty = (Property) modelChild;
				if (referencedProp.equals(otherProperty.getPropertyNameWithProfile())
						&& otherProperty.getPropertyValue() != null && !otherProperty.getPropertyValue().isEmpty()) {
					return createHover(otherProperty.getPropertyValue(), node);
				}
			}
		}
		// Check project info for the default value
		ItemMetadata projectProp = MicroProfilePropertiesUtils.getProperty(referencedProp, projectInfo);
		if (projectProp != null && projectProp.getDefaultValue() != null) {
			return createHover(projectProp.getDefaultValue(), node);
		}
		// Fail
		return null;
	}

	private static Hover createHover(String content, Node range) {
		Hover hover = new Hover();
		MarkupContent markedUp = new MarkupContent();
		markedUp.setKind(MarkupKind.PLAINTEXT);
		markedUp.setValue(content);
		hover.setContents(markedUp);
		hover.setRange(PositionUtils.createRange(range));
		return hover;
	}

	/**
	 * Returns the hover range covering the %profilename in <code>key</code> Returns
	 * range of <code>key</code> if <code>key</code> does not provide a profile
	 *
	 * @param key the property key
	 * @return the hover range covering the %profilename in <code>key</code>
	 */
	private static Range getProfileHoverRange(PropertyKey key) {
		Range range = PositionUtils.createRange(key);

		if (key.getProfile() == null) {
			return range;
		}

		String profile = key.getProfile();
		Position endPosition = range.getEnd();
		endPosition.setCharacter(range.getStart().getCharacter() + profile.length() + 1);
		range.setEnd(endPosition);
		return range;
	}

	private static ValueHint getValueHint(String propertyValue, ItemMetadata metadata,
			ConfigurationMetadata configuration, PropertiesModel model) {
		if (metadata == null) {
			return null;
		}
		ItemHint enumItem = configuration.getHint(metadata);
		if (enumItem != null) {
			ValueHint valueHint = enumItem.getValue(propertyValue, metadata.getConverterKinds());
			if (valueHint != null) {
				return valueHint;
			}
		}
		return null;
	}
}