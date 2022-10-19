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
package org.eclipse.lsp4mp.ls.java;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures.FutureCancelChecker;
import org.eclipse.lsp4mp.commons.JavaFileInfo;
import org.eclipse.lsp4mp.commons.MicroProfileJavaFileInfoParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaProjectLabelsParams;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesChangeEvent;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4mp.ls.api.MicroProfileJavaFileInfoProvider;
import org.eclipse.lsp4mp.ls.api.MicroProfileJavaProjectLabelsProvider;
import org.eclipse.lsp4mp.ls.commons.TextDocument;
import org.eclipse.lsp4mp.ls.commons.TextDocuments;
import org.eclipse.lsp4mp.ls.java.JavaTextDocuments.JavaTextDocument;

/**
 * Java Text documents registry which manages opened Java file.
 *
 * @author Angelo ZERR
 *
 */
class JavaTextDocuments extends TextDocuments<JavaTextDocument> {

	private static final String MICROPROFILE_PROJECT_LABEL = "microprofile";

	private static final Logger LOGGER = Logger.getLogger(JavaTextDocuments.class.getName());

	private static final ProjectLabelInfoEntry PROJECT_INFO_LOADING = new ProjectLabelInfoEntry(null, null, null);

	private final Map<String /* Java file URI */, CompletableFuture<ProjectLabelInfoEntry>> documentCache;

	private final Map<String /* project URI */, CompletableFuture<ProjectLabelInfoEntry>> projectCache;

	private final MicroProfileJavaProjectLabelsProvider projectInfoProvider;

	private final MicroProfileJavaFileInfoProvider fileInfoProvider;

	private JavaTextDocumentSnippetRegistry snippetRegistry;

	/**
	 * Opened Java file.
	 *
	 */
	public class JavaTextDocument extends TextDocument {

		private String projectURI;

		private String packageName;

		private CompletableFuture<JavaFileInfo> fileInfoFuture;

		public JavaTextDocument(TextDocumentItem document) {
			super(document);
			collectFileInfo();
		}

		/**
		 * Collect Java file information (ex : package name) from the JDT LS side.
		 */
		private void collectFileInfo() {
			if (fileInfoProvider != null) {
				if (fileInfoFuture == null || fileInfoFuture.isCancelled()
						|| fileInfoFuture.isCompletedExceptionally()) {
					MicroProfileJavaFileInfoParams params = new MicroProfileJavaFileInfoParams();
					params.setUri(super.getUri());
					fileInfoFuture = fileInfoProvider.getJavaFileInfo(params);
				}
				JavaFileInfo fileInfo = fileInfoFuture.getNow(null);
				if (fileInfo != null) {
					setPackageName(fileInfo.getPackageName());
				}
			}
		}

		public String getProjectURI() {
			return projectURI;
		}

		public String getPackageName() {
			if (packageName == null) {
				collectFileInfo();
			}
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		public void setProjectURI(String projectURI) {
			this.projectURI = projectURI;
		}

		/**
		 * Execute the given code only if the Java file belongs to a MicroProfile
		 * without waiting for the load of project information.
		 *
		 * @param <T>          the type to return.
		 * @param code         the code to execute.
		 * @param defaultValue the default value to return if the Java file doesn't
		 *                     belong to a MicroProfile project.
		 * @return the given code only if the Java file belongs to a MicroProfile
		 *         project without waiting for the load of project information.
		 */
		public <T> CompletableFuture<T> executeIfInMicroProfileProject(
				BiFunction<ProjectLabelInfoEntry, CancelChecker, CompletableFuture<T>> code, T defaultValue) {
			return executeIfInMicroProfileProject(code, defaultValue, false);
		}

		/**
		 * Execute the given code only if the Java file belongs to a MicroProfile
		 * project.
		 *
		 * @param <T>                       the type to return.
		 * @param code                      the code to execute.
		 * @param defaultValue              the default value to return if the Java file
		 *                                  doesn't belong to a MicroProfile project.
		 * @param waitForLoadingProjectInfo true if code to apply must be done when
		 *                                  project information is loaded and false
		 *                                  otherwise.
		 * @return the given code only if the Java file belongs to a MicroProfile
		 *         project.
		 */
		public <T> CompletableFuture<T> executeIfInMicroProfileProject(
				BiFunction<ProjectLabelInfoEntry, CancelChecker, CompletableFuture<T>> code, T defaultValue,
				boolean waitForLoadingProjectInfo) {
			return computeAsyncCompose(cancelChecker -> {
				CompletableFuture<ProjectLabelInfoEntry> projectInfoFuture = getProjectInfo(this);
				ProjectLabelInfoEntry projectInfo = projectInfoFuture.getNow(PROJECT_INFO_LOADING);
				if (isProjectInfoLoading(projectInfo)) {
					// The project information is loading.
					if (!waitForLoadingProjectInfo) {
						// don't wait the load of the project, apply the given code.
						return executeIfInMicroProfileProject(null, code, defaultValue, cancelChecker);
					}
					// Wait the load of the project and apply the given code.
					return projectInfoFuture.thenCompose(loadedProjectInfo -> {
						return executeIfInMicroProfileProject(loadedProjectInfo, code, defaultValue, cancelChecker);
					});
				}
				// The project information is loaded, apply the given code
				return executeIfInMicroProfileProject(projectInfo, code, defaultValue, cancelChecker);
			});
		}

		private <T> CompletableFuture<T> executeIfInMicroProfileProject(ProjectLabelInfoEntry projectInfo,
				BiFunction<ProjectLabelInfoEntry, CancelChecker, CompletableFuture<T>> code, T defaultValue,
				CancelChecker cancelChecker) {
			cancelChecker.checkCanceled();
			if (projectInfo == null || !isMicroProfileProject(projectInfo)) {
				return CompletableFuture.completedFuture(defaultValue);
			}
			return code.apply(projectInfo, cancelChecker);
		}

		/**
		 * Returns true if the Java file belongs to a MicroProfile project and false
		 * otherwise.
		 *
		 * @return true if the Java file belongs to a MicroProfile project and false
		 *         otherwise.
		 */
		public boolean isInMicroProfileProject() {
			ProjectLabelInfoEntry projectInfo = getProjectInfo(this).getNow(null);
			return isMicroProfileProject(projectInfo);
		}
	}

