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
package org.eclipse.lsp4mp.utils;

/**
 * Environment variable utility class
 *
 */
public class EnvUtils {

	public static final boolean isWindows = isWindows();

	public static final String SYSTEM_PROPERTIES_ORIGN = "System Properties";
	public static final String ENVIRONMENT_VARIABLES_ORIGIN = "Environment variables";

	/**
	 * Return true if the variable is an ENV variable, and false otherwise
	 *
	 * @param variable the variable to check
	 * @return true if the variable is an ENV variable, and false otherwise
	 */
	public static boolean isEnvVariable(String variable) {
		char[] chars = variable.toCharArray();
		for (char c : chars) {
			if (!Character.isUpperCase(c) && Character.isLetter(c)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isWindows() {
		try {
			String property = System.getProperty("os.name");
			return property != null ? property.toLowerCase().contains("win") : false;
		} catch (SecurityException e) {
			return false;
		}
	}
}