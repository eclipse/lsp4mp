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

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4mp.commons.JavaFileInfo;
import org.eclipse.lsp4mp.commons.MicroProfileJavaFileInfoParams;

/**
 * MicroProfile Java file information provider.
 *
 * @author Angelo ZERR
 *
 */
public interface MicroProfileJavaFileInfoProvider {

	@JsonRequest("microprofile/java/fileInfo")
	default CompletableFuture<JavaFileInfo> getJavaFileInfo(MicroProfileJavaFileInfoParams javaParams) {
		return CompletableFuture.completedFuture(null);
	}

}
