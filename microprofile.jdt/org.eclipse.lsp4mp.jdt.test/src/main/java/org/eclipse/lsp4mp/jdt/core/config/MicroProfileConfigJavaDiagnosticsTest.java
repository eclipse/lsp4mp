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
package org.eclipse.lsp4mp.jdt.core.config;

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
import org.eclipse.lsp4mp.jdt.internal.metrics.MicroProfileMetricsConstants;
import org.junit.Test;

public class MicroProfileConfigJavaDiagnosticsTest extends BasePropertiesManagerTest {

	@Test
	public void improperDefaultValues() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_quickstart);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/config/DefaultValueResource.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d1 = d(8, 53, 58, "'foo' does not match the expected type of 'int'.",
				DiagnosticSeverity.Error, MicroProfileMetricsConstants.DIAGNOSTIC_SOURCE, null);

		Diagnostic d2 = d(11, 53, 58, "'bar' does not match the expected type of 'Integer'.",
				DiagnosticSeverity.Error, MicroProfileMetricsConstants.DIAGNOSTIC_SOURCE, null);

		Diagnostic d3 = d(17, 53, 58, "'128' does not match the expected type of 'byte'.",
				DiagnosticSeverity.Error, MicroProfileMetricsConstants.DIAGNOSTIC_SOURCE, null);

		assertJavaDiagnostics(diagnosticsParams, utils, //
				d1, d2, d3);
	}

}
