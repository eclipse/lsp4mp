/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaHoverParams;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProject;
import org.eclipse.lsp4mp.jdt.internal.config.java.MicroProfileConfigHoverParticipant;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * JDT Quarkus manager test for hover in Java file.
 *
 *
 */
public class JavaHoverTest extends BasePropertiesManagerTest {

	private static IJavaProject javaProject;

	@After
	public void cleanup() throws JavaModelException, IOException {
		deleteFile(JDTMicroProfileProject.APPLICATION_YAML_FILE, javaProject);
		deleteFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, javaProject);
		deleteFile(JDTMicroProfileProject.MICROPROFILE_CONFIG_PROPERTIES_FILE, javaProject);
	}

	@Test
	public void configPropertyNameHover() throws Exception {

		javaProject = loadMavenProject(MavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, //
				"greeting.message = hello\r\n" + //
						"greeting.name = quarkus\r\n" + //
						"greeting.number = 100",
				javaProject);
		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		Hover info = getActualHover(new Position(14, 40), javaFileUri);
		assertHoverEquals(h("`greeting.message = hello` *in* application.properties", 14, 28, 44), info);

		// Test left edge
		// Position(14, 28) is the character after the | symbol:
		// @ConfigProperty(name = "|greeting.message")
		info = getActualHover(new Position(14, 28), javaFileUri);
		assertHoverEquals(h("`greeting.message = hello` *in* application.properties", 14, 28, 44), info);

		// Test right edge
		// Position(14, 43) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.messag|e")
		info = getActualHover(new Position(14, 43), javaFileUri);
		assertHoverEquals(h("`greeting.message = hello` *in* application.properties", 14, 28, 44), info);

		// Test no hover
		// Position(14, 27) is the character after the | symbol:
		// @ConfigProperty(name = |"greeting.message")
		info = getActualHover(new Position(14, 27), javaFileUri);
		Assert.assertNull(info);

		// Test no hover 2
		// Position(14, 44) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.message|")
		info = getActualHover(new Position(14, 44), javaFileUri);
		Assert.assertNull(info);

		// Hover default value
		// Position(17, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.suffix", defaultValue="!")
		info = getActualHover(new Position(17, 33), javaFileUri);
		assertHoverEquals(h("`greeting.suffix = !` *in* GreetingResource.java", 17, 28, 43), info);

		// Hover override default value
		// Position(26, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.number", defaultValue="0")
		info = getActualHover(new Position(26, 33), javaFileUri);
		assertHoverEquals(h("`greeting.number = 100` *in* application.properties", 26, 28, 43), info);

		// Hover when no value
		// Position(23, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.missing")
		info = getActualHover(new Position(23, 33), javaFileUri);
		assertHoverEquals(h("`greeting.missing` is not set", 23, 28, 44), info);
	}

	@Test
	public void configPropertyNameHoverWithProfiles() throws Exception {

		javaProject = loadMavenProject(MavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, //
				"greeting.message = hello\r\n" + //
						"%dev.greeting.message = hello dev\r\n" + //
						"%prod.greeting.message = hello prod\r\n" + //
						"my.greeting.message\r\n" + //
						"%dev.my.greeting.message",
				javaProject);

		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		Hover info = getActualHover(new Position(14, 40), javaFileUri);
		assertHoverEquals(h("`%dev.greeting.message = hello dev` *in* application.properties  \n" + //
				"`%prod.greeting.message = hello prod` *in* application.properties  \n" + //
				"`greeting.message = hello` *in* application.properties", //
				14, 28, 44), info);

		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, //
				"%dev.greeting.message = hello dev\r\n" + //
						"%prod.greeting.message = hello prod\r\n" + //
						"my.greeting.message\r\n" + //
						"%dev.my.greeting.message",
				javaProject);

		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		info = getActualHover(new Position(14, 40), javaFileUri);
		assertHoverEquals(h("`%dev.greeting.message = hello dev` *in* application.properties  \n" + //
				"`%prod.greeting.message = hello prod` *in* application.properties  \n" + //
				"`greeting.message` is not set", //
				14, 28, 44), info);
	}

	@Test
	public void configPropertyNameYaml() throws Exception {

		javaProject = loadMavenProject(MavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		saveFile(JDTMicroProfileProject.APPLICATION_YAML_FILE, //
				"greeting:\n" + //
						"  message: message from yaml\n" + //
						"  number: 2001",
				javaProject);

		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, //
				"greeting.message = hello\r\n" + //
						"greeting.name = quarkus\r\n" + //
						"greeting.number = 100",
				javaProject);

		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		Hover info = getActualHover(new Position(14, 40), javaFileUri);
		assertHoverEquals(h("`greeting.message = message from yaml` *in* application.yaml", 14, 28, 44), info);

		// Position(26, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.number", defaultValue="0")
		info = getActualHover(new Position(26, 33), javaFileUri);
		assertHoverEquals(h("`greeting.number = 2001` *in* application.yaml", 26, 28, 43), info);

		saveFile(JDTMicroProfileProject.APPLICATION_YAML_FILE, //
				"greeting:\n" + //
						"  message: message from yaml",
				javaProject);

		// fallback to application.properties
		info = getActualHover(new Position(26, 33), javaFileUri);
		assertHoverEquals(h("`greeting.number = 100` *in* application.properties", 26, 28, 43), info);
	}

	@Test
	public void configPropertyNameMethod() throws Exception {

		javaProject = loadMavenProject(MavenProjectName.config_quickstart);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingMethodResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, "greeting.method.message = hello", javaProject);

		// Position(22, 61) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.m|ethod.message")
		Hover info = getActualHover(new Position(22, 61), javaFileUri);
		assertHoverEquals(h("`greeting.method.message = hello` *in* application.properties", 22, 51, 74), info);

		// Position(27, 60) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.m|ethod.suffix" , defaultValue="!")
		info = getActualHover(new Position(27, 60), javaFileUri);
		assertHoverEquals(h("`greeting.method.suffix = !` *in* GreetingMethodResource.java", 27, 50, 72), info);

		// Position(32, 58) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.method.name")
		info = getActualHover(new Position(32, 58), javaFileUri);
		assertHoverEquals(h("`greeting.method.name` is not set", 32, 48, 68), info);
	}

	@Test
	public void configPropertyNameConstructor() throws Exception {

		javaProject = loadMavenProject(MavenProjectName.config_quickstart);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingConstructorResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, "greeting.constructor.message = hello",
				javaProject);
		Hover info;

		// Position(23, 48) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.con|structor.message")
		info = getActualHover(new Position(23, 48), javaFileUri);
		assertHoverEquals(h("`greeting.constructor.message = hello` *in* application.properties", 23, 36, 64), info);

		// Position(24, 48) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.con|structor.suffix" , defaultValue="!")
		info = getActualHover(new Position(24, 48), javaFileUri);
		assertHoverEquals(h("`greeting.constructor.suffix = !` *in* GreetingConstructorResource.java", 24, 36, 63),
				info);

		// Position(25, 48) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.con|structor.name")
		info = getActualHover(new Position(25, 48), javaFileUri);
		assertHoverEquals(h("`greeting.constructor.name` is not set", 25, 36, 61), info);
	}

	@Test
	public void configPropertyNameRespectsPrecendence() throws Exception {

		javaProject = loadMavenProject(MavenProjectName.config_quickstart);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingConstructorResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();
		Hover info;

		// microprofile-config.properties exists
		saveFile(JDTMicroProfileProject.MICROPROFILE_CONFIG_PROPERTIES_FILE, "greeting.constructor.message = hello 1",
				javaProject);
		info = getActualHover(new Position(23, 48), javaFileUri);
		assertHoverEquals(h("`greeting.constructor.message = hello 1` *in* META-INF/microprofile-config.properties", 23, 36, 64), info);

		// microprofile-config.properties and application.properties exist
		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, "greeting.constructor.message = hello 2",
				javaProject);
		info = getActualHover(new Position(23, 48), javaFileUri);
		assertHoverEquals(h("`greeting.constructor.message = hello 2` *in* application.properties", 23, 36, 64), info);

		// microprofile-config.properties, application.properties, and application.yaml exist
		saveFile(JDTMicroProfileProject.APPLICATION_YAML_FILE, //
				"greeting:\n" + //
				"  constructor:\n" + //
				"    message: hello 3", //
				javaProject);
		info = getActualHover(new Position(23, 48), javaFileUri);
		assertHoverEquals(h("`greeting.constructor.message = hello 3` *in* application.yaml", 23, 36, 64), info);

	}

	private Hover getActualHover(Position hoverPosition, String javaFileUri) throws JavaModelException {
		MicroProfileJavaHoverParams params = new MicroProfileJavaHoverParams();
		params.setDocumentFormat(DocumentFormat.Markdown);
		params.setPosition(hoverPosition);
		params.setUri(javaFileUri);
		params.setSurroundEqualsWithSpaces(true);

		return PropertiesManagerForJava.getInstance().hover(params, JDT_UTILS, new NullProgressMonitor());
	}

	private static void assertHoverEquals(Hover expected, Hover actual) {
		assertEquals(expected.getContents().getRight(), actual.getContents().getRight());
		assertEquals(expected.getRange(), actual.getRange());
	}

	private static Hover h(String hoverContent, int startLine, int startOffset, int endLine, int endOffset) {
		Position p1 = new Position(startLine, startOffset);
		Position p2 = new Position(endLine, endOffset);
		Range range = new Range(p1, p2);
		Hover hover = new Hover();
		hover.setContents(Either.forRight(new MarkupContent("markdown", hoverContent)));
		hover.setRange(range);
		return hover;
	}

	private static Hover h(String hoverContent, int line, int startOffset, int endOffset) {
		return h(hoverContent, line, startOffset, line, endOffset);
	}

}