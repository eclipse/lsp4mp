/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v2.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls.api;

import org.eclipse.lsp4j.services.LanguageClient;

/**
 * MicroProfile language client API.
 * 
 * @author Angelo ZERR
 *
 */
public interface MicroProfileLanguageClientAPI extends LanguageClient, MicroProfileProjectInfoProvider,
		MicroProfilePropertyDefinitionProvider, MicroProfileJavaCodeActionProvider, MicroProfileJavaCodeLensProvider,
		MicroProfileJavaDiagnosticsProvider, MicroProfileJavaHoverProvider, MicroProfileJavaProjectLabelsProvider {

}
