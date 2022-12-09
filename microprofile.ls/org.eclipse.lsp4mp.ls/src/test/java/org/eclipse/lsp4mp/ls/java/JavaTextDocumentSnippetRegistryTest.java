/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4mp.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4mp.ls.commons.snippets.ISnippetContext;
import org.eclipse.lsp4mp.ls.commons.snippets.Snippet;
import org.eclipse.lsp4mp.ls.commons.snippets.SnippetRegistry;
import org.eclipse.lsp4mp.ls.java.JavaTextDocuments.JavaTextDocument;
import org.eclipse.lsp4mp.snippets.SnippetContextForJava;
import org.junit.Test;

/**
 * Test for Java snippet registry.
 *
 * @author Angelo ZERR
 *
 */
public class JavaTextDocumentSnippetRegistryTest {

	private static JavaTextDocumentSnippetRegistry registry = new JavaTextDocumentSnippetRegistry();

	private static final ProjectLabelInfoEntry JAVAX_PROJECT_INFO = new ProjectLabelInfoEntry("", "", Collections.emptyList());
	private static final ProjectLabelInfoEntry JAKARTA_PROJECT_INFO = new ProjectLabelInfoEntry("", "", Collections.singletonList("jakarta.ws.rs.GET"));

	@Test
	public void haveJavaSnippets() {
		assertFalse("Tests has MicroProfile Java snippets", registry.getSnippets().isEmpty());
	}

	@Test
	public void restSnippets() {
		Optional<Snippet> restClassSnippet = findByPrefix("rest_class", registry);
		assertTrue("Tests has rest_class Java snippet", restClassSnippet.isPresent());

		Optional<Snippet> restGetSnippet = findByPrefix("rest_get", registry);
		assertTrue("Tests has rest_get Java snippet", restGetSnippet.isPresent());

		ISnippetContext<?> context = restClassSnippet.get().getContext();
		assertNotNull("rest_class snippet has context", context);
		assertTrue("rest_class snippet context is Java context", context instanceof SnippetContextForJava);

		ProjectLabelInfoEntry projectInfo = new ProjectLabelInfoEntry("", "", new ArrayList<>());
		boolean match = ((SnippetContextForJava) context).isMatch(projectInfo);
		assertFalse("Project has no javax.ws.rs.GET or jakarta.ws.rs.GET type", match);

		ProjectLabelInfoEntry projectInfo2 = new ProjectLabelInfoEntry("", "", Arrays.asList("javax.ws.rs.GET"));
		boolean match2 = ((SnippetContextForJava) context).isMatch(projectInfo2);
		assertTrue("Project has javax.ws.rs.GET type", match2);

		ProjectLabelInfoEntry projectInfo3 = new ProjectLabelInfoEntry("", "", Arrays.asList("jakarta.ws.rs.GET"));
		boolean match3 = ((SnippetContextForJava) context).isMatch(projectInfo3);
		assertTrue("Project has jakarta.ws.rs.GET type", match3);

		ProjectLabelInfoEntry projectInfo4 = new ProjectLabelInfoEntry("", "", Arrays.asList("javax.ws.rs.GET", "jakarta.ws.rs.GET"));
		boolean match4= ((SnippetContextForJava) context).isMatch(projectInfo4);
		assertTrue("Project has javax.ws.rs.GET and jakarta.ws.rs.GET types", match4);
	}

	@Test
	public void mpMetricsSnippets() {
		Optional<Snippet> metricSnippet = findByPrefix("@Metric", registry);
		assertTrue("Tests has @Metric Java snippets", metricSnippet.isPresent());

		ISnippetContext<?> context = metricSnippet.get().getContext();
		assertNotNull("@Metric snippet has context", context);
		assertTrue("@Metric snippet context is Java context", context instanceof SnippetContextForJava);

		ProjectLabelInfoEntry projectInfo = new ProjectLabelInfoEntry("", "", new ArrayList<>());
		boolean match = ((SnippetContextForJava) context).isMatch(projectInfo);
		assertFalse("Project has no org.eclipse.microprofile.metrics.annotation.Metric type", match);

		ProjectLabelInfoEntry projectInfo2 = new ProjectLabelInfoEntry("", "",
				Arrays.asList("org.eclipse.microprofile.metrics.annotation.Metric"));
		boolean match2 = ((SnippetContextForJava) context).isMatch(projectInfo2);
		assertTrue("Project has org.eclipse.microprofile.metrics.annotation.Metric type", match2);
	}

	@Test
	public void mpOpenAPISnippets() {
		Optional<Snippet> apiResponseSnippet = findByPrefix("@APIResponse", registry);
		assertTrue("Tests has @APIResponse Java snippets", apiResponseSnippet.isPresent());

		ISnippetContext<?> context = apiResponseSnippet.get().getContext();
		assertNotNull("@APIResponse snippet has context", context);
		assertTrue("@APIResponse snippet context is Java context", context instanceof SnippetContextForJava);

		ProjectLabelInfoEntry projectInfo = new ProjectLabelInfoEntry("", "", new ArrayList<>());
		boolean match = ((SnippetContextForJava) context).isMatch(projectInfo);
		assertFalse("Project has no org.eclipse.microprofile.openapi.annotations.responses.APIResponse type", match);

		ProjectLabelInfoEntry projectInfo2 = new ProjectLabelInfoEntry("", "",
				Arrays.asList("org.eclipse.microprofile.openapi.annotations.responses.APIResponse"));
		boolean match2 = ((SnippetContextForJava) context).isMatch(projectInfo2);
		assertTrue("Project has org.eclipse.microprofile.openapi.annotations.responses.APIResponse type", match2);
	}

