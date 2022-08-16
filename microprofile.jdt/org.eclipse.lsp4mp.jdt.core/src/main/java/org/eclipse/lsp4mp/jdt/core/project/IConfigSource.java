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
package org.eclipse.lsp4mp.jdt.core.project;

import java.util.List;
import java.util.Set;

/**
 * Configuration source (ex : META-INF/microprofile-config.properties) API
 *
 * @author Angelo ZERR
 *
 */
public interface IConfigSource {

	/**
	 * Returns the property from the given <code>key</code> and null otherwise.
	 * 
	 * Any property expressions in the property value should not be expanded.
	 *
	 * @param key the key
	 * @return the property from the given <code>key</code> and null otherwise.
	 */
	String getProperty(String key);

	/**
	 * Returns the property as Integer from the given <code>key</code> and null
	 * otherwise.
	 * 
	 * Any property expressions in the property value should not be expanded.
	 *
	 * @param key the key
	 * @return the property as Integer from the given <code>key</code> and null
	 *         otherwise.
	 */
	Integer getPropertyAsInt(String key);

	/**
	 * Returns the file name of the associated config file
	 *
	 * @return the file name of the associated config file
	 */
	String getConfigFileName();

	/**
	 * Returns the source file URI of the associated config file
	 *
	 * @return the source file URI of the associated config file
	 */
	String getSourceConfigFileURI();

	/**
	 * Returns a list of all values for properties and different profiles that are
	 * defined in this config source.
	 * 
	 * <p>
	 * This list contains information for the property (ex : greeting.message) and
	 * profile property (ex : %dev.greeting.message).
	 * </p>
	 *
	 * @param propertyKey the name of the property to collect the values for
	 * 
	 * @return a list of all values for properties and different profiles that are
	 *         defined in this config source.
	 */
	List<MicroProfileConfigPropertyInformation> getPropertyInformations(String propertyKey);

	/**
	 * Returns the ordinal for this config source
	 *
	 * See
	 * https://download.eclipse.org/microprofile/microprofile-config-2.0/microprofile-config-spec-2.0.html#_configsource_ordering
	 *
	 * @return the ordinal for this config source
	 */
	int getOrdinal();

	/**
	 * Returns the profile of the config source and null otherwise.
	 * 
	 * @return the profile of the config source and null otherwise.
	 */
	String getProfile();

	/**
	 * Returns the keys of all values defined in this config source
	 *
	 * @return the keys of all values defined in this config source
	 */
	Set<String> getAllKeys();

}
