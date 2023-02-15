/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.eclipse.lsp4j.jsonrpc.CompletableFutures;

/**
 * Utilities for working with <code>CompletableFuture</code>s.
 */
public class FutureUtils {

	/**
	 * It's a copy of
	 * {@link org.eclipse.lsp4j.jsonrpc.CompletableFutures#computeAsync} that
	 * accepts a function that returns a CompletableFuture.
	 *
	 * @see CompletableFutures#computeAsync
	 *
	 * @param <R>  the return type of the asynchronous computation
	 * @param code the code to run asynchronously
	 * @return a future that sends the correct $/cancelRequest notification when
	 *         canceled
	 */
	public static <R> CompletableFuture<R> computeAsyncCompose(
			Function<ExtendedCancelChecker, CompletableFuture<R>> code) {
		CompletableFuture<ExtendedCancelChecker> start = new CompletableFuture<>();
		CompletableFuture<R> result = start.thenComposeAsync(code);
		CompletableFutureWrapper<R> wrapper = new CompletableFutureWrapper<>(result);
		start.complete(wrapper);
		return wrapper;
	}

}