	@Test
	public void mpFaultToleranceSnippets() {
		Optional<Snippet> fallbackSnippet = findByPrefix("@Fallback", registry);
		assertTrue("Tests has @Fallback Java snippets", fallbackSnippet.isPresent());

		ISnippetContext<?> context = fallbackSnippet.get().getContext();
		assertNotNull("@Fallback snippet has context", context);
		assertTrue("@Fallback snippet context is Java context", context instanceof SnippetContextForJava);

		ProjectLabelInfoEntry projectInfo = new ProjectLabelInfoEntry("", "", new ArrayList<>());
		boolean match = ((SnippetContextForJava) context).isMatch(projectInfo);
		assertFalse("Project has no org.eclipse.microprofile.faulttolerance.Fallback type", match);

		ProjectLabelInfoEntry projectInfo2 = new ProjectLabelInfoEntry("", "",
				Arrays.asList("org.eclipse.microprofile.faulttolerance.Fallback"));
		boolean match2 = ((SnippetContextForJava) context).isMatch(projectInfo2);
		assertTrue("Project has org.eclipse.microprofile.faulttolerance.Fallback type", match2);
	}

	@Test
	public void mpHealthSnippets() {
		Optional<Snippet> readinessSnippet = findByPrefix("mpreadiness", registry);
		assertTrue("Tests has mpreadiness Java snippets", readinessSnippet.isPresent());

		ISnippetContext<?> readinessContext = readinessSnippet.get().getContext();
		assertNotNull("mpreadiness snippet has context", readinessContext);
		assertTrue("mpreadiness snippet context is Java context", readinessContext instanceof SnippetContextForJava);

		ProjectLabelInfoEntry readinessProjectInfo = new ProjectLabelInfoEntry("", "", new ArrayList<>());
		boolean match = ((SnippetContextForJava) readinessContext).isMatch(readinessProjectInfo);
		assertFalse("Project has no org.eclipse.microprofile.health.Readiness type", match);

		ProjectLabelInfoEntry readinessProjectInfo2 = new ProjectLabelInfoEntry("", "",
				Arrays.asList("org.eclipse.microprofile.health.Readiness"));
		boolean match2 = ((SnippetContextForJava) readinessContext).isMatch(readinessProjectInfo2);
		assertTrue("Project has org.eclipse.microprofile.health.Readiness type", match2);

		Optional<Snippet> livenessSnippet = findByPrefix("mpliveness", registry);
		assertTrue("Tests has mpliveness Java snippets", livenessSnippet.isPresent());

		ISnippetContext<?> livenessContext = livenessSnippet.get().getContext();
		assertNotNull("mpliveness snippet has context", livenessContext);
	}

	@Test
	public void mpRestClientSnippets() {
		Optional<Snippet> newRestClientSnippet = findByPrefix("mpnrc", registry);
		assertTrue("Tests has new MicroProfile rest client Java snippets", newRestClientSnippet.isPresent());

		ISnippetContext<?> context = newRestClientSnippet.get().getContext();
		assertNotNull("mpnrc snippet has context", context);
		assertTrue("mpnrc snippet context is Java context", context instanceof SnippetContextForJava);

		ProjectLabelInfoEntry projectInfo = new ProjectLabelInfoEntry("", "", new ArrayList<>());
		boolean match = ((SnippetContextForJava) context).isMatch(projectInfo);
		assertFalse("Project has no org.eclipse.microprofile.rest.client.inject.RegisterRestClient type", match);

		ProjectLabelInfoEntry projectInfo2 = new ProjectLabelInfoEntry("", "",
				Arrays.asList("org.eclipse.microprofile.rest.client.inject.RegisterRestClient"));
		boolean match2 = ((SnippetContextForJava) context).isMatch(projectInfo2);
		assertTrue("Project has org.eclipse.microprofile.rest.client.inject.RegisterRestClient type", match2);

		Optional<Snippet> injectRestClientSnippet = findByPrefix("mpirc", registry);
		assertTrue("Tests has inject MicroProfile rest client Java snippets", injectRestClientSnippet.isPresent());

		ISnippetContext<?> context2 = injectRestClientSnippet.get().getContext();
		assertNotNull("mpirc snippet has context", context2);
	}

