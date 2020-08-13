/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.internal.core.project;

/**
 * Configuration file API
 * 
 * @author Angelo ZERR
 *
 */
public interface IConfigSource {

	/**
	 * Returns the property from the given <code>key</code> and null otherwise.
	 * 
	 * @param key the key
	 * @return the property from the given <code>key</code> and null otherwise.
	 */
	String getProperty(String key);

	/**
	 * Returns the property as Integer from the given <code>key</code> and null
	 * otherwise.
	 * 
	 * @param key the key
	 * @return the property as Integer from the given <code>key</code> and null
	 *         otherwise.
	 */
	Integer getPropertyAsInt(String key);
}
