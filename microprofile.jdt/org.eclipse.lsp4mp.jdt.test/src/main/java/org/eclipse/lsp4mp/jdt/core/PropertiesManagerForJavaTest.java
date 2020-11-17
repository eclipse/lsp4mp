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
package org.eclipse.lsp4mp.jdt.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.commons.JavaFileInfo;
import org.eclipse.lsp4mp.commons.MicroProfileJavaFileInfoParams;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for Java file information.
 * 
 * @author Angelo ZERR
 *
 */
public class PropertiesManagerForJavaTest extends BasePropertiesManagerTest {

	@Test
	public void fileInfoWithPackage() throws CoreException, Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		MicroProfileJavaFileInfoParams params = new MicroProfileJavaFileInfoParams();
		params.setUri(javaFileUri);
		JavaFileInfo javaFileInfo = PropertiesManagerForJava.getInstance().fileInfo(params, JDT_UTILS,
				new NullProgressMonitor());
		Assert.assertNotNull(javaFileInfo);
		Assert.assertEquals("org.acme.config", javaFileInfo.getPackageName());
	}

	@Test
	public void fileInfoWithoutPackage() throws CoreException, Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/NoPackage.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		MicroProfileJavaFileInfoParams params = new MicroProfileJavaFileInfoParams();
		params.setUri(javaFileUri);
		JavaFileInfo javaFileInfo = PropertiesManagerForJava.getInstance().fileInfo(params, JDT_UTILS,
				new NullProgressMonitor());
		Assert.assertNotNull(javaFileInfo);
		Assert.assertEquals("", javaFileInfo.getPackageName());
	}

	@Test
	public void fileInfoNull() throws CoreException, Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/BAD_JAVA_FILE.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		MicroProfileJavaFileInfoParams params = new MicroProfileJavaFileInfoParams();
		params.setUri(javaFileUri);
		JavaFileInfo javaFileInfo = PropertiesManagerForJava.getInstance().fileInfo(params, JDT_UTILS,
				new NullProgressMonitor());
		Assert.assertNull(javaFileInfo);

	}
}
