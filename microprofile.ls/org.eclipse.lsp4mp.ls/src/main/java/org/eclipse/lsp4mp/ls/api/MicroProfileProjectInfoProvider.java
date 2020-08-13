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
package org.eclipse.lsp4mp.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfoParams;

/**
 * MicroProfile project information provider.
 *
 * @author Angelo ZERR
 *
 */
public interface MicroProfileProjectInfoProvider {

	/**
	 * Returns the MicroProfile project information for the given
	 * <code>params</code>.
	 *
	 * @param params the MicroProfile project information parameters
	 * @return the MicroProfile project information for the given
	 *         <code>params</code>.
	 */
	@JsonRequest("microprofile/projectInfo")
	CompletableFuture<MicroProfileProjectInfo> getProjectInfo(MicroProfileProjectInfoParams params);

}
