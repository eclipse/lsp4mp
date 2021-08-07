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
package org.eclipse.lsp4mp.jdt.core.faulttolerance.java;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaDefinitions;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.def;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.fixURI;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.p;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.r;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.junit.Test;

/**
 * MicroProfile Fault Tolerance validation in Java file.
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfileFaultToleranceJavaDefinitionTest extends BasePropertiesManagerTest {

	@Test
	public void fallbackMethodsDefinition() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/FaultTolerantResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());

		// @Fallback(fallbackMethod = "a|aa") --> no definition
		assertJavaDefinitions(p(14, 33), javaFileUri, utils);

		// @Fallback(|) --> no definition
		assertJavaDefinitions(p(35, 14), javaFileUri, utils);

		// @Fallback(fallbackMethod = "b|bb") --> public String bbb()
		assertJavaDefinitions(p(21, 33), javaFileUri, utils, //
				def(r(21, 32, 35), javaFileUri, r(26, 18, 21)));

	}

}
