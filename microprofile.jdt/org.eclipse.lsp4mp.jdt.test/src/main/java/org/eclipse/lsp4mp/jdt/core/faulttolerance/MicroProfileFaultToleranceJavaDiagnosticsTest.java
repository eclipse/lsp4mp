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
package org.eclipse.lsp4mp.jdt.core.faulttolerance;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.d;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants;
import org.eclipse.lsp4mp.jdt.internal.faulttolerance.java.MicroProfileFaultToleranceErrorCode;
import org.junit.Test;

/**
 * MicroProfile Fault Tolerance definition in Java file.
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfileFaultToleranceJavaDiagnosticsTest extends BasePropertiesManagerTest {

	@Test
	public void fallbackMethodsMissing() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/FaultTolerantResource.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d = d(14, 31, 36, "The referenced fallback method 'aaa' does not exist", DiagnosticSeverity.Error,
				MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE,
				MicroProfileFaultToleranceErrorCode.FALLBACK_METHOD_DOES_NOT_EXIST);
		assertJavaDiagnostics(diagnosticsParams, utils, //
				d);
	}

	@Test
	public void fallbackMethodValidationFaultTolerant() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/OtherFaultTolerantResource.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);
		assertJavaDiagnostics(diagnosticsParams, utils);
	}

}
