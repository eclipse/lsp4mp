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
package org.eclipse.lsp4mp.jdt.core.config.java;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaHover;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.fixURI;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.h;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ls.core.internal.ProjectUtils;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.TestConfigSourceProvider;
import org.eclipse.lsp4mp.jdt.internal.core.providers.MicroProfileConfigSourceProvider;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JDT MicroProfile manager test for hover in Java file.
 *
 *
 */
public class MicroProfileConfigJavaHoverTest extends BasePropertiesManagerTest {

	private static IJavaProject javaProject;

	@BeforeClass
	public static void setupProject() throws Exception {
		loadJavaProjects(new String[] {
				"maven/" + MicroProfileMavenProjectName.config_hover,
				"maven/" + MicroProfileMavenProjectName.config_quickstart });
	}

	@After
	public void cleanup() throws Exception {
		deleteFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE, javaProject);
		deleteFile(TestConfigSourceProvider.MICROPROFILE_CONFIG_TEST_FILE, javaProject);
		deleteFile(TestConfigSourceProvider.CONFIG_FILE, javaProject);
	}

	@Test
	public void configPropertyNameHover() throws Exception {

		javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/META-INF/microprofile-config.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		saveFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE, //
				"greeting.message = hello\r\n" + //
						"greeting.name = quarkus\r\n" + //
						"greeting.number = 100",
				javaProject);
		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS, h(
				"`greeting.message = hello` *in* [META-INF/microprofile-config.properties](" + propertiesFileUri + ")",
				14, 28, 44));

		// Test left edge
		// Position(14, 28) is the character after the | symbol:
		// @ConfigProperty(name = "|greeting.message")
		assertJavaHover(new Position(14, 28), javaFileUri, JDT_UTILS, h(
				"`greeting.message = hello` *in* [META-INF/microprofile-config.properties](" + propertiesFileUri + ")",
				14, 28, 44));

		// Test right edge
		// Position(14, 43) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.messag|e")
		assertJavaHover(new Position(14, 43), javaFileUri, JDT_UTILS, h(
				"`greeting.message = hello` *in* [META-INF/microprofile-config.properties](" + propertiesFileUri + ")",
				14, 28, 44));

		// Test no hover
		// Position(14, 27) is the character after the | symbol:
		// @ConfigProperty(name = |"greeting.message")
		assertJavaHover(new Position(14, 27), javaFileUri, JDT_UTILS, null);

		// Test no hover 2
		// Position(14, 44) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.message|")
		assertJavaHover(new Position(14, 44), javaFileUri, JDT_UTILS, null);

		// Hover default value
		// Position(17, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.suffix", defaultValue="!")
		assertJavaHover(new Position(17, 33), javaFileUri, JDT_UTILS,
				h("`greeting.suffix = !` *in* [GreetingResource.java](" + javaFileUri + ")", 17, 28, 43));

		// Hover override default value
		// Position(26, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.number", defaultValue="0")
		assertJavaHover(new Position(29, 33), javaFileUri, JDT_UTILS,
				h("`greeting.number = 100` *in* [META-INF/microprofile-config.properties](" + propertiesFileUri + ")",
						29, 28, 43));

		// Hover when no value
		// Position(23, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.missing")
		assertJavaHover(new Position(23, 33), javaFileUri, JDT_UTILS, h("`greeting.missing` is not set", 23, 28, 44));
	}

	@Test
	public void configPropertyNameHoverWithProfiles() throws Exception {

		javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/META-INF/microprofile-config.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		saveFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE, //
				"greeting.message = hello\r\n" + //
						"%dev.greeting.message = hello dev\r\n" + //
						"%prod.greeting.message = hello prod\r\n" + //
						"my.greeting.message\r\n" + //
						"%dev.my.greeting.message",
				javaProject);

		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS, //
				h("`%dev.greeting.message = hello dev` *in* [META-INF/microprofile-config.properties]("
						+ propertiesFileUri + ")  \n" + //
						"`%prod.greeting.message = hello prod` *in* [META-INF/microprofile-config.properties]("
						+ propertiesFileUri + ")  \n" + //
						"`greeting.message = hello` *in* [META-INF/microprofile-config.properties](" + propertiesFileUri
						+ ")", //
						14, 28, 44));

		saveFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE, //
				"%dev.greeting.message = hello dev\r\n" + //
						"%prod.greeting.message = hello prod\r\n" + //
						"my.greeting.message\r\n" + //
						"%dev.my.greeting.message",
				javaProject);

		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS, //
				h("`%dev.greeting.message = hello dev` *in* [META-INF/microprofile-config.properties]("
						+ propertiesFileUri + ")  \n" + //
						"`%prod.greeting.message = hello prod` *in* [META-INF/microprofile-config.properties]("
						+ propertiesFileUri + ")  \n" + //
						"`greeting.message` is not set", //
						14, 28, 44));
	}

	@Test
	public void configPropertyNameMethod() throws Exception {

		javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.config_quickstart);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingMethodResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/META-INF/microprofile-config.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		saveFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE,
				"greeting.method.message = hello", javaProject);

		// Position(22, 61) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.m|ethod.message")
		assertJavaHover(new Position(22, 61), javaFileUri, JDT_UTILS,
				h("`greeting.method.message = hello` *in* [META-INF/microprofile-config.properties]("
						+ propertiesFileUri + ")", 22, 51, 74));

		// Position(27, 60) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.m|ethod.suffix" , defaultValue="!")
		assertJavaHover(new Position(27, 60), javaFileUri, JDT_UTILS,
				h("`greeting.method.suffix = !` *in* [GreetingMethodResource.java](" + javaFileUri + ")", 27, 50, 72));

		// Position(32, 58) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.method.name")
		assertJavaHover(new Position(32, 48), javaFileUri, JDT_UTILS,
				h("`greeting.method.name` is not set", 32, 48, 68));
	}

	@Test
	public void configPropertyNameConstructor() throws Exception {

		javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.config_quickstart);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingConstructorResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/META-INF/microprofile-config.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		saveFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE,
				"greeting.constructor.message = hello", javaProject);

		// Position(23, 48) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.con|structor.message")
		assertJavaHover(new Position(23, 48), javaFileUri, JDT_UTILS, //
				h("`greeting.constructor.message = hello` *in* [META-INF/microprofile-config.properties]("
						+ propertiesFileUri + ")", 23, 36, 64));

		// Position(24, 48) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.con|structor.suffix" , defaultValue="!")
		assertJavaHover(new Position(24, 48), javaFileUri, JDT_UTILS, //
				h("`greeting.constructor.suffix = !` *in* [GreetingConstructorResource.java](" + javaFileUri + ")", 24,
						36, 63));

		// Position(25, 48) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.con|structor.name")
		assertJavaHover(new Position(25, 48), javaFileUri, JDT_UTILS, //
				h("`greeting.constructor.name` is not set", 25, 36, 61));
	}

	@Test
	public void configPropertyNamePrecedence() throws Exception {

		javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/META-INF/microprofile-config.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());
		IFile configPropertiesFile = project.getFile(new Path("src/main/resources/META-INF/config.properties"));
		String configPropertiesFileUri = fixURI(configPropertiesFile.getLocation().toFile().toURI());

		saveFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE, //
				"greeting.message = hello\r\n", javaProject);
		saveFile(TestConfigSourceProvider.CONFIG_FILE, //
				"greeting.message = hi\r\n", javaProject);

		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS,
				h("`greeting.message = hi` *in* [META-INF/config.properties](" + configPropertiesFileUri + ")", 14, 28,
						44));

		saveFile(TestConfigSourceProvider.CONFIG_FILE, //
				"\r\n", javaProject);

		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS, h(
				"`greeting.message = hello` *in* [META-INF/microprofile-config.properties](" + propertiesFileUri + ")",
				14, 28, 44));

	}

	@Test
	public void configPropertyNameProfile() throws Exception {

		javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/META-INF/microprofile-config.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());
		IFile testPropertiesFile = project
				.getFile(new Path("src/main/resources/META-INF/microprofile-config-test.properties"));
		String testPropertiesFileUri = fixURI(testPropertiesFile.getLocation().toFile().toURI());

		saveFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE, //
				"greeting.message = hello\r\n", javaProject);
		saveFile(TestConfigSourceProvider.MICROPROFILE_CONFIG_TEST_FILE, //
				"greeting.message = hi\r\n", javaProject);

		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS,
				h("`%test.greeting.message = hi` *in* [META-INF/microprofile-config-test.properties]("
						+ testPropertiesFileUri + ")  \n" + //
						"`greeting.message = hi` *in* [META-INF/microprofile-config-test.properties]("
						+ testPropertiesFileUri + ")", 14, 28, 44));

		saveFile(TestConfigSourceProvider.MICROPROFILE_CONFIG_TEST_FILE, //
				"\r\n", javaProject);

		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS, h(
				"`greeting.message = hello` *in* [META-INF/microprofile-config.properties](" + propertiesFileUri + ")",
				14, 28, 44));

	}

	@Test
	public void configPropertyNameResolveExpression() throws Exception {
		javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/META-INF/microprofile-config.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		saveFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE, //
				"greeting.message = ${asdf}\r\n" + "asdf = hello", //
				javaProject);
		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS, h(
				"`greeting.message = hello` *in* [META-INF/microprofile-config.properties](" + propertiesFileUri + ")",
				14, 28, 44));

		saveFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE, //
				"greeting.message = ${${asdf}}\r\n" + "asdf = hjkl\r\n" + "hjkl = salutations", //
				javaProject);
		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS,
				h("`greeting.message = salutations` *in* [META-INF/microprofile-config.properties](" + propertiesFileUri
						+ ")", 14, 28, 44));

		saveFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE, //
				"greeting.message = ${asdf:hi}\r\n", //
				javaProject);
		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS,
				h("`greeting.message = hi` *in* [META-INF/microprofile-config.properties](" + propertiesFileUri + ")",
						14, 28, 44));

		saveFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE, //
				"greeting.message = ${asdf}\r\n", //
				javaProject);
		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS,
				h("`greeting.message = ${asdf}` *in* [META-INF/microprofile-config.properties](" + propertiesFileUri
						+ ")", 14, 28, 44));
	}
}