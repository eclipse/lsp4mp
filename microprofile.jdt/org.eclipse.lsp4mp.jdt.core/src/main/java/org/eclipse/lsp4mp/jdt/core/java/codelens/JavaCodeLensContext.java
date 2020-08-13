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
package org.eclipse.lsp4mp.jdt.core.java.codelens;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeLensParams;
import org.eclipse.lsp4mp.jdt.core.java.AbtractJavaContext;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;

/**
 * Java codeLens context for a given compilation unit.
 *
 * @author Angelo ZERR
 *
 */
public class JavaCodeLensContext extends AbtractJavaContext {

	private final MicroProfileJavaCodeLensParams params;

	public JavaCodeLensContext(String uri, ITypeRoot typeRoot, IJDTUtils utils, MicroProfileJavaCodeLensParams params) {
		super(uri, typeRoot, utils);
		this.params = params;
	}

	public MicroProfileJavaCodeLensParams getParams() {
		return params;
	}

}
