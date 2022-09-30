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
package org.eclipse.lsp4mp.services.properties;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.metadata.ConfigurationMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ValueHint;
import org.eclipse.lsp4mp.commons.utils.ConfigSourcePropertiesProviderUtils;
import org.eclipse.lsp4mp.commons.utils.IConfigSourcePropertiesProvider;
import org.eclipse.lsp4mp.commons.utils.PropertyValueExpander;
import org.eclipse.lsp4mp.commons.utils.StringUtils;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.model.BasePropertyValue;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.PropertyKey;
import org.eclipse.lsp4mp.model.PropertyValueExpression;
import org.eclipse.lsp4mp.settings.MicroProfileHoverSettings;
import org.eclipse.lsp4mp.utils.DocumentationUtils;
import org.eclipse.lsp4mp.utils.PositionUtils;
import org.eclipse.lsp4mp.utils.PropertiesFileUtils;

/**
 * The properties file hover support.
 */
class PropertiesFileHover {

	private static final Logger LOGGER = Logger.getLogger(PropertiesFileHover.class.getName());

	/**
	 * Returns Hover object for the currently hovered token
	 *
	 * @param document      the properties model document
	 * @param position      the hover position
	 * @param projectInfo   the MicroProfile project information
	 * @param hoverSettings the hover settings
	 * @param cancelChecker the cancel checker
	 * @return Hover object for the currently hovered token
	 */
	public Hover doHover(PropertiesModel document, Position position, MicroProfileProjectInfo projectInfo,
			MicroProfileHoverSettings hoverSettings, CancelChecker cancelChecker) {

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
			PropertyValueExpression propExpr = (PropertyValueExpression) node;
			boolean inDefaultValue = propExpr.isInDefaultValue(offset);
			if (inDefaultValue) {
				// quarkus.log.file.level=${ENV:OF|F}
				return getPropertyValueHover(propExpr, inDefaultValue, projectInfo, hoverSettings);
			}
			// quarkus.log.file.level=${E|NV:OFF}
			return getPropertyValueExpressionHover(propExpr, projectInfo, hoverSettings, cancelChecker);
		case PROPERTY_VALUE_LITERAL:
		case PROPERTY_VALUE:
			return getPropertyValueHover((BasePropertyValue) node, false, projectInfo, hoverSettings);
		case PROPERTY_KEY:
			PropertyKey key = (PropertyKey) node;
			if (key.isBeforeProfile(offset)) {
				// hover documentation on profile
				return getProfileHover(key, hoverSettings);
			} else {
				// hover documentation on property key
				return getPropertyKeyHover(key, projectInfo, hoverSettings, cancelChecker);
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
	 * @param cancelChecker the cancel checker
	 * @return the documentation hover for property key represented by token
	 */
	private static Hover getPropertyKeyHover(PropertyKey key, MicroProfileProjectInfo projectInfo,
			MicroProfileHoverSettings hoverSettings, CancelChecker cancelChecker) {
		boolean markdownSupported = hoverSettings.isContentFormatSupported(MarkupKind.MARKDOWN);
		// retrieve MicroProfile property from the project information
		String propertyName = key.getPropertyName();

		PropertiesModel model = key.getOwnerModel();
		IConfigSourcePropertiesProvider propertiesProvider = ConfigSourcePropertiesProviderUtils.layer(model,
				new PropertiesInfoPropertiesProvider(projectInfo.getProperties()));
		PropertyValueExpander expander = new PropertyValueExpander(propertiesProvider);
		cancelChecker.checkCanceled();

		String propertyValue = expander.getValue(key.getPropertyNameWithProfile());
		if (!StringUtils.hasText(propertyValue)) {
			propertyValue = null;
		}

		ItemMetadata item = PropertiesFileUtils.getProperty(propertyName, projectInfo);

		if (item != null || propertyValue != null) {
			Hover hover = new Hover();
			MarkupContent markupContent = null;
			if (item != null) {
				// MicroProfile property found, display the documentation as hover
				markupContent = DocumentationUtils.getDocumentation(item, key.getProfile(), propertyValue,
						markdownSupported);
			} else {
				// The property was not found, display just the resolved value
				markupContent = DocumentationUtils.getDocumentation(key.getProfile(), propertyName, propertyValue,
						markdownSupported);
			}
			hover.setContents(markupContent);
			hover.setRange(PositionUtils.createRange(key));
			return hover;
		}

		return null;
	}

	/**
	 * Returns the documentation hover for the given property value.
	 *
	 * @param value          the property value node
	 * @param inDefaultValue true if it's a default value expression.
	 * @param projectInfo    the MicroProfile project information
	 * @param hoverSettings  the hover settings
	 *
	 * @return the documentation hover for the given property value.
	 */
	private static Hover getPropertyValueHover(BasePropertyValue value, boolean inDefaultValue,
			MicroProfileProjectInfo projectInfo, MicroProfileHoverSettings hoverSettings) {
		// retrieve MicroProfile property from the project information
		String propertyValue = value.getValue();
		if (StringUtils.isEmpty(propertyValue)) {
			return null;
		}
		String propertyName = value.getProperty().getPropertyName();
		ItemMetadata item = PropertiesFileUtils.getProperty(propertyName, projectInfo);
		ValueHint enumItem = getValueHint(propertyValue, item, projectInfo, value.getOwnerModel());
		if (enumItem != null) {
			// MicroProfile property enumeration item, found, display its documentation as
			// hover
			boolean markdownSupported = hoverSettings.isContentFormatSupported(MarkupKind.MARKDOWN);
			MarkupContent markupContent = DocumentationUtils.getDocumentation(enumItem, markdownSupported);
			Hover hover = new Hover();
			hover.setContents(markupContent);
			Range range = inDefaultValue ? PositionUtils.selectDefaultValue((PropertyValueExpression) value)
					: PositionUtils.createRange(value);
			hover.setRange(range);
			return hover;
		}
		return null;
	}

	private static Hover getPropertyValueExpressionHover(PropertyValueExpression node,
			MicroProfileProjectInfo projectInfo, MicroProfileHoverSettings hoverSettings, CancelChecker cancelChecker) {
		String referencedProp = node.getReferencedPropertyName();
		if (referencedProp == null) {
			return null;
		}

		PropertiesModel model = node.getOwnerModel();
		IConfigSourcePropertiesProvider propertiesProvider = ConfigSourcePropertiesProviderUtils.layer(model, new PropertiesInfoPropertiesProvider(projectInfo.getProperties()));
		PropertyValueExpander expander = new PropertyValueExpander(propertiesProvider);
		cancelChecker.checkCanceled();

		String resolvedValue = expander.getValue(referencedProp);

		if (StringUtils.hasText(resolvedValue)) {
			return createHover(resolvedValue, node);
		}
		cancelChecker.checkCanceled();

		// Check project info for the default value
		ItemMetadata projectProp = PropertiesFileUtils.getProperty(referencedProp, projectInfo);
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