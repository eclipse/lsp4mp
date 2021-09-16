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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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

/**
 * Abstract class for config file.
 *
 * @author Angelo ZERR
 *
 * @param <T> the config model (ex: Properties for *.properties file)
 */
public abstract class AbstractConfigSource<T> implements IConfigSource {

	private static final Logger LOGGER = Logger.getLogger(AbstractConfigSource.class.getName());

	private static final int DEFAULT_ORDINAL = 100;

	private final String configFileName;

	private final String profile;

	private final int ordinal;

	private final IJavaProject javaProject;
	private Path outputConfigFile;
	private Path sourceConfigFile;
	private FileTime lastModified;
	private T config;

	private Map<String, List<MicroProfileConfigPropertyInformation>> propertyInformations;

	public AbstractConfigSource(String configFileName, int ordinal, IJavaProject javaProject) {
		this(configFileName, null, ordinal, javaProject);
	}

	public AbstractConfigSource(String configFileName, IJavaProject javaProject) {
		this(configFileName, null, javaProject);
	}

	public AbstractConfigSource(String configFileName, String profile, IJavaProject javaProject) {
		this(configFileName, profile, DEFAULT_ORDINAL, javaProject);
	}

	public AbstractConfigSource(String configFileName, String profile, int ordinal, IJavaProject javaProject) {
		this.configFileName = configFileName;
		this.profile = profile;
		this.ordinal = ordinal;
		this.javaProject = javaProject;
		// load config file to udpate some fields like lastModified, config instance
		// which must be updated when the config source is created. It's important that
		// those fields are initialized here (and not in lazy mode) to prevent from
		// multi
		// thread
		// context.
		init();
	}

	private void init() {
		T config = getConfig();
		if (config != null && propertyInformations == null) {
			propertyInformations = loadPropertyInformations();
		}
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
	private Path getOutputConfigFile() {
		if (outputConfigFile != null && Files.exists(outputConfigFile)) {
			return outputConfigFile;
		}
		sourceConfigFile = null;
		outputConfigFile = null;
		if (javaProject.getProject() != null && javaProject.getProject().isAccessible()) {
			try {
				List<IClasspathEntry> sourceEntries = Stream.of(((JavaProject) javaProject).getResolvedClasspath(true)) //
						.filter(entry -> !entry.isTest()) //
						.filter(entry -> entry.getEntryKind() == IClasspathEntry.CPE_SOURCE
								&& entry.getOutputLocation() != null) //
						.distinct() //
						.collect(Collectors.toList());
				// Search the config file in the source and in the output folders
				for (IClasspathEntry sourceEntry : sourceEntries) {
					// Search the config file in the source folder
					IPath source = sourceEntry.getPath();
					File sourceFile = javaProject.getProject().getLocation().append(source.removeFirstSegments(1))
							.append(configFileName).toFile();
					if (sourceFile.exists()) {
						sourceConfigFile = sourceFile.toPath();
					}
					// Search the config file in the output folder
					IPath output = sourceEntry.getOutputLocation();
					File outputFile = javaProject.getProject().getLocation().append(output.removeFirstSegments(1))
							.append(configFileName).toFile();
					if (outputFile.exists()) {
						outputConfigFile = outputFile.toPath();
					}
				}
				return outputConfigFile;
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

	@Override
	public String getProfile() {
		return profile;
	}

	@Override
	public int getOrdinal() {
		return ordinal;
	}

	@Override
	public String getSourceConfigFileURI() {
		getOutputConfigFile();
		if (sourceConfigFile != null) {
			URI uri = sourceConfigFile.toFile().toURI();
			return fixURI(uri);
		}
		return null;
	}

	private static String fixURI(URI uri) {
		String uriString = uri.toString();
		return uriString.replaceFirst("file:/([^/])", "file:///$1");
	}

	/**
	 * Returns the loaded config and null otherwise.
	 *
	 * @return the loaded config and null otherwise
	 */
	protected final T getConfig() {
		Path configFile = getOutputConfigFile();
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
		propertyInformations = null;
	}

	@Override
	public List<MicroProfileConfigPropertyInformation> getPropertyInformations(String propertyKey) {
		init();
		return propertyInformations != null ? propertyInformations.get(propertyKey) : null;
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
	 * Load the property informations.
	 * 
	 * @return the property information.
	 */
	protected abstract Map<String /* property key without profile */, List<MicroProfileConfigPropertyInformation>> loadPropertyInformations();

}
