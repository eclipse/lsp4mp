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
package org.eclipse.lsp4mp.jdt.core.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utilities for working with Yaml
 *
 * @author datho7561
 */
public class YamlUtils {

	private YamlUtils() {
	};

	/**
	 * Returns the set of keys defined in the given Yaml map in the format used by
	 * microprofile-config.properties
	 *
	 * @param yamlMap the yaml map
	 * @return the set of keys defined in the given Yaml map in the format used by
	 *         microprofile-config.properties
	 */
	public static Set<String> flattenYamlMapToKeys(Map<String, Object> yamlMap) {
		Set<String> keys = new HashSet<>();
		for (String keyRoot : yamlMap.keySet()) {
			Object keyRootValue = yamlMap.get(keyRoot);
			if (keyRootValue instanceof Map<?, ?>) {
				Set<String> childKeys = flattenYamlMapToKeys((Map<String, Object>) keyRootValue);
				for (String childKey : childKeys) {
					keys.add(keyRoot + "." + childKey);
				}
			} else {
				keys.add(keyRoot);
			}
		}
		return keys;
	}

}