	@Test
	public void completionWithPackagenameJavax() {
		// Create the snippet
		Snippet snippet = new Snippet();
		snippet.setPrefixes(Arrays.asList("test"));
		snippet.setBody(new ArrayList<>(Arrays.asList("package ${packagename};", //
				"import ${ee-namespace}.ws.rs.GET;", //
				"import ${ee-namespace}.ws.rs.Path;")));
		JavaTextDocumentSnippetRegistry registry = new JavaTextDocumentSnippetRegistry(false);
		registry.registerSnippet(snippet);

		// Create a Java text document with com.foo package.
		JavaTextDocument document = new JavaTextDocuments(null, null)
				.createDocument(new TextDocumentItem("test.java", "java", 0, "abcd"));
		document.setPackageName("com.foo");

		List<CompletionItem> items = registry.getCompletionItems(document, 0, true, true, (context, model) -> true, JAVAX_PROJECT_INFO);
		assertEquals("Completion size", 1, items.size());
		CompletionItem item = items.get(0);
		assertNotNull("Completion text edit", item.getTextEdit());
		assertEquals("package com.foo;" + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"import javax.ws.rs.GET;" + //
				System.lineSeparator() + //
				"import javax.ws.rs.Path;" //
				, item.getTextEdit().getLeft().getNewText());
	}

	@Test
	public void completionWithPackagenameJakarta() {
		// Create the snippet
		Snippet snippet = new Snippet();
		snippet.setPrefixes(Arrays.asList("test"));
		snippet.setBody(new ArrayList<>(Arrays.asList("package ${packagename};", //
				"import ${ee-namespace}.ws.rs.GET;", //
				"import ${ee-namespace}.ws.rs.Path;")));
		JavaTextDocumentSnippetRegistry registry = new JavaTextDocumentSnippetRegistry(false);
		registry.registerSnippet(snippet);

		// Create a Java text document with com.foo package.
		JavaTextDocument document = new JavaTextDocuments(null, null)
				.createDocument(new TextDocumentItem("test.java", "java", 0, "abcd"));
		document.setPackageName("com.foo");

		List<CompletionItem> items = registry.getCompletionItems(document, 0, true, true, (context, model) -> true, JAKARTA_PROJECT_INFO);
		assertEquals("Completion size", 1, items.size());
		CompletionItem item = items.get(0);
		assertNotNull("Completion text edit", item.getTextEdit());
		assertEquals("package com.foo;" + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"import jakarta.ws.rs.GET;" + //
				System.lineSeparator() + //
				"import jakarta.ws.rs.Path;" //
				, item.getTextEdit().getLeft().getNewText());
	}

	@Test
	public void completionWithoutPackagename() {
		// Create the snippet
		Snippet snippet = new Snippet();
		snippet.setPrefixes(Arrays.asList("test"));
		snippet.setBody(new ArrayList<>(Arrays.asList("package ${packagename};", //
				"import javax.ws.rs.GET;", //
				"import javax.ws.rs.Path;")));
		JavaTextDocumentSnippetRegistry registry = new JavaTextDocumentSnippetRegistry(false);
		registry.registerSnippet(snippet);

		// Create a Java text document with com.foo package.
		JavaTextDocument document = new JavaTextDocuments(null, null)
				.createDocument(new TextDocumentItem("test.java", "java", 0, "abcd"));
		document.setPackageName(null);

		List<CompletionItem> items = registry.getCompletionItems(document, 0, true, true, (context, model) -> true, JAVAX_PROJECT_INFO);
		assertEquals("Completion size", 1, items.size());
		CompletionItem item = items.get(0);
		assertNotNull("Completion text edit", item.getTextEdit());
		assertEquals("package ${1:packagename};" + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"import javax.ws.rs.GET;" + //
				System.lineSeparator() + //
				"import javax.ws.rs.Path;" //
				, item.getTextEdit().getLeft().getNewText());
	}

	@Test
	public void completionWithEmptyPackagename() {
		// Create the snippet
		Snippet snippet = new Snippet();
		snippet.setPrefixes(Arrays.asList("test"));
		snippet.setBody(new ArrayList<>(Arrays.asList("package ${1:packagename};", //
				"import javax.ws.rs.GET;", //
				"import javax.ws.rs.Path;")));
		JavaTextDocumentSnippetRegistry registry = new JavaTextDocumentSnippetRegistry(false);
		registry.registerSnippet(snippet);

		// Create a Java text document with empty package.
		JavaTextDocument document = new JavaTextDocuments(null, null)
				.createDocument(new TextDocumentItem("test.java", "java", 0, "abcd"));
		document.setPackageName("");

		List<CompletionItem> items = registry.getCompletionItems(document, 0, true, true, (context, model) -> true, JAVAX_PROJECT_INFO);
		assertEquals("Completion size", 1, items.size());
		CompletionItem item = items.get(0);
		assertNotNull("Completion text edit", item.getTextEdit());
		assertEquals("import javax.ws.rs.GET;" + //
				System.lineSeparator() + //
				"import javax.ws.rs.Path;" //
				, item.getTextEdit().getLeft().getNewText());
	}

	private static Optional<Snippet> findByPrefix(String prefix, SnippetRegistry registry) {
		return registry.getSnippets().stream().filter(snippet -> snippet.getPrefixes().contains(prefix)).findFirst();
	}

}
