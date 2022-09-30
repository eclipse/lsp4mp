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
package org.eclipse.lsp4mp.jdt.core.reactivemessaging.properties;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertHints;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertHintsDuplicate;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertPropertiesDuplicate;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.h;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.p;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.vh;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.d;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.reactivemessaging.MicroProfileReactiveMessagingConstants;
import org.eclipse.lsp4mp.jdt.internal.reactivemessaging.java.MicroProfileReactiveMessagingErrorCode;
import org.junit.Test;

/**
 * Test collection of MicroProfile properties for MicroProfile Reactive
 * Messaging annotations
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfileReactiveMessagingTest extends BasePropertiesManagerTest {

	@Test
	public void microprofileReactiveMessagingPropertiesTest() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MicroProfileMavenProjectName.microprofile_reactive_messaging, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,

				// mp.messaging.incoming.
				p(null, "mp.messaging.incoming.prices.connector",
						"org.eclipse.microprofile.reactive.messaging.spi.Connector", null, false,
						"org.acme.kafka.PriceConverter", null, "process(I)D", 0, null),

				// mp.messaging.outgoing
				p(null, "mp.messaging.outgoing.my-data-stream.connector",
						"org.eclipse.microprofile.reactive.messaging.spi.Connector", null, false,
						"org.acme.kafka.PriceConverter", null, "process(I)D", 0, null),

				// outgoing generated from Emitter
				p(null, "mp.messaging.outgoing.price-create.connector",
						"org.eclipse.microprofile.reactive.messaging.spi.Connector", null, false,
						"org.acme.kafka.PriceResource", "priceEmitter", null, 0, null),

				// mp.messaging.incoming.${connector-name}
				p(null, "mp.messaging.incoming.${smallrye-kafka}.topic", "java.lang.String",
						"The consumed / populated Kafka topic. If not set, the channel name is used", true,
						"io.smallrye.reactive.messaging.kafka.KafkaConnector", null, null, 0, null),

				// mp.messaging.outgoing.${connector-name}
				p(null, "mp.messaging.outgoing.${smallrye-kafka}.topic", "java.lang.String",
						"The consumed / populated Kafka topic. If not set, the channel name is used", true,
						"io.smallrye.reactive.messaging.kafka.KafkaConnector", null, null, 0, null),

				// mp.messaging.incoming.${connector-name}
				p(null, "mp.messaging.incoming.${smallrye-kafka}.bootstrap.servers", "java.lang.String",
						"A comma-separated list of host:port to use for establishing the initial connection to the Kafka cluster.",
						true, "io.smallrye.reactive.messaging.kafka.KafkaConnector", null, null, 0, "localhost:9092") //
		);

		assertPropertiesDuplicate(infoFromClasspath);

		assertHints(infoFromClasspath, h("${mp.messaging.connector.binary}", null, true, null, //
				vh("smallrye-kafka", null, "io.smallrye.reactive.messaging.kafka.KafkaConnector")) //
		);

		assertHintsDuplicate(infoFromClasspath);
	}

    @Test
    public void blankAnnotation() throws Exception {
        IJavaProject javaProject = loadMavenProject(
                MicroProfileMavenProjectName.microprofile_reactive_messaging);
        IJDTUtils utils = JDT_UTILS;

        MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
        IFile javaFile = javaProject.getProject().getFile(
                new Path("src/main/java/org/acme/kafka/PriceConverter.java"));
        diagnosticsParams.setUris(Arrays
                .asList(javaFile.getLocation().toFile().toURI().toString()));
        diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

        Diagnostic d1 = d(24, 14, 16,
                "The name of the consumed channel must not be blank.",
                DiagnosticSeverity.Error,
                MicroProfileReactiveMessagingConstants.MICRO_PROFILE_REACTIVE_MESSAGING_DIAGNOSTIC_SOURCE,
                MicroProfileReactiveMessagingErrorCode.BLANK_CHANNEL_NAME);
        Diagnostic d2 = d(25, 20, 22,
                "The name of the consumed channel must not be blank.",
                DiagnosticSeverity.Error,
                MicroProfileReactiveMessagingConstants.MICRO_PROFILE_REACTIVE_MESSAGING_DIAGNOSTIC_SOURCE,
                MicroProfileReactiveMessagingErrorCode.BLANK_CHANNEL_NAME);
        assertJavaDiagnostics(diagnosticsParams, utils, d1, d2);
    }

}
