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
package org.eclipse.lsp4mp.jdt.core.faulttolerance;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaCompletion;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.c;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.fixURI;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.p;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.te;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCompletionParams;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.junit.Test;

/**
 * Tests for completion in Java files
 *
 * @author datho7561
 */
public class MicroProfileFaultToleranceJavaCompletionTest extends BasePropertiesManagerTest {

	@Test
	public void fallbackMethodCompletion() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/FaultTolerantResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());

		// fallbackMethod = "b|bb"
		assertJavaCompletion(new MicroProfileJavaCompletionParams(javaFileUri, p(21, 33)), utils, //
				c(te(21, 32, 21, 35, "hello"), "hello()", CompletionItemKind.Method), //
				c(te(21, 32, 21, 35, "bbb"), "bbb()", CompletionItemKind.Method), //
				c(te(21, 32, 21, 35, "stringMethod"), "stringMethod()", CompletionItemKind.Method), //
				c(te(21, 32, 21, 35, "ccc"), "ccc()", CompletionItemKind.Method));
	}

	@Test
	public void fallbackMethodCompletionBeginning() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/FaultTolerantResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());

		// fallbackMethod = "|bbb"
		assertJavaCompletion(new MicroProfileJavaCompletionParams(javaFileUri, p(21, 32)), utils, //
				c(te(21, 32, 21, 35, "hello"), "hello()", CompletionItemKind.Method), //
				c(te(21, 32, 21, 35, "bbb"), "bbb()", CompletionItemKind.Method), //
				c(te(21, 32, 21, 35, "stringMethod"), "stringMethod()", CompletionItemKind.Method), //
				c(te(21, 32, 21, 35, "ccc"), "ccc()", CompletionItemKind.Method));
	}


	@Test
	public void fallbackMethodNoCompletionOutside() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/FaultTolerantResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());

		// fallbackMethod = |"bbb"
		assertJavaCompletion(new MicroProfileJavaCompletionParams(javaFileUri, p(21, 31)), utils);
	}

	@Test
	public void fallbackMethodEmptyQuotes() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/OtherFaultToleranceResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());

		assertJavaCompletion(new MicroProfileJavaCompletionParams(javaFileUri, p(28, 32)), utils, //
				c(te(28, 32, 28, 32, "hello"), "hello()", CompletionItemKind.Method), //
				c(te(28, 32, 28, 32, "hi"), "hi()", CompletionItemKind.Method), //
				c(te(28, 32, 28, 32, "fourth"), "fourth()", CompletionItemKind.Method), //
				c(te(28, 32, 28, 32, "fifth"), "fifth()", CompletionItemKind.Method), //
				c(te(28, 32, 28, 32, "aaa"), "aaa()", CompletionItemKind.Method));
	}

	@Test
	public void fallbackMethodNoSpacesAroundEquals() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/OtherFaultToleranceResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());

		assertJavaCompletion(new MicroProfileJavaCompletionParams(javaFileUri, p(35, 30)), utils, //
				c(te(35, 30, 35, 30, "hello"), "hello()", CompletionItemKind.Method), //
				c(te(35, 30, 35, 30, "hi"), "hi()", CompletionItemKind.Method), //
				c(te(35, 30, 35, 30, "third"), "third()", CompletionItemKind.Method), //
				c(te(35, 30, 35, 30, "fifth"), "fifth()", CompletionItemKind.Method), //
				c(te(35, 30, 35, 30, "aaa"), "aaa()", CompletionItemKind.Method));
	}

	@Test
	public void fallbackMethodMultiline() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/OtherFaultToleranceResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());

		assertJavaCompletion(new MicroProfileJavaCompletionParams(javaFileUri, p(43, 9)), utils, //
				c(te(43, 9, 43, 9, "hello"), "hello()", CompletionItemKind.Method), //
				c(te(43, 9, 43, 9, "hi"), "hi()", CompletionItemKind.Method), //
				c(te(43, 9, 43, 9, "third"), "third()", CompletionItemKind.Method), //
				c(te(43, 9, 43, 9, "fourth"), "fourth()", CompletionItemKind.Method), //
				c(te(43, 9, 43, 9, "aaa"), "aaa()", CompletionItemKind.Method));
	}

}
