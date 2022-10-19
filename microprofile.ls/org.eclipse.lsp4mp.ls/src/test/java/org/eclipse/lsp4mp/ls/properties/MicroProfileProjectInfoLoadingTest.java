/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.ls.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfoParams;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.ls.MockMicroProfileLanguageClient;
import org.eclipse.lsp4mp.ls.MockMicroProfileLanguageServer;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Test with inlay hint, diagnostics which must be available after a load of
 * project.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileProjectInfoLoadingTest {

	private static final int LOADING_PROJECT_TIMEOUT = 1000;
	private final String json = "{\r\n" + //
			"    \"settings\": {\r\n" + //
			"        \"microprofile\": {\r\n" + //
			"            \"tools\": {\r\n" + //
			"                \"inlayHint\": {\r\n" + //
			"                    \"enabled\": true\r\n" + //
			"                },\r\n" + //
			"                \"validation\": {\r\n" + //
			"                    \"enabled\": \"true\"\r\n" + //
			"                }\r\n" + //
			"            }\r\n" + //
			"        }\r\n" + //
			"    }\r\n" + //
			"}";

	@Test
	public void reportDiagnosticsAfterLoading() throws InterruptedException, ExecutionException {
		MockMicroProfileLanguageServer server = createServer();
		InitializeParams params = createInitializeParams(json);
		server.initialize(params);

		String uri = "microprofile-config.properties";
		server.didOpen(uri, "server.url = http://${server.host}:${server.port}");
		Assert.assertEquals(0, server.getPublishDiagnostics().size());
		// Wait for 1,5 sec
		Thread.sleep(LOADING_PROJECT_TIMEOUT + 500);
		// After the load of the projects1 (which takes 1sec), the validation hint
		// should appear
		Assert.assertEquals(1, server.getPublishDiagnostics().size());
	}

	@Test
	public void reportInlayHintAfterLoading() throws InterruptedException, ExecutionException {
		MockMicroProfileLanguageServer server = createServer();
		InitializeParams params = createInitializeParams(json);
		server.initialize(params);

		String uri = "microprofile-config.properties";
		server.didOpen(uri, "server.url = http://${server.host}:${server.port}");
		List<InlayHint> result = server.inlayHint(uri);
		// After the load of the projects1 (which takes 1sec), the inlay hint should
		// appear
		Assert.assertEquals(1, result.size());
	}

	private InitializeParams createInitializeParams(String json2) {
		InitializeParams initializeParams = new InitializeParams();
		Object initializationOptions = new Gson().fromJson(json, JsonObject.class);
		initializeParams.setInitializationOptions(initializationOptions);
		ClientCapabilities capabilities = new ClientCapabilities();
		initializeParams.setCapabilities(capabilities);
		return initializeParams;
	}

	private MockMicroProfileLanguageServer createServer() {
		MockMicroProfileLanguageServer server = new MockMicroProfileLanguageServer();
		MockMicroProfileLanguageClient client = new MockMicroProfileLanguageClient(server) {

			@Override
			public CompletableFuture<MicroProfileProjectInfo> getProjectInfo(MicroProfileProjectInfoParams params) {
				return CompletableFuture.supplyAsync(() -> {
					try {
						Thread.sleep(LOADING_PROJECT_TIMEOUT);
					} catch (InterruptedException e) {

					}
					MicroProfileProjectInfo info = new MicroProfileProjectInfo();
					info.setProjectURI("project1");
					List<ItemMetadata> properties = new ArrayList<>();
					ItemMetadata host = new ItemMetadata();
					host.setName("server.host");
					host.setDefaultValue("localhost");
					properties.add(host);
					ItemMetadata port = new ItemMetadata();
					port.setName("server.port");
					port.setDefaultValue("8080");
					properties.add(port);
					info.setProperties(properties);
					return info;
				});
			}
		};
		server.setClient(client);
		return server;
	}
}
