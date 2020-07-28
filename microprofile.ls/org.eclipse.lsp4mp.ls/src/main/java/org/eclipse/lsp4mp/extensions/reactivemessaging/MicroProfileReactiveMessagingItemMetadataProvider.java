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
package org.eclipse.lsp4mp.extensions.reactivemessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.extensions.AbstractItemMetadataProvider;
import org.eclipse.lsp4mp.extensions.ExtendedMicroProfileProjectInfo;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.Node.NodeType;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils;
import org.eclipse.lsp4mp.utils.StringUtils;

/**
 * Properties provider implementation to build MicroProfile Reactive Messaging
 * properties from dynamic incoming/outgoing properties and hint according the
 * assign value of property declared in 'microprofile-config.properties'.
 * 
 * For example if microprofile-config.properties declares:
 * 
 * <code>
 * mp.messaging.incoming.prices.connector = smallrye-kafka
 * </code>
 * 
 * The following property is available:
 * 
 * <code>
 *mp.messaging.incoming.prices.topic = ...
 * </code>
 * 
 * which comes from the dynamic property:
 *
 * <code>
 * mp.messaging.incoming.${smallrye-kafka}.topic
 * </code>
 * 
 * In other words, to compute those properties, this provider uses:
 * <ul>
 * <li>dynamic incoming/outgoing properties for a given connector. It uses the
 * dynamic syntax ${connector-name} (ex:
 * mp.messaging.incoming.${smallrye-kafka}.topic)</li>
 * <li>hints with all connector names (class annotated with @Connector)</li>
 * </ul>
 * 
 * Here a JSON sample:
 * 
 * <code>
 * {
 * 
	"properties": [
		{
			"type": "org.eclipse.microprofile.reactive.messaging.spi.Connector",
			"sourceMethod": "process(I)D",
			"required": true,
			"phase": 0,
			"name": "mp.messaging.incoming.prices.connector",
			"sourceType": "org.acme.kafka.PriceConverter",
			"source": true
		},	
		{
			"type": "java.lang.String",
			"required": true,
			"phase": 0,
			"name": "mp.messaging.incoming.${smallrye-kafka}.topic",
			"description": "The consumed / populated Kafka topic. If not set, the channel name is used",
			"sourceType": "io.smallrye.reactive.messaging.kafka.KafkaConnector"
		}
	],
	"hints": [
		{
			"values": [
				{
					"value": "smallrye-kafka",
					"sourceType": "io.smallrye.reactive.messaging.kafka.KafkaConnector"
				},
				{
					"value": "smallrye-amqp",
					"sourceType": "io.smallrye.reactive.messaging.amqp.AmqpConnector"
				}
			],
			"name": "${mp.messaging.connector.binary}"
		}
	]
}
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://github.com/eclipse/microprofile-reactive-messaging/blob/62c9ed5dffe01125941bb185f1433d6307b83c86/spec/src/main/asciidoc/architecture.asciidoc#configuration
 *
 */
public class MicroProfileReactiveMessagingItemMetadataProvider extends AbstractItemMetadataProvider {

	private static final String MP_MESSAGING_INCOMING = "mp.messaging.incoming.";
	private static final String MP_MESSAGING_OUTGOING = "mp.messaging.outgoing.";
	private static final String CONNECTOR_ATTRIBUTE = ".connector";

	// List of connector names (smallrye-kafka, smallrye-amqp, etc)
	private final ItemHint binaryConnectorHint;
	private ItemHint sourceConnectorHint;
	private ItemHint allConnectorHint;

	// List of mp reactive messaging properties (mp.messaging.[incoming|outgoing].*
	private final List<ItemMetadata> binaryProperties;
	private List<ItemMetadata> sourceProperties;

	private Map<String /* connector name */ , List<ChannelInfo>> connectorChannelsAssociations;

	private static class ChannelInfo {

		private final String name;

		private final boolean incoming;

		private final ItemMetadata metadata;

		public ChannelInfo(String name, boolean incoming, ItemMetadata metadata) {
			this.name = name;
			this.incoming = incoming;
			this.metadata = metadata;
		}

		public String getName() {
			return name;
		}

		public boolean isIncoming() {
			return incoming;
		}

		public ItemMetadata getMetadata() {
			return metadata;
		}
	}

