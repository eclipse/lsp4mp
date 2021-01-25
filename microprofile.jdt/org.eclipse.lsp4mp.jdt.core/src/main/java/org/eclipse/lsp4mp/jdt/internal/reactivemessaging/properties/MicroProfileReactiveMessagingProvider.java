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
package org.eclipse.lsp4mp.jdt.internal.reactivemessaging.properties;

import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.isMatchAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceField;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceMethod;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.isBinary;
import static org.eclipse.lsp4mp.jdt.internal.reactivemessaging.MicroProfileReactiveMessagingConstants.CHANNEL_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.reactivemessaging.MicroProfileReactiveMessagingConstants.CONNECTOR_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.reactivemessaging.MicroProfileReactiveMessagingConstants.EMITTER_CLASS;
import static org.eclipse.lsp4mp.jdt.internal.reactivemessaging.MicroProfileReactiveMessagingConstants.CONNECTOR_ATTRIBUTES_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.reactivemessaging.MicroProfileReactiveMessagingConstants.CONNECTOR_ATTRIBUTE_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.reactivemessaging.MicroProfileReactiveMessagingConstants.INCOMING_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.reactivemessaging.MicroProfileReactiveMessagingConstants.OUTGOING_ANNOTATION;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ValueHint;
import org.eclipse.lsp4mp.jdt.core.AbstractAnnotationTypeReferencePropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.SearchContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Properties provider to collect MicroProfile properties from the MicroProfile
 * Reactive Messaging annotations.
 *
 * <ul>
 * <li>static incoming/outgoing properties for connector according the Java
 * annotation @Incoming/Outgoing (ex: mp.messaging.incoming.prices.connector).
 * </li>
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
public class MicroProfileReactiveMessagingProvider extends AbstractAnnotationTypeReferencePropertiesProvider {

	private static final String[] ANNOTATION_NAMES = { CONNECTOR_ANNOTATION, INCOMING_ANNOTATION, OUTGOING_ANNOTATION, CHANNEL_ANNOTATION };

	private static enum Direction {
		INCOMING, OUTGOING, INCOMING_AND_OUTGOING;
	}

