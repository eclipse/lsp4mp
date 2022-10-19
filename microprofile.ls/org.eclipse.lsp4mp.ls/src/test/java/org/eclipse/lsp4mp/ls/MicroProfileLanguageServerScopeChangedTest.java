/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls;

import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.assertCompletions;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.c;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.r;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ValueHint;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test with change usecase of classpath and java sources changed.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileLanguageServerScopeChangedTest {

	private static final String PROJECT1 = "project1";
	private static final String PROJECT1_APPLICATION_PROPERTIES = PROJECT1 + "/application.properties";

	private static final ItemMetadata property1FromJar;
	private static final ItemMetadata property2FromJar;
	private static final ItemMetadata property1FromSources;
	private static final ItemMetadata property2FromSources;
	private static final ItemMetadata dynamicProperty1FromSources;

	private static final ItemMetadata dynamicProperty2FromSources;
	private static final ItemHint itemHintFromSources;

	static {
		property1FromJar = new ItemMetadata();
		property1FromJar.setName("quarkus.application.name");

		property2FromJar = new ItemMetadata();
		property2FromJar.setName("quarkus.application.version");

		property1FromSources = new ItemMetadata();
		property1FromSources.setName("greeting.message");
		property1FromSources.setSource(Boolean.TRUE);

		property2FromSources = new ItemMetadata();
		property2FromSources.setName("greeting.suffix");
		property2FromSources.setSource(Boolean.TRUE);

		dynamicProperty1FromSources = new ItemMetadata();
		dynamicProperty1FromSources.setName("${mp.register.rest.client.class}/mp-rest/url");
		dynamicProperty1FromSources.setSource(Boolean.TRUE);

		dynamicProperty2FromSources = new ItemMetadata();
		dynamicProperty2FromSources.setName("${mp.register.rest.client.class}/mp-rest/scope");
		dynamicProperty2FromSources.setSource(Boolean.TRUE);

		itemHintFromSources = new ItemHint();
		itemHintFromSources.setName("${mp.register.rest.client.class}");
		itemHintFromSources.setValues(new ArrayList<>());
		ValueHint value = new ValueHint();
		value.setValue("org.acme.restclient.CountriesService");
		itemHintFromSources.getValues().add(value);
		value = new ValueHint();
		value.setValue("configKey");
		itemHintFromSources.getValues().add(value);
	}

	@Test
	public void classpathChanged() throws InterruptedException, ExecutionException {
		MockMicroProfileLanguageServer server = createServer();
		MockMicroProfileLanguageClient client = (MockMicroProfileLanguageClient) server.getLanguageClient();
		// Initialize properties
		client.changedClasspath(PROJECT1, property1FromJar, property2FromJar, property1FromSources);

		server.didOpen(PROJECT1_APPLICATION_PROPERTIES);
		CompletionList list = server.completion(PROJECT1_APPLICATION_PROPERTIES);
		assertCompletions(list, 3, c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)), //
				c("quarkus.application.version", "quarkus.application.version=", r(0, 0, 0)), //
				c("greeting.message", "greeting.message=", r(0, 0, 0)));

		// Emulate change of classpath (Jar and Java sources)
		client.changedClasspath(PROJECT1, property1FromJar, property1FromSources);
		list = server.completion(PROJECT1_APPLICATION_PROPERTIES);
		assertCompletions(list, 2, c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)), //
				c("greeting.message", "greeting.message=", r(0, 0, 0)));

		// Emulate change of Java sources (add)
		client.changedJavaSources(PROJECT1, property2FromSources);
		list = server.completion(PROJECT1_APPLICATION_PROPERTIES);
		assertCompletions(list, 2, c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)), //
				c("greeting.suffix", "greeting.suffix=", r(0, 0, 0)));

		// Emulate change of Java sources with dynamic properties
		client.changedJavaSources(PROJECT1, dynamicProperty1FromSources, dynamicProperty2FromSources,
				itemHintFromSources);
		list = server.completion(PROJECT1_APPLICATION_PROPERTIES);
		assertCompletions(list, 1 /* (from JAR ) */ + 4 /* from sources */,
				c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)), //
				c("org.acme.restclient.CountriesService/mp-rest/url",
						"org.acme.restclient.CountriesService/mp-rest/url=", r(0, 0, 0)),
				c("org.acme.restclient.CountriesService/mp-rest/scope",
						"org.acme.restclient.CountriesService/mp-rest/scope=", r(0, 0, 0)),
				c("configKey/mp-rest/url", "configKey/mp-rest/url=", r(0, 0, 0)),
				c("configKey/mp-rest/scope", "configKey/mp-rest/scope=", r(0, 0, 0)));
	}

	@Test
	public void javaSourcesChangedInThreadContext() throws InterruptedException, ExecutionException {
		MockMicroProfileLanguageServer server = createServer();
		MockMicroProfileLanguageClient client = (MockMicroProfileLanguageClient) server.getLanguageClient();
		// Initialize properties
		client.changedClasspath(PROJECT1, property1FromJar, property2FromJar, property1FromSources);

		server.didOpen(PROJECT1_APPLICATION_PROPERTIES);
		CompletionList list = server.completion(PROJECT1_APPLICATION_PROPERTIES);
		assertCompletions(list, 3, c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)), //
				c("quarkus.application.version", "quarkus.application.version=", r(0, 0, 0)), //
				c("greeting.message", "greeting.message=", r(0, 0, 0)));

		// Emulate change of classpath (Jar and Java sources)
		client.changedClasspath(PROJECT1, property1FromJar, property1FromSources);
		list = server.completion(PROJECT1_APPLICATION_PROPERTIES);
		assertCompletions(list, 2, c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)), //
				c("greeting.message", "greeting.message=", r(0, 0, 0)));

		// create a lot of thread which change java sources (update properties)
		// and execute completion (to recompute properties)
		List<Thread> threads = new ArrayList<>();
		List<Integer> count = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			Thread t1 = createChangedJavaSourcesThread(server, client);
			threads.add(t1);
			Thread t2 = createCompletionThread(server, client, count);
			threads.add(t2);
			t1.start();
			t2.start();
		}

		for (Thread thread : threads) {
			thread.join();
		}
		Integer max = count.stream().max(Math::max).get();
		Assert.assertTrue(max <= 2);
	}

	private Thread createCompletionThread(MockMicroProfileLanguageServer server, MockMicroProfileLanguageClient client,
			List<Integer> count) {
		return new Thread(() -> {
			try {
				CompletionList list = server.completion(PROJECT1_APPLICATION_PROPERTIES);
				synchronized (count) {
					count.add(list.getItems().size());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private Thread createChangedJavaSourcesThread(MicroProfileLanguageServer server,
			MockMicroProfileLanguageClient client) {
		return new Thread(() -> {
			try {
				// Emulate change of Java sources (add)
				client.changedJavaSources(PROJECT1, property2FromSources);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private static MockMicroProfileLanguageServer createServer() {
		return new MockMicroProfileLanguageServer();
	}

}