	public MicroProfileReactiveMessagingItemMetadataProvider(ExtendedMicroProfileProjectInfo projectInfo) {
		super(projectInfo);
		this.binaryConnectorHint = projectInfo.getHint("${mp.messaging.connector.binary}");
		this.binaryProperties = collectConnectorProperties(projectInfo, true);

		// Create 'org.eclipse.microprofile.reactive.messaging.spi.Connector' hint which
		// merges connector names from
		// ${mp.messaging.connector.binary} and ${mp.messaging.connector.source}.
		allConnectorHint = new ItemHint();
		allConnectorHint.setName("org.eclipse.microprofile.reactive.messaging.spi.Connector");
		allConnectorHint.setValues(new ArrayList<>());
		projectInfo.getHints().add(allConnectorHint);

		updateFromSources();
	}

	/**
	 * Merge values from '${mp.messaging.connector.binary}' and
	 * '${mp.messaging.connector.source}' hints into the
	 * 'org.eclipse.microprofile.reactive.messaging.spi.Connector' hint.
	 */
	private void updateConnectorHint() {
		allConnectorHint.getValues().clear();
		if (binaryConnectorHint != null) {
			allConnectorHint.getValues().addAll(binaryConnectorHint.getValues());
		}
		if (sourceConnectorHint != null) {
			allConnectorHint.getValues().addAll(sourceConnectorHint.getValues());
		}
	}

	private void updateFromSources() {
		this.sourceConnectorHint = getProjectInfo().getHint("${mp.messaging.connector.source}");
		this.sourceProperties = collectConnectorProperties(getProjectInfo(), false);
		updateConnectorHint();
	}

	@Override
	protected void doUpdate(PropertiesModel document) {
		if (document == null) {
			// Java sources changes
			updateFromSources();
		}
		if (document != null) {
			Map<String, List<ChannelInfo>> connectorChannelsAssociations = getConnectorChannelsAssociations(document,
					getProjectInfo());
			if (this.connectorChannelsAssociations == null) {
				this.connectorChannelsAssociations = connectorChannelsAssociations;
			} else {
				if (Objects.deepEquals(this.connectorChannelsAssociations, connectorChannelsAssociations)) {
					return;
				}
				this.connectorChannelsAssociations = connectorChannelsAssociations;
			}
		}
		if (connectorChannelsAssociations == null) {
			return;
		}
		expandDynamicProperties(binaryProperties);
		expandDynamicProperties(sourceProperties);

	}

