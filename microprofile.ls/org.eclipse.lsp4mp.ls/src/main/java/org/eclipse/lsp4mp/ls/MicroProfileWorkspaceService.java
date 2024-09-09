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
package org.eclipse.lsp4mp.ls;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.eclipse.lsp4mp.ls.java.JavaTextDocuments;
import org.eclipse.lsp4mp.utils.FutureUtils;

/**
 * MicroProfile workspace service.
 *
 */
public class MicroProfileWorkspaceService implements WorkspaceService {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileWorkspaceService.class.getName());

	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private final MicroProfileLanguageServer microprofileLanguageServer;
	private final JavaTextDocuments javaTextDocuments;

	public MicroProfileWorkspaceService(MicroProfileLanguageServer microprofileLanguageServer,
			JavaTextDocuments javaTextDocuments) {
		this.microprofileLanguageServer = microprofileLanguageServer;
		this.javaTextDocuments = javaTextDocuments;
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		microprofileLanguageServer.updateSettings(params.getSettings());
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {

	}

	@Override
	public CompletableFuture<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>> symbol(
			WorkspaceSymbolParams params) {
		return FutureUtils.computeAsyncCompose(cancelChecker -> {

			return javaTextDocuments.getWorkspaceProjects() //
					.thenCompose((workspaceProjects) -> {

						cancelChecker.checkCanceled();

						List<CompletableFuture<List<SymbolInformation>>> symbolFutures = workspaceProjects.stream() //
								.map(projectLabelInfo -> {
									String uri = projectLabelInfo.getUri();
									return cancelChecker.cancelIfNeeded(microprofileLanguageServer.getLanguageClient()
											.getJavaWorkspaceSymbols(uri));
								}) //
								.collect(Collectors.toList());

						cancelChecker.checkCanceled();

						// NOTE: we don't need to implement resolve, because resolve is just
						// for calculating the source range. The source range is very cheap to calculate
						// in comparison to invoking the search engine to locate the symbols.

						return CompletableFuture
								.allOf((CompletableFuture[]) symbolFutures.stream().toArray(CompletableFuture[]::new)) //
								.exceptionally(e -> { //
									if (!(e instanceof CancellationException)
											&& !(e.getCause() instanceof CancellationException)) {
										LOGGER.log(Level.SEVERE, "Failure while collecting symbols", e);
									}
									return null;
								}) //
								.thenApply(_void -> {
									// remove the background task to cancel the delegate commands, since they should
									// all be completed or cancelled by now
									cancelChecker.checkCanceled();

									return Either.forLeft(symbolFutures.stream() //
											.flatMap(projectSymbolsFuture -> {
												List<SymbolInformation> symbols = projectSymbolsFuture.getNow(null);
												return symbols != null ? symbols.stream() : Stream.empty();
											}) //
											.collect(Collectors.toList()));
								});
					});
		});

	}

}
