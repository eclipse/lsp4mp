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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertyDefinitionParams;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ValueHint;
import org.eclipse.lsp4mp.ls.api.MicroProfilePropertyDefinitionProvider;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.Node.NodeType;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.model.PropertyKey;
import org.eclipse.lsp4mp.model.PropertyValue;
import org.eclipse.lsp4mp.model.PropertyValueExpression;
import org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils;
import org.eclipse.lsp4mp.utils.PositionUtils;

/**
 * The properties file definition.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesFileDefinition {

	private static final Logger LOGGER = Logger.getLogger(PropertiesFileDefinition.class.getName());

	/**
	 * Returns as promise the Java field definition location of the property at the
	 * given <code>position</code> of the given microprofile-config.properties
	 * <code>document</code>.
	 *
	 * @param document    the properties model.
	 * @param position    the position where definition was triggered
	 * @param projectInfo the MicroProfile project info
	 * @param provider    the MicroProfile property definition provider.
	 * @return as promise the Java field definition location of the property at the
	 *         given <code>position</code> of the given
	 *         microprofile-config.properties <code>document</code>.
	 */
	public CompletableFuture<List<LocationLink>> findDefinition(PropertiesModel document, Position position,
			MicroProfileProjectInfo projectInfo, MicroProfilePropertyDefinitionProvider provider) {

		try {
			Node node = document.findNodeAt(position);
			if (node == null) {
				return getEmptyDefinition();
			}

			if (node.getNodeType() == NodeType.PROPERTY_VALUE_EXPRESSION) {
				return CompletableFuture.completedFuture(
						findPropertyValueExpressionDefinition(document, (PropertyValueExpression) node));
			}

			// Get the property at the given position
			PropertyKey key = getPropertyKey(node);

			if (key == null) {
				return getEmptyDefinition();
			}

			// Get metadata of the property
			ItemMetadata item = MicroProfilePropertiesUtils.getProperty(key.getPropertyName(), projectInfo);
			if (item == null) {
				return getEmptyDefinition();
			}

			MicroProfilePropertyDefinitionParams definitionParams = getPropertyDefinitionParams(document, item,
					projectInfo, node);
			if (definitionParams == null) {
				return getEmptyDefinition();
			}
			return provider.getPropertyDefinition(definitionParams).thenApply(target -> {
				if (target == null) {
					return Collections.emptyList();
				}
				LocationLink link = new LocationLink(target.getUri(), target.getRange(), target.getRange(),
						PositionUtils.createRange(node));
				return Collections.singletonList(link);
			});

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In MicroProfileDefinition, position error", e);
		}
		return getEmptyDefinition();
	}

	/**
	 * Finds all locations where the property referenced by
	 * <code>propertyExpression</code> is assigned a value, and returns them as a
	 * list of <code>LocationLink</code>
	 *
	 * If the referenced property is not assigned a value in the properties file,
	 * then a empty list is returned.
	 *
	 * @param document                the PropertiesModel of the
	 *                                PropertyValueExpression
	 * @param propertyValueExpression the PropertyValueExpression whose definition
	 *                                is to be found
	 * @return a list of LocationLinks to the lines where the property referenced by
	 *         the property expression is assigned a value
	 */
	private static List<LocationLink> findPropertyValueExpressionDefinition(PropertiesModel document,
			PropertyValueExpression propertyValueExpression) {

		String propToResolveName = propertyValueExpression.getReferencedPropertyName();

		List<Property> props = new ArrayList<>();

		for (Node modelChild : document.getChildren()) {
			if (modelChild.getNodeType() == NodeType.PROPERTY) {
				Property property = (Property) modelChild;
				if (property.getPropertyName().equals(propToResolveName)
						|| property.getPropertyNameWithProfile().equals(propToResolveName)) {
					props.add(property);
				}
			}
		}

		if (!props.isEmpty()) {
			return getPropertyDefinition(document, propertyValueExpression, props);
		}

		return Collections.emptyList();
	}

	private static CompletableFuture<List<LocationLink>> getEmptyDefinition() {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	private static MicroProfilePropertyDefinitionParams getPropertyDefinitionParams(PropertiesModel document,
			ItemMetadata item, MicroProfileProjectInfo projectInfo, Node node) {

		if (node.getNodeType() != NodeType.PROPERTY_KEY && node.getNodeType() != NodeType.PROPERTY_VALUE
				&& node.getNodeType() != NodeType.PROPERTY_VALUE_EXPRESSION
				&& node.getNodeType() != NodeType.PROPERTY_VALUE_LITERAL) {
			return null;
		}

		MicroProfilePropertyDefinitionParams definitionParams = new MicroProfilePropertyDefinitionParams();

		String sourceType = null;
		String sourceField = null;

		switch (node.getNodeType()) {
			case PROPERTY_KEY: {
				sourceType = item.getSourceType();
				sourceField = item.getSourceField();
				break;
			}
			case PROPERTY_VALUE_EXPRESSION:
			case PROPERTY_VALUE_LITERAL:
				node = node.getParent(); // HACK:
			case PROPERTY_VALUE: {
				sourceType = item.getHintType();
				sourceField = ((PropertyValue) node).getValue();
				// for the case of property which uses kebab_case, we must get the real value of
				// the Java enumeration
				// Ex: for quarkus.datasource.transaction-isolation-level = read-uncommitted
				// the real value of Java enumeration 'read-uncommitted' is 'READ_UNCOMMITTED'
				ItemHint itemHint = projectInfo.getHint(sourceType);
				if (itemHint != null) {
					ValueHint realValue = itemHint.getValue(sourceField, item.getConverterKinds());
					if (realValue != null) {
						sourceField = realValue.getValue();
						if (realValue.getSourceType() != null) {
							sourceType = realValue.getSourceType();
						}
					}
				}
				break;
			}
			default:
				return null;
		}

		// Find definition (class, field of class, method of class, enum) only when
		// metadata
		// contains source type
		if (sourceType == null) {
			return null;
		}

		definitionParams.setSourceType(sourceType);
		definitionParams.setSourceField(sourceField);
		definitionParams.setUri(document.getDocumentURI());
		definitionParams.setSourceMethod(item.getSourceMethod());

		return definitionParams;
	}

	private static List<LocationLink> getPropertyDefinition(PropertiesModel document,
			PropertyValueExpression propertyValueExpression, List<Property> referencedProperties) {

		List<LocationLink> locationLinks = new ArrayList<>();
		for (Property referencedProperty : referencedProperties) {
			Node key = referencedProperty.getKey();
			Range referencedPropertyRange = PositionUtils.createRange(key);
			Range propertyReferenceRange = PositionUtils.createRange(propertyValueExpression.getReferenceStartOffset(),
					propertyValueExpression.getReferenceEndOffset(), referencedProperty.getDocument());
			locationLinks.add(new LocationLink(document.getDocumentURI(), referencedPropertyRange,
					referencedPropertyRange, propertyReferenceRange));
		}
		return locationLinks;
	}

	private static PropertyKey getPropertyKey(Node node) {
		if (node == null) {
			return null;
		}
		switch (node.getNodeType()) {
			case PROPERTY_KEY:
				return (PropertyKey) node;
			case PROPERTY_VALUE:
				return ((Property) node.getParent()).getKey();
			case PROPERTY_VALUE_EXPRESSION:
			case PROPERTY_VALUE_LITERAL:
				return ((Property) node.getParent().getParent()).getKey();
			default:
				return null;
		}
	}
}
