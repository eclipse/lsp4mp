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
package org.eclipse.lsp4mp.ls.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;

/**
 * MicroProfile Java diagnostics provider.
 *
 */
public interface MicroProfileJavaDiagnosticsProvider {

	@JsonRequest("microprofile/java/diagnostics")
	default CompletableFuture<List<PublishDiagnosticsParams>> getJavaDiagnostics(
			MicroProfileJavaDiagnosticsParams javaParams) {
		return CompletableFuture.completedFuture(null);
	}

}