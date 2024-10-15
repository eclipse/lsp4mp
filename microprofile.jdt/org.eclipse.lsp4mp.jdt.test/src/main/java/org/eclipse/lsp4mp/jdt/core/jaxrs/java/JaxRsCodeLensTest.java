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
package org.eclipse.lsp4mp.jdt.core.jaxrs.java;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertCodeLens;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.cl;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.r;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeLensParams;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.junit.Test;

/**
 * JAX-RS URL Codelens test for Java file.
 *
 * @author Angelo ZERR
 *
 */
public class JaxRsCodeLensTest extends BasePropertiesManagerTest {

	@Test
	public void urlCodeLensProperties() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.hibernate_orm_resteasy);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/hibernate/orm/FruitResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		// Default port
		assertCodeLenses(8080, params, utils);
	}

	@Test
	public void urlCodeLensYaml() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.hibernate_orm_resteasy_yaml);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/hibernate/orm/FruitResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		// Default port
		assertCodeLenses(8080, params, utils);
	}

	@Test
	public void customJaxRsInfoProvider() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.hibernate_orm_resteasy);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/hibernate/orm/CustomJaxRsResolving.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		assertCodeLens(params, utils, //
				cl("http://localhost:8080/myPath", "", r(7, 18, 26)));
	}

	private static void assertCodeLenses(int port, MicroProfileJavaCodeLensParams params, IJDTUtils utils)
			throws JavaModelException {
		assertCodeLens(params, utils, //
				cl("http://localhost:" + port + "/fruits", "", r(31, 8, 8)), //
				cl("http://localhost:" + port + "/fruits/{id}", "", r(38, 17, 17)), //
				cl("http://localhost:" + port + "/fruits", "", r(48, 18, 18)), //
				cl("http://localhost:" + port + "/fruits/{id}", "", r(60, 18, 18)), //
				cl("http://localhost:" + port + "/fruits/{id}", "", r(79, 17, 17)), //
				cl("http://localhost:" + port + "/fruits/path_with_java_constant", "", r(109, 15, 15)), //
				cl("http://localhost:" + port + "/fruits/path_with_java_constant", "", r(119, 29, 29)));
	}
}
