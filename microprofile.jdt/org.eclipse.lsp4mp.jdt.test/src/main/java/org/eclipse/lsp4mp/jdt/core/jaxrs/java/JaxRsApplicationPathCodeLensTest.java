/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.core.jaxrs.java;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertCodeLens;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.cl;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.r;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.ProjectUtils;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeLensParams;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest.GradleProjectName;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest.MicroProfileMavenProjectName;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JAX-RS URL Codelens test for Java file with @ApplicationPath annotation.
 *
 */
public class JaxRsApplicationPathCodeLensTest extends BasePropertiesManagerTest {

	@BeforeClass
	public static void setupTests() throws Exception {
		BasePropertiesManagerTest.loadJavaProjects(new String [] {
				"maven/" + MicroProfileMavenProjectName.microprofile_applicationpath,
				"maven/" + MicroProfileMavenProjectName.open_liberty
				});
	}

	@Test
	public void urlCodeLensApplicationPath() throws Exception {
		IJavaProject javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.microprofile_applicationpath);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/ApplicationPathResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		saveFile("org/acme/MyApplication.java", "package org.acme;\r\n" + //
				"import javax.ws.rs.ApplicationPath;\r\n" + //
				"import javax.ws.rs.core.Application;\r\n" + //
				"@ApplicationPath(\"/api\")\r\n" + //
				"public class MyApplication extends Application {}\r\n", javaProject, true);

		// Default port
		assertCodeLenses(8080, params, utils, "/api/path");
	}

	@Test
	public void urlCodeLensApplicationPathNoSlash() throws Exception {
		IJavaProject javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.microprofile_applicationpath);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/ApplicationPathResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		saveFile("org/acme/MyApplication.java", "package org.acme;\r\n" + //
				"import javax.ws.rs.ApplicationPath;\r\n" + //
				"import javax.ws.rs.core.Application;\r\n" + //
				"@ApplicationPath(\"api\")\r\n" + //
				"public class MyApplication extends Application {}\r\n", javaProject, true);

		// Default port
		assertCodeLenses(8080, params, utils, "/api/path");
	}

	@Test
	public void urlCodeLensApplicationPathChange() throws Exception {
		IJavaProject javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.microprofile_applicationpath);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/ApplicationPathResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		saveFile("org/acme/MyApplication.java", "package org.acme;\r\n" + //
				"import javax.ws.rs.ApplicationPath;\r\n" + //
				"import javax.ws.rs.core.Application;\r\n" + //
				"@ApplicationPath(\"/api\")\r\n" + //
				"public class MyApplication extends Application {}\r\n", javaProject, true);

		// Default port
		assertCodeLenses(8080, params, utils, "/api/path");

		saveFile("org/acme/MyApplication.java", "package org.acme;\r\n" + //
				"import javax.ws.rs.ApplicationPath;\r\n" + //
				"import javax.ws.rs.core.Application;\r\n" + //
				"@ApplicationPath(\"/ipa\")\r\n" + //
				"public class MyApplication extends Application {}\r\n", javaProject, true);

		assertCodeLenses(8080, params, utils, "/ipa/path");
	}

	@Test
	public void openLibertyJakarta() throws Exception {
		IJavaProject javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.open_liberty);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/com/demo/rest/MyResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		assertCodeLens(params, utils, //
				cl("http://localhost:8080/api/api/resource", "", r(13, 5, 5)));
	}

	private static void assertCodeLenses(int port, MicroProfileJavaCodeLensParams params, IJDTUtils utils,
			String actualEndpoint) throws JavaModelException {
		assertCodeLens(params, utils, //
				cl("http://localhost:" + port + actualEndpoint, "", r(12, 35, 35)));
	}

}
