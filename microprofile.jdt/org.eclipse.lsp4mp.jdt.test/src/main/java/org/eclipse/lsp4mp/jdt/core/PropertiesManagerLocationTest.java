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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ls.core.internal.ProjectUtils;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest.MicroProfileMavenProjectName;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test with find MicroProfile definition.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesManagerLocationTest extends BasePropertiesManagerTest {

	@BeforeClass
	public static void setupTests() throws Exception {
		BasePropertiesManagerTest.loadJavaProjects(new String [] {
				"maven/" + MicroProfileMavenProjectName.using_vertx,
				"maven/" + MicroProfileMavenProjectName.config_properties,
				"maven/" + MicroProfileMavenProjectName.config_quickstart
				});
	}

	@Test
	public void usingVertxTest() throws Exception {
		IJavaProject javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.using_vertx);

		// Test with Java sources
		// myapp.schema.create
		Location location = PropertiesManager.getInstance().findPropertyLocation(javaProject, "org.acme.vertx.FruitResource",
				"schemaCreate", null, JDT_UTILS, new NullProgressMonitor());
		Assert.assertNotNull("Definition from Java Sources", location);
	}

	@Test
	public void configPropertiesTest() throws Exception {
		IJavaProject javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.config_properties);

		// Test with method
		// greetingInterface.name
		Location location = PropertiesManager.getInstance().findPropertyLocation(javaProject,
				"org.acme.config.IGreetingConfiguration", null, "getName()QOptional<QString;>;",
				JDT_UTILS, new NullProgressMonitor());

		Assert.assertNotNull("Definition from IGreetingConfiguration#getName() method", location);
	}

	@Test
	public void configPropertiesMethodTest() throws Exception {
		IJavaProject javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.config_quickstart);

		// Test with method with parameters
		// greeting.constructor.message
		Location location = PropertiesManager.getInstance().findPropertyLocation(javaProject,
				"org.acme.config.GreetingMethodResource", null, "setMessage(QString;)V",
				JDT_UTILS, new NullProgressMonitor());

		Assert.assertNotNull("Definition from GreetingMethodResource#setMessage() method", location);
	}

	@Test
	public void configPropertiesConstructorTest() throws Exception {
		IJavaProject javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.config_quickstart);

		// Test with constructor with parameters
		// greeting.constructor.message
		Location location = PropertiesManager.getInstance().findPropertyLocation(javaProject,
				"org.acme.config.GreetingConstructorResource", null,
				"GreetingConstructorResource(QString;QString;QOptional<QString;>;)V",
				JDT_UTILS, new NullProgressMonitor());

		Assert.assertNotNull("Definition from GreetingConstructorResource constructor", location);
	}

	@Test
	public void nonExistantFieldTest() throws Exception {
		// Use case:
		// In the properties file we have the following:
		// ```properties
		// my.fruit=Banana
		// ```
		//
		// In a Java file, we have `my.fruit` defined as:
		// ```java
		// @ConfigProperty(name="my.fruit") public org.acme.vertx.Fruit myFruit;
		// ```
		//
		// Because of this, we are expecting class "Fruit" to be an enum, but it is a
		// POJO.
		// When we attempt to locate the enum value `Banana`, it returns a handle to a
		// non-existent field, which we must ignore.

		IJavaProject javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.using_vertx);

		Location location = PropertiesManager.getInstance().findPropertyLocation(javaProject, "org.acme.vertx.Fruit",
				"Banana", null, JDT_UTILS, new NullProgressMonitor());
		Assert.assertNull(location);
	}

}
