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
package org.eclipse.lsp4mp.jdt.core;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.fixURI;
import static org.junit.Assert.assertEquals;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfilePropertyDocumentationParams;
import org.junit.Test;

/**
 * Tests for {@link PropertiesManager#collectPropertyDocumentation}.
 */
public class PropertiesManagerDocumentationTest extends BasePropertiesManagerTest {

	@Test
	public void testCollectDocumentationMarkdown() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IFile propertiesFile = javaProject.getProject()
				.getFile(new Path("src/main/resources/META-INF/microprofile-config.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		var params = createDocParams(propertiesFileUri, "org.acme.config.GreetingResource", "number", null,
				DocumentFormat.Markdown);
		String documentation = PropertiesManager.getInstance().collectPropertyDocumentation(params, JDT_UTILS, null);

		assertEquals("The `number` of the greeting.", documentation);
	}

	@Test
	public void testCollectDocumentationPlainText() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IFile propertiesFile = javaProject.getProject()
				.getFile(new Path("src/main/resources/META-INF/microprofile-config.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		var params = createDocParams(propertiesFileUri, "org.acme.config.GreetingResource", "number", null,
				DocumentFormat.PlainText);
		String documentation = PropertiesManager.getInstance().collectPropertyDocumentation(params, JDT_UTILS, null);

		assertEquals(" The number of the greeting. ", documentation);
	}

	@Test
	public void testCollectDocumentationForNoDocs() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IFile propertiesFile = javaProject.getProject()
				.getFile(new Path("src/main/resources/META-INF/microprofile-config.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		// try markdown
		var params = createDocParams(propertiesFileUri, "org.acme.config.GreetingResource", "suffix", null,
				DocumentFormat.Markdown);
		String documentation = PropertiesManager.getInstance().collectPropertyDocumentation(params, JDT_UTILS, null);
		assertEquals(null, documentation);
		// try plaintext
		params.setDocumentFormat(DocumentFormat.PlainText);
		documentation = PropertiesManager.getInstance().collectPropertyDocumentation(params, JDT_UTILS, null);
		assertEquals(null, documentation);
	}

	public MicroProfilePropertyDocumentationParams createDocParams(String uri, String sourceType, String sourceField,
			String sourceMethod, DocumentFormat documentFormat) {
		var params = new MicroProfilePropertyDocumentationParams();
		params.setUri(uri);
		params.setSourceType(sourceType);
		params.setSourceField(sourceField);
		params.setSourceMethod(sourceMethod);
		params.setDocumentFormat(documentFormat);
		return params;
	}

}
