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

import java.util.HashSet;
import java.util.Set;

/**
 * Functions for working with <code>IConfigSourcePropertiesProvider</code>
 * 
 * @author datho7561
 */
public class ConfigSourcePropertiesProviderUtils {

	private ConfigSourcePropertiesProviderUtils() {
	}

	/**
	 * Returns a new provider that attempts to get the value from the first
	 * provider, then falls back to the second provider if the value couldn't be
	 * found in the first provider.
	 * 
	 * @param first  the first provider that will be consulted
	 * @param second the second provider that will be consulted
	 * @return Returns a new provider that attempts to get the value from the first
	 *         provider, then falls back to the second provider if the value
	 *         couldn't be found in the first provider.
	 */
	public static IConfigSourcePropertiesProvider layer(IConfigSourcePropertiesProvider first,
			IConfigSourcePropertiesProvider second) {
		return new IConfigSourcePropertiesProvider() {

			@Override
			public Set<String> keys() {
				Set<String> keys = new HashSet<>();
				keys.addAll(second.keys());
				keys.addAll(first.keys());
				return keys;
			}

			@Override
			public boolean hasKey(String key) {
				return first.hasKey(key) || second.hasKey(key);
			}

			@Override
			public String getValue(String key) {
				String firstValue = first.getValue(key);
				if (firstValue != null) {
					return firstValue;
				}
				return second.getValue(key);
			}

		};
	}

}
