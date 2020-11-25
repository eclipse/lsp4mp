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
package org.eclipse.lsp4mp.jdt.internal.core.project;

import java.util.Map;

import org.eclipse.lsp4mp.jdt.core.project.MicroProfileConfigPropertyInformation;

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
	 * Returns a map from the property and profile, in the format used by
	 * <code>microprofile-config.properties</code>, to the related property
	 * information, for each property and profile that's assigned a value in this
	 * config source
	 *
	 * A map is used so that it can be merged with another map and override any
	 * property informations from that map
	 *
	 * @param propertyKey the name of the property to collect the values for
	 * @return a map from the property and profile, in the format used by
	 *         <code>microprofile-config.properties</code>, to the related property
	 *         information, for each property and profile that's assigned a value in
	 *         this config source
	 */
	Map<String, MicroProfileConfigPropertyInformation> getPropertyInformations(String propertyKey);

}