	/**
	 * Returns the associations between connector names and channel names and null
	 * if there are none associations.
	 * 
	 * @param document    the microprofile-config.properties file content.
	 * @param projectInfo the project information.
	 * @return the associations between connector names and channel names and null
	 *         if there are none associations.
	 */
	private static Map<String, List<ChannelInfo>> getConnectorChannelsAssociations(PropertiesModel document,
			MicroProfileProjectInfo projectInfo) {
		Map<String, List<ChannelInfo>> connectorChannelsAssociations = null;
		List<Node> properties = document.getChildren();
		// Loop for each property name/value declared in the
		// microprofile-config.properties.
		for (Node node : properties) {
			if (node.getNodeType() == NodeType.PROPERTY) {
				Property property = (Property) node;
				String propertyValue = property.getPropertyValue();
				if (StringUtils.hasText(propertyValue)) {
					// The property has a value
					String propertyName = property.getPropertyName();
					ChannelInfo channelInfo = getChannelInfo(propertyName, projectInfo);
					if (channelInfo != null
							&& MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo) != null) {
						// The property exists as metadata and it is a MP Reactive Messaging connector
						// declaration
						// - mp.messaging.incoming.*.connector = ...
						// - mp.messaging.outgoing.*.connector = ...
						if (connectorChannelsAssociations == null) {
							connectorChannelsAssociations = new HashMap<>();
						}
						// Ex : mp.messaging.incoming.prices.connector = smallrye-kafka
						// Store the association between connector name (smallrye-kafka) and channel
						// name (prices).
						String connectorName = propertyValue;
						List<ChannelInfo> channels = connectorChannelsAssociations.get(connectorName);
						if (channels == null) {
							channels = new ArrayList<>();
							connectorChannelsAssociations.put(connectorName, channels);
						}
						channels.add(channelInfo);
					}
				}
			}
		}
		return connectorChannelsAssociations;
	}

	/**
	 * Expand properties of connector bound with channels.
	 * 
	 * @param dynamicProperties the properties
	 */
	private void expandDynamicProperties(List<ItemMetadata> dynamicProperties) {
		if (dynamicProperties == null) {
			return;
		}
		// Loop for each dynamic connector property
		// ex: mp.messaging.incoming.${smallrye-kafka}.topic
		for (ItemMetadata property : dynamicProperties) {
			String propertyName = property.getName(); // ex: mp.messaging.incoming.${smallrye-kafka}.topic
			int start = propertyName.indexOf("${") + 2;
			int end = propertyName.indexOf("}"); // // smallrye-kafka
			String connectorName = propertyName.substring(start, end);
			// Check if there are channels which are linked to the current connector name.
			List<ChannelInfo> channels = connectorChannelsAssociations.get(connectorName);
			if (channels != null) {
				boolean incoming = propertyName.startsWith(MP_MESSAGING_INCOMING);
				// Loop for each bounded channels with the current connector name
				for (ChannelInfo info : channels) {
					if (info.isIncoming() == incoming) {
						String channelName = info.getName();
						// Generate connector attribute for the current channel
						ItemMetadata item = new ItemMetadata();
						item.setName(propertyName.replace("${" + connectorName + "}", channelName));
						item.setType(property.getType());
						item.setDescription(property.getDescription());
						item.setDefaultValue(property.getDefaultValue());
						ItemMetadata metadata = info.getMetadata();
						if (metadata != null) {
							item.setSourceType(metadata.getSourceType());
							// @Incoming, @Outgoing annotations are only available for method
							item.setSourceMethod(metadata.getSourceMethod());
						}
						getProperties().add(item);
					}
				}
			}
		}
	}

	/**
	 * Extract informations of channel name, incoming for a given MP messaging
	 * property and null otherwise.
	 * 
	 * @param propertyName the property name.
	 * @param projectInfo  the project information.
	 * @return informations of channel name, incoming for a given MP messaging
	 *         property and null otherwise.
	 */
	private static ChannelInfo getChannelInfo(String propertyName, MicroProfileProjectInfo projectInfo) {
		boolean incoming = true;
		int start = propertyName.indexOf(MP_MESSAGING_INCOMING);
		if (start == -1) {
			start = propertyName.indexOf(MP_MESSAGING_OUTGOING);
			incoming = false;
		}
		if (start == -1) {
			return null;
		}
		int end = propertyName.indexOf(CONNECTOR_ATTRIBUTE, start);
		if (end == -1) {
			return null;
		}
		start = incoming ? MP_MESSAGING_INCOMING.length() : MP_MESSAGING_OUTGOING.length();
		ItemMetadata metadata = MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo);
		return new ChannelInfo(propertyName.substring(start, end), incoming, metadata);
	}

	/**
	 * Returns list of dynamic MP messaging properties (attributes for connector
	 * name)
	 * 
	 * @param projectInfo the project information.
	 * @param binary      true if binary and false otherwise.
	 * @return list of dynamic MP messaging properties (attributes for connector
	 *         name)
	 */
	private static List<ItemMetadata> collectConnectorProperties(ExtendedMicroProfileProjectInfo projectInfo,
			boolean binary) {
		return projectInfo.getDynamicProperties().stream().filter(metadata -> {
			if (binary != metadata.isBinary()) {
				return false;
			}
			return isMPMessagingProperty(metadata.getName());
		}).collect(Collectors.toList());
	}

	/**
	 * Returns true if the given property name is a MP messaging property and false
	 * otherwise.
	 * 
	 * @param propertyName the property name.
	 * @return true if the given property name is a MP messaging property and false
	 *         otherwise.
	 */
	private static boolean isMPMessagingProperty(String propertyName) {
		return propertyName.startsWith(MP_MESSAGING_INCOMING) || propertyName.startsWith(MP_MESSAGING_OUTGOING);
	}

	/**
	 * Returns true if list of connector names is filled (ex: with smallrye-kafka)
	 * and false otherwise.
	 * 
	 * @return true if list of connector names is filled (ex: with smallrye-kafka)
	 *         and false otherwise.
	 */
	@Override
	public boolean isAvailable() {
		return allConnectorHint.getValues().isEmpty();
	}
}
