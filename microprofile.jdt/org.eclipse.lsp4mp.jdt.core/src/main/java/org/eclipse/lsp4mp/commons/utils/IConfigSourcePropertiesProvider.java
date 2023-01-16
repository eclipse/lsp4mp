/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.commons.utils;

import java.util.Set;

/**
 * Represents a config source that maps keys to values
 *
 * @author datho7561
 */
public interface IConfigSourcePropertiesProvider {

	/**
	 * Returns a set of all the keys that the config source defines.
	 *
	 * @return a set of all the keys that the config source defines
	 */
	Set<String> keys();

	/**
	 * Returns true if the config source defines a non-null non-empty value for the
	 * given key, and false otherwise.
	 *
	 * @param key the key to check if this config source defines
	 * @return true if the config source defines a non-null non-empty value for the
	 *         given key, and false otherwise
	 */
	boolean hasKey(String key);

	/**
	 * Returns the value of the property, or null if the property doesn't have a value
	 *
	 * @param key the property key
	 * @return the value of the property, or null if the property doesn't have a value
	 */
	String getValue(String key);

}
