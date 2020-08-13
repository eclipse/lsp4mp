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
package org.eclipse.lsp4mp.settings;

/**
 * MicroProfile formatting settings
 */
public class MicroProfileFormattingSettings {

	private boolean surroundEqualsWithSpaces;

	/**
	 * Returns <code>true</code> if spaces are to be inserted around the equals sign
	 *
	 * @return <code>true</code> if spaces are to be inserted around the equals sign
	 */
	public boolean isSurroundEqualsWithSpaces() {
		return surroundEqualsWithSpaces;
	}

	/**
	 * Sets whether to insert spaces around the equals sign or not
	 *
	 * @param insertSpaces the <code>boolean</code> that determines whether to
	 *                     insert spaces around the equals sign or not
	 */
	public void setSurroundEqualsWithSpaces(boolean surroundEqualsWithSpaces) {
		this.surroundEqualsWithSpaces = surroundEqualsWithSpaces;
	}

}
