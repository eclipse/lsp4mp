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

import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * {@link CancelChecker} extension to add {@link CompletableFuture} to cancel
 * when the main {@link CompletableFuture} is cancelled.
 * 
 * @author Angelo ZERR
 *
 */
public interface ExtendedCancelChecker extends CancelChecker {

	/**
	 * Add future to cancel when cancel is called.
	 *
	 * @param futureToCancel the future to cancel if needed.
	 * 
	 * @return the future to cancel.
	 */
	<T> CompletableFuture<T> cancelIfNeeded(CompletableFuture<T> futureToCancel);
}