	JavaTextDocuments(MicroProfileJavaProjectLabelsProvider projectInfoProvider,
			MicroProfileJavaFileInfoProvider fileInfoProvider) {
		this.projectInfoProvider = projectInfoProvider;
		this.fileInfoProvider = fileInfoProvider;
		this.documentCache = new ConcurrentHashMap<>();
		this.projectCache = new ConcurrentHashMap<>();
	}

	@Override
	public JavaTextDocument createDocument(TextDocumentItem document) {
		JavaTextDocument doc = new JavaTextDocument(document);
		doc.setIncremental(isIncremental());
		return doc;
	}

	/**
	 * Returns as promise the MicroProfile project information for the given java
	 * file document.
	 *
	 * @param document the java file document.
	 * @return as promise the MicroProfile project information for the given java
	 *         file document.
	 */
	private CompletableFuture<ProjectLabelInfoEntry> getProjectInfo(JavaTextDocument document) {
		return getProjectInfoFromCache(document). //
				exceptionally(ex -> {
					LOGGER.log(Level.WARNING, String.format(
							"Error while getting ProjectLabelInfoEntry (classpath) for '%s'", document.getUri()), ex);
					return null;
				});
	}

	CompletableFuture<ProjectLabelInfoEntry> getProjectInfoFromCache(JavaTextDocument document) {
		String projectURI = document.getProjectURI();
		String documentURI = document.getUri();
		// Search future which load project info in cache
		CompletableFuture<ProjectLabelInfoEntry> projectInfo = null;
		if (projectURI != null) {
			// the java document has already been linked to a project URI, get future from
			// the project cache.
			projectInfo = projectCache.get(projectURI);
		} else {
			// get the current future for the given document URI
			projectInfo = documentCache.get(documentURI);
		}
		if (projectInfo == null || projectInfo.isCancelled() || projectInfo.isCompletedExceptionally()) {
			// not found in the cache, load the project info from the JDT LS Extension
			MicroProfileJavaProjectLabelsParams params = new MicroProfileJavaProjectLabelsParams();
			params.setUri(documentURI);
			params.setTypes(getSnippetRegistry().getTypes());
			final CompletableFuture<ProjectLabelInfoEntry> future = projectInfoProvider.getJavaProjectlabels(params);
			future.thenApply(entry -> {
				if (entry != null) {
					// project info with labels are get from the JDT LS
					String newProjectURI = entry.getUri();
					// cache the project info in the project cache level.
					projectCache.put(newProjectURI, future);
					// update the project URI of the document to link it to a project URI
					document.setProjectURI(newProjectURI);
					// evict the document cache level.
					documentCache.remove(documentURI);
				}
				return entry;
			});
			// cache the future in the document level.
			documentCache.put(documentURI, future);
			return future;
		}

		// Returns the cached project info
		return projectInfo;
	}

	public boolean propertiesChanged(MicroProfilePropertiesChangeEvent event) {
		List<MicroProfilePropertiesScope> scopes = event.getType();
		boolean changedOnlyInSources = MicroProfilePropertiesScope.isOnlySources(scopes);
		if (!changedOnlyInSources && event.getProjectURIs() != null) {
			// evict the project cache level with the given project uris.
			classpathChanged(event.getProjectURIs());
			return true;
		}
		return false;
	}

	private void classpathChanged(Set<String> projectURIs) {
		// Some dependencies have changed, evict the project cache level.
		projectURIs.forEach(projectCache::remove);
	}

	/**
	 * Returns true if the given project information has the "microprofile" label
	 * and false otherwise.
	 *
	 * @param projectInfo the project information.
	 * @return true if the given project information has the "microprofile" label
	 *         and false otherwise.
	 */
	private static boolean isMicroProfileProject(ProjectLabelInfoEntry projectInfo) {
		return projectInfo != null && projectInfo.hasLabel(MICROPROFILE_PROJECT_LABEL);
	}

	public JavaTextDocumentSnippetRegistry getSnippetRegistry() {
		if (snippetRegistry == null) {
			snippetRegistry = new JavaTextDocumentSnippetRegistry();
		}
		return snippetRegistry;
	}

	private static <R> CompletableFuture<R> computeAsyncCompose(Function<CancelChecker, CompletableFuture<R>> code) {
		CompletableFuture<CancelChecker> start = new CompletableFuture<>();
		CompletableFuture<R> result = start.thenComposeAsync(code);
		start.complete(new FutureCancelChecker(result));
		return result;
	}

	private static boolean isProjectInfoLoading(ProjectLabelInfoEntry projectInfo) {
		return PROJECT_INFO_LOADING == projectInfo;
	}
}
