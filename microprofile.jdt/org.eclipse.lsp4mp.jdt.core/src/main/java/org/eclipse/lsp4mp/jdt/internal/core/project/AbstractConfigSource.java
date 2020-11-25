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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.lsp4mp.jdt.core.project.MicroProfileConfigPropertyInformation;

/**
 * Abstract class for config file.
 *
 * @author Angelo ZERR
 *
 * @param <T> the config model (ex: Properties for *.properties file)
 */
public abstract class AbstractConfigSource<T> implements IConfigSource {

	private static final Logger LOGGER = Logger.getLogger(AbstractConfigSource.class.getName());

	private final String configFileName;
	private final IJavaProject javaProject;
	private Path configFile;

	private FileTime lastModified;

	private T config;

	public AbstractConfigSource(String configFileName, IJavaProject javaProject) {
		this.configFileName = configFileName;
		this.javaProject = javaProject;
	}

	/**
	 * Returns the target/classes/$configFile and null otherwise.
	 *
	 * <p>
	 * Using this file instead of using src/main/resources/$configFile gives the
	 * capability to get the filtered value.
	 * </p>
	 *
	 * @return the target/classes/$configFile and null otherwise.
	 */
	private Path getConfigFile() {
		if (configFile != null && Files.exists(configFile)) {
			return configFile;
		}
		if (javaProject.getProject() != null && javaProject.getProject().isAccessible()) {
			try {
				List<IPath> outputs = Stream.of(((JavaProject) javaProject).getResolvedClasspath(true)) //
						.filter(entry -> !entry.isTest()) //
						.filter(entry -> entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) //
						.map(entry -> entry.getOutputLocation()) //
						.filter(output -> output != null) //
						.distinct() //
						.collect(Collectors.toList());
				for (IPath output : outputs) {
					File file = javaProject.getProject().getLocation().append(output.removeFirstSegments(1))
							.append(configFileName).toFile();
					if (file.exists()) {
						configFile = file.toPath();
						return configFile;
					}
				}
				return null;
			} catch (JavaModelException e) {
				LOGGER.log(Level.SEVERE, "Error while getting configuration", e);
				return null;
			}
		}
		return null;
	}

	@Override
	public String getConfigFileName() {
		return configFileName;
	}

	/**
	 * Returns the loaded config and null otherwise.
	 *
	 * @return the loaded config and null otherwise
	 */
	private T getConfig() {
		Path configFile = getConfigFile();
		if (configFile == null) {
			reset();
			return null;
		}
		try {
			FileTime currentLastModified = Files.getLastModifiedTime(configFile);
			if (!currentLastModified.equals(lastModified)) {
				reset();
				try (InputStream input = new FileInputStream(configFile.toFile())) {
					config = loadConfig(input);
					lastModified = Files.getLastModifiedTime(configFile);
				} catch (IOException e) {
					reset();
					LOGGER.log(Level.SEVERE, "Error while loading properties from '" + configFile + "'.", e);
				}
			}
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, "Error while getting last modified time for '" + configFile + "'.", e1);
		}
		return config;
	}

	@Override
	public final String getProperty(String key) {
		T config = getConfig();
		if (config == null) {
			return null;
		}
		return getProperty(key, config);
	}

	@Override
	public Integer getPropertyAsInt(String key) {
		String property = getProperty(key);
		if (property != null && !property.trim().isEmpty()) {
			try {
				return Integer.parseInt(property.trim());
			} catch (NumberFormatException e) {
				LOGGER.log(Level.SEVERE,
						"Error while converting '" + property.trim() + "' as Integer for key '" + key + "'", e);
				return null;
			}
		}
		return null;
	}

	private void reset() {
		config = null;
	}

	@Override
	public Map<String, MicroProfileConfigPropertyInformation> getPropertyInformations(String propertyKey) {
		return getPropertyInformations(propertyKey, getConfig());
	}

	/**
	 * Load the config model from the given input stream <code>input</code>.
	 *
	 * @param input the input stream
	 * @return he config model from the given input stream <code>input</code>.
	 * @throws IOException
	 */
	protected abstract T loadConfig(InputStream input) throws IOException;

	/**
	 * Returns the property from the given <code>key</code> and null otherwise.
	 *
	 * @param key
	 * @param config
	 * @return the property from the given <code>key</code> and null otherwise.
	 */
	protected abstract String getProperty(String key, T config);

	/**
	 * Returns the property informations for the given propertyKey
	 *
	 * The property information are returned as a Map from the property and profile
	 * in the microprofile-config.properties format to the property information
	 *
	 * @param propertyKey
	 * @param config
	 * @return the property informations for the given propertyKey
	 */
	protected abstract Map<String, MicroProfileConfigPropertyInformation> getPropertyInformations(String propertyKey,
			T config);

}
