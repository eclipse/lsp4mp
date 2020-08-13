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
package org.eclipse.lsp4mp.settings;

import org.eclipse.lsp4j.HoverCapabilities;

/**
 * A wrapper around LSP {@link HoverCapabilities}.
 *
 */
public class MicroProfileHoverSettings {

	private HoverCapabilities capabilities;

	public void setCapabilities(HoverCapabilities capabilities) {
		this.capabilities = capabilities;
	}

	public HoverCapabilities getCapabilities() {
		return capabilities;
	}

	/**
	 * Returns <code>true</code> if the client support the given documentation
	 * format and <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the client support the given documentation
	 *         format and <code>false</code> otherwise.
	 */
	public boolean isContentFormatSupported(String documentationFormat) {
		return capabilities.getContentFormat() != null && capabilities.getContentFormat().contains(documentationFormat);
	}

}
