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

		Diagnostic d = d(14, 31, 36, "The referenced fallback method 'aaa' does not exist.", DiagnosticSeverity.Error,
				MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE,
				MicroProfileFaultToleranceErrorCode.FALLBACK_METHOD_DOES_NOT_EXIST);
		assertJavaDiagnostics(diagnosticsParams, utils, d);
	}

	@Test
	public void asynchronousNonFutureOrCompletionStage() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/AsynchronousFaultToleranceResource.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d1 = d(34, 11, 17,
				"The annotated method 'objectReturnTypeAsynchronousMethod' with @Asynchronous should return an object of type 'java.util.concurrent.Future', 'java.util.concurrent.CompletionStage'.",
				DiagnosticSeverity.Error, MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE,
				MicroProfileFaultToleranceErrorCode.FAULT_TOLERANCE_DEFINITION_EXCEPTION);

		Diagnostic d2 = d(39, 11, 15,
				"The annotated method 'noReturnTypeAsynchronousMethod' with @Asynchronous should return an object of type 'java.util.concurrent.Future', 'java.util.concurrent.CompletionStage'.",
				DiagnosticSeverity.Error, MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE,
				MicroProfileFaultToleranceErrorCode.FAULT_TOLERANCE_DEFINITION_EXCEPTION);

		Diagnostic d3 = d(44, 11, 36,
				"The annotated method 'completableFutureAsynchronousMethod' with @Asynchronous should return an object of type 'java.util.concurrent.Future', 'java.util.concurrent.CompletionStage'.",
				DiagnosticSeverity.Error, MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE,
				MicroProfileFaultToleranceErrorCode.FAULT_TOLERANCE_DEFINITION_EXCEPTION);

		assertJavaDiagnostics(diagnosticsParams, utils, d1, d2, d3);
	}

	@Test
	public void asynchronousClassNonFutureOrCompletionStage() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/AsynchronousFaultToleranceClassResource.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d1 = d(32, 11, 17,
				"The annotated method 'objectReturnTypeAsynchronousMethod' with @Asynchronous should return an object of type 'java.util.concurrent.Future', 'java.util.concurrent.CompletionStage'.",
				DiagnosticSeverity.Error, MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE,
				MicroProfileFaultToleranceErrorCode.FAULT_TOLERANCE_DEFINITION_EXCEPTION);

		Diagnostic d2 = d(36, 11, 15,
				"The annotated method 'noReturnTypeAsynchronousMethod' with @Asynchronous should return an object of type 'java.util.concurrent.Future', 'java.util.concurrent.CompletionStage'.",
				DiagnosticSeverity.Error, MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE,
				MicroProfileFaultToleranceErrorCode.FAULT_TOLERANCE_DEFINITION_EXCEPTION);

		Diagnostic d3 = d(40, 11, 36,
				"The annotated method 'completableFutureAsynchronousMethod' with @Asynchronous should return an object of type 'java.util.concurrent.Future', 'java.util.concurrent.CompletionStage'.",
				DiagnosticSeverity.Error, MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE,
				MicroProfileFaultToleranceErrorCode.FAULT_TOLERANCE_DEFINITION_EXCEPTION);

		assertJavaDiagnostics(diagnosticsParams, utils, d1, d2, d3);
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

	@Test
	public void circuitBreakerClientForValidationDelay() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject().getFile(new Path(
				"src/main/java/org/eclipse/microprofile/fault/tolerance/tck/invalidParameters/CircuitBreakerClientForValidationDelay.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d1 = d(36, 35, 37, "The value `-1` must be between `0` (inclusive) and `1` (inclusive).",
				DiagnosticSeverity.Error, MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE, null);

		Diagnostic d2 = d(41, 35, 36, "The value `2` must be between `0` (inclusive) and `1` (inclusive).",
				DiagnosticSeverity.Error, MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE, null);

		assertJavaDiagnostics(diagnosticsParams, utils, d1, d2);
	}

	@Test
	public void bulkheadClientForValidation() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject().getFile(new Path(
				"src/main/java/org/eclipse/microprofile/fault/tolerance/tck/invalidParameters/BulkheadClientForValidation.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d1 = d(34, 14, 16, "The value `-1` must be greater than or equal to `0`.", DiagnosticSeverity.Error,
				MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE, null);

		Diagnostic d2 = d(39, 20, 22, "The value `-1` must be greater than or equal to `0`.", DiagnosticSeverity.Error,
				MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE, null);

		Diagnostic d3 = d(44, 31, 33, "The value `-1` must be greater than or equal to `0`.", DiagnosticSeverity.Error,
				MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE, null);

		Diagnostic d4_a = d(49, 20, 22, "The value `-1` must be greater than or equal to `0`.",
				DiagnosticSeverity.Error, MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE, null);

		Diagnostic d4_b = d(49, 41, 43, "The value `-1` must be greater than or equal to `0`.",
				DiagnosticSeverity.Error, MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE, null);

		Diagnostic d5 = d(54, 20, 22, "The value `-1` must be greater than or equal to `0`.", DiagnosticSeverity.Error,
				MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE, null);

		Diagnostic d6 = d(59, 40, 42, "The value `-1` must be greater than or equal to `0`.", DiagnosticSeverity.Error,
				MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE, null);

		assertJavaDiagnostics(diagnosticsParams, utils, d1, d2, d3, d4_a, d4_b, d5, d6);
	}

	@Test
	public void timeoutClientForValidation() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject().getFile(new Path(
				"src/main/java/org/eclipse/microprofile/fault/tolerance/tck/invalidParameters/TimeoutClientForValidation.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d1 = d(33, 13, 15, "The value `-1` must be greater than or equal to `0`.", DiagnosticSeverity.Error,
				MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE, null);

		Diagnostic d2 = d(38, 19, 21, "The value `-1` must be greater than or equal to `0`.", DiagnosticSeverity.Error,
				MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE, null);

		assertJavaDiagnostics(diagnosticsParams, utils, d1, d2);
	}
}