	private static enum MessageType {
		INCOMING, OUTGOING, CONNECTOR;
	}

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation mprmAnnotation, String annotationName,
			SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		switch (annotationName) {
		case CONNECTOR_ANNOTATION:
			// @Connector(KafkaConnector.CONNECTOR_NAME)
			// @ConnectorAttribute(name = "bootstrap.servers", alias =
			// "kafka.bootstrap.servers", type = "string", defaultValue = "localhost:9092",
			// direction = Direction.INCOMING_AND_OUTGOING, description = "A comma-separated
			// list of host:port to use for establishing the initial connection to the Kafka
			// cluster.")
			// ...

			// public class KafkaConnector implements IncomingConnectorFactory,
			// OutgoingConnectorFactory {

			// public static final String CONNECTOR_NAME = "smallrye-kafka";

			processConnector(javaElement, mprmAnnotation, context);
			break;
		case INCOMING_ANNOTATION:
			// public class PriceConverter {
			// @Incoming("prices")
			// public double process(int priceInUsd) {
			processIncomingChannel(javaElement, mprmAnnotation, context);
			break;
		case CHANNEL_ANNOTATION:
			// @Inject
			// @Channel("prices")
			// Emitter<double> pricesEmitter;
			if (isAnnotatingEmitterObject(javaElement)) {
				processOutgoingChannel(javaElement, mprmAnnotation, context);
			}
			break;
		case OUTGOING_ANNOTATION:
			// public class PriceConverter {
			// @Outgoing("my-data-stream")
			// public double process(int priceInUsd) {
			processOutgoingChannel(javaElement, mprmAnnotation, context);
			break;
		default:
			break;
		}
	}

	private static boolean isAnnotatingEmitterObject(IJavaElement element) {
		if (element.getElementType() != IJavaElement.FIELD) {
			return false;
		}
		IField field = (IField) element;
		String typeSignature = JDTTypeUtils.getResolvedTypeName(field);
		if (typeSignature == null) {
			return false;
		}
		return typeSignature.startsWith(EMITTER_CLASS);
	}

	/**
	 * Generate static property for incoming connector (ex :
	 * mp.messaging.incoming.prices.connector).
	 *
	 * @param javaElement        the Java element.
	 * @param incomingAnnotation the incoming annotation.
	 * @param context            the search context.
	 * @throws JavaModelException
	 */
	private void processIncomingChannel(IJavaElement javaElement, IAnnotation incomingAnnotation, SearchContext context)
			throws JavaModelException {
		processChannelConnector(javaElement, incomingAnnotation, MessageType.INCOMING, context);
	}

	/**
	 * Generate static property for outgoing connector (ex :
	 * mp.messaging.outgoing.generated-price.connector).
	 *
	 * @param javaElement        the Java element.
	 * @param outgoingAnnotation the outgoing annotation.
	 * @param context            the search context.
	 * @throws JavaModelException
	 */
	private void processOutgoingChannel(IJavaElement javaElement, IAnnotation outgoingAnnotation, SearchContext context)
			throws JavaModelException {
		processChannelConnector(javaElement, outgoingAnnotation, MessageType.OUTGOING, context);
	}

	/**
	 * Generate static property for incoming/outgoing connector
	 *
	 * @param javaElement                  the Java element.
	 * @param incomingOrOutgoingAnnotation the incoming/outgoing annotation.
	 * @param messageType                  the message type to generate (incoming,
	 *                                     outgoing).
	 * @param context                      the search context.
	 * @throws JavaModelException
	 */
	private void processChannelConnector(IJavaElement javaElement, IAnnotation incomingOrOutgoingAnnotation,
			MessageType messageType, SearchContext context) throws JavaModelException {
		// Extract channel name from
		// - @Incoming("channel-name") or
		// - @Outgoing("channel-name") annotation
		String channelName = getAnnotationMemberValue(incomingOrOutgoingAnnotation, "value");
		if (StringUtils.isBlank(channelName)) {
			// channel name must not be blank, see
			// https://github.com/eclipse/microprofile-reactive-messaging/blob/62c9ed5dffe01125941bb185f1433d6307b83c86/api/src/main/java/org/eclipse/microprofile/reactive/messaging/Incoming.java#L95
			return;
		}
		String sourceType = getSourceType(javaElement);
		String sourceMethod = null;
		String sourceField = null;
		if (javaElement.getElementType() == IJavaElement.METHOD) {
			IMethod method = (IMethod) javaElement;
			sourceMethod = getSourceMethod(method);
		} else if (javaElement.getElementType() == IJavaElement.FIELD) {
			IField field = (IField) javaElement;
			sourceField = getSourceField(field);
		}
		boolean binary = isBinary(javaElement);
		String description = null;
		String type = "org.eclipse.microprofile.reactive.messaging.spi.Connector";
		addMpMessagingItem(channelName, false, "connector", messageType, sourceType, sourceField, sourceMethod, binary, type,
				description, null, context);
	}

	/**
	 * Generate dynamic property connector (attributes connector).
	 *
	 * @param javaElement         the Java element.
	 * @param connectorAnnotation the Connector annotation.
	 * @param context             the search context.
	 * @throws JavaModelException
	 */
	private void processConnector(IJavaElement javaElement, IAnnotation connectorAnnotation, SearchContext context)
			throws JavaModelException {

		// 1) Collect connector names into hints ${mp.messaging.connector.binary} and
		// ${mp.messaging.connector.source}
		String connectorName = getAnnotationMemberValue(connectorAnnotation, "value");
		String connectorHint = getHint("mp.messaging.connector.", javaElement);
		String description = null;
		String sourceType = getSourceType(javaElement);
		fillValueHint(connectorHint, connectorName, description, sourceType, context);

		// 2) Generate property per @ConnectorAttribute which provides attribute and
		// direction informations for the current connector
		// - mp.messaging.[attribute=incoming|outgoing].${connector-name}.[attribute]
		boolean binary = isBinary(javaElement);
		IAnnotation[] annotations = ((IAnnotatable) javaElement).getAnnotations();
		for (IAnnotation connectorAttributeAnnotation : annotations) {
			if (isMatchAnnotation(connectorAttributeAnnotation, CONNECTOR_ATTRIBUTE_ANNOTATION)) {
				processConnectorAttribute(connectorName, connectorAttributeAnnotation, sourceType, binary, context);
			} else if (isMatchAnnotation(connectorAttributeAnnotation, CONNECTOR_ATTRIBUTES_ANNOTATION)) {
				for (IMemberValuePair pair : connectorAttributeAnnotation.getMemberValuePairs()) {
					if (pair.getValue() instanceof Object[]) {
						Object[] connectorAttributeAnnotations = (Object[]) pair.getValue();
						for (Object annotation : connectorAttributeAnnotations) {
							if (annotation instanceof IAnnotation) {
								processConnectorAttribute(connectorName, (IAnnotation) annotation, sourceType, binary,
										context);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Generate the dynamic connector attribute property.
	 *
	 * @param connectorName       the connector name.
	 * @param connectorAnnotation the connector annotation.
	 * @param sourceType          the source type.
	 * @param binary              true if binary.
	 * @param context             the search context.
	 * @throws JavaModelException
	 */
	private void processConnectorAttribute(String connectorName, IAnnotation connectorAnnotation, String sourceType,
			boolean binary, SearchContext context) throws JavaModelException {
		String attributeName = getAnnotationMemberValue(connectorAnnotation, "name");
		String type = getType(getAnnotationMemberValue(connectorAnnotation, "type"));
		String description = getAnnotationMemberValue(connectorAnnotation, "description");
		String defaultValue = getAnnotationMemberValue(connectorAnnotation, "defaultValue");
		if (StringUtils.isEmpty(defaultValue)) {
			defaultValue = null;
		}
		Direction direction = getDirection(getAnnotationMemberValue(connectorAnnotation, "direction"));

		switch (direction) {
		case INCOMING:
			// Generate mp.messaging.incoming.${connector-name}.[attribute]
			// ex : mp.messaging.incoming.${smallrye-kafka}.topic
			addMpMessagingItem(connectorName, true, attributeName, MessageType.INCOMING, sourceType, null, null, binary, type,
					description, defaultValue, context);
			break;
		case OUTGOING:
			// Generate mp.messaging.outgoing.${connector-name}.[attribute]
			addMpMessagingItem(connectorName, true, attributeName, MessageType.OUTGOING, sourceType, null, null, binary, type,
					description, defaultValue, context);
			break;
		case INCOMING_AND_OUTGOING:
			// Generate mp.messaging.incoming.${connector-name}.[attribute]
			addMpMessagingItem(connectorName, true, attributeName, MessageType.INCOMING, sourceType, null, null, binary, type,
					description, defaultValue, context);
			// Generate mp.messaging.outgoing.${connector-name}.[attribute]
			addMpMessagingItem(connectorName, true, attributeName, MessageType.OUTGOING, sourceType, null, null, binary, type,
					description, defaultValue, context);
			break;
		}
		// Generate mp.messaging.connector.[connector-name].[attribute]
		addMpMessagingItem(connectorName, false, attributeName, MessageType.CONNECTOR, sourceType, null, null, binary, type,
				description, defaultValue, context);
	}

	private void addMpMessagingItem(String connectorOrChannelName, boolean dynamic, String attributeName,
			MessageType messageType, String sourceType, String sourceField, String sourceMethod, boolean binary, String type,
			String description, String defaultValue, SearchContext context) {
		String propertyName = getMPMessagingName(messageType, dynamic, connectorOrChannelName, attributeName);
		super.addItemMetadata(context.getCollector(), propertyName, type, description, sourceType, sourceField, sourceMethod,
				defaultValue, null, binary);
	}

	/**
	 * Returns the direction according the given enumeration value.
	 *
	 * @param connectorAttributeType
	 * @return the direction according the given enumeration value.
	 */
	private static Direction getDirection(String connectorAttributeType) {
		if (connectorAttributeType != null) {
			if (connectorAttributeType.endsWith("INCOMING_AND_OUTGOING")) {
				return Direction.INCOMING_AND_OUTGOING;
			}
			if (connectorAttributeType.endsWith("INCOMING")) {
				return Direction.INCOMING;
			}
			if (connectorAttributeType.endsWith("OUTGOING")) {
				return Direction.OUTGOING;
			}
		}
		return Direction.INCOMING_AND_OUTGOING;
	}

	/**
	 * Returns the Java type from the given connector attribute type (coming from
	 * the @ConnectorAttribute/type).
	 *
	 * @param connectorAttributeType
	 * @return the Java type from the given connector attribute type (coming from
	 *         the @ConnectorAttribute/type).
	 */
	private String getType(String connectorAttributeType) {
		if (StringUtils.isEmpty(connectorAttributeType)) {
			return null;
		}
		switch (connectorAttributeType) {
		case "string":
			return "java.lang.String";
		default:
			return connectorAttributeType;
		}
	}

	private static String getMPMessagingName(MessageType messageType, boolean dynamic, String connectorOrChannelName,
			String attributeName) {
		StringBuilder propertyName = new StringBuilder("mp.messaging");
		propertyName.append('.');
		propertyName.append(messageType.name().toLowerCase());
		propertyName.append('.');
		if (dynamic) {
			propertyName.append("${");
		}
		propertyName.append(connectorOrChannelName);
		if (dynamic) {
			propertyName.append("}");
		}
		propertyName.append('.');
		propertyName.append(attributeName);
		return propertyName.toString();
	}

	private static void fillValueHint(String hint, String value, String description, String sourceType,
			SearchContext context) {
		if (hint == null || value == null) {
			return;
		}
		ItemHint itemHint = context.getCollector().getItemHint(hint);
		ValueHint valueHint = new ValueHint();
		valueHint.setValue(value);
		valueHint.setDescription(description);
		valueHint.setSourceType(sourceType);
		itemHint.getValues().add(valueHint);
	}

	private static String getHint(String baseKey, IJavaElement javaElement) {
		StringBuilder hint = new StringBuilder("${").append(baseKey);
		if (javaElement != null) {
			hint.append(isBinary(javaElement) ? "binary" : "source");
		}
		return hint.append("}").toString();
	}

}
