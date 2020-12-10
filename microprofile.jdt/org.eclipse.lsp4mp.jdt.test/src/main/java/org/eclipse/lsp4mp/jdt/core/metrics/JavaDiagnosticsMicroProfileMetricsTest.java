/*******************************************************************************
* Copyright (c) 2020 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.core.metrics;

import static org.eclipse.lsp4mp.jdt.internal.core.java.MicroProfileForJavaAssert.assertJavaCodeAction;
import static org.eclipse.lsp4mp.jdt.internal.core.java.MicroProfileForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4mp.jdt.internal.core.java.MicroProfileForJavaAssert.ca;
import static org.eclipse.lsp4mp.jdt.internal.core.java.MicroProfileForJavaAssert.createCodeActionParams;
import static org.eclipse.lsp4mp.jdt.internal.core.java.MicroProfileForJavaAssert.d;
import static org.eclipse.lsp4mp.jdt.internal.core.java.MicroProfileForJavaAssert.te;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeActionParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.metrics.MicroProfileMetricsConstants;
import org.eclipse.lsp4mp.jdt.internal.metrics.java.MicroProfileMetricsErrorCode;
import org.junit.Test;

/**
 * Java diagnostics and code action for MicroProfile Metrics.
 * 
 * @author Kathryn Kodama
 */
public class JavaDiagnosticsMicroProfileMetricsTest extends BasePropertiesManagerTest {

	@Test
	public void ApplicationScopedAnnotationMissing() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_metrics);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/IncorrectScope.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		// check for MicroProfile metrics diagnostic warning
		Diagnostic d = d(10, 13, 27,
				"The class `org.acme.IncorrectScope` using the @Gauge annotation should use the @ApplicationScoped annotation." + 
				" The @Gauge annotation does not support multiple instances of the underlying bean to be created.",
				DiagnosticSeverity.Warning, MicroProfileMetricsConstants.DIAGNOSTIC_SOURCE,
				MicroProfileMetricsErrorCode.ApplicationScopedAnnotationMissing);
		assertJavaDiagnostics(diagnosticsParams, utils, d);

		String uri = javaFile.getLocation().toFile().toURI().toString();
		MicroProfileJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
		// check for MicroProfile metrics quick fix code action associated with diagnostic warning
		assertJavaCodeAction(codeActionParams, utils, //
			ca(uri, "Replace current scope with @ApplicationScoped", d, //
				te(4, 57, 9, 0, "\n\nimport javax.enterprise.context.ApplicationScoped;\n" + //
					"import javax.enterprise.context.RequestScoped;\n\n" + //
					"@ApplicationScoped\n")));
	}
	
}