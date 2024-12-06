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
package org.eclipse.lsp4mp.jdt.core.health.java;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaCodeAction;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.ca;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.createCodeActionParams;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.d;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.te;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ls.core.internal.ProjectUtils;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeActionParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;
import org.eclipse.lsp4mp.commons.codeaction.MicroProfileCodeActionId;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest.MicroProfileMavenProjectName;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.health.MicroProfileHealthConstants;
import org.eclipse.lsp4mp.jdt.internal.health.java.MicroProfileHealthErrorCode;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Java diagnostics and code action for MicroProfile Health.
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfileHealthJavaDiagnosticsTest extends BasePropertiesManagerTest {

	@BeforeClass
	public static void setupTests() throws Exception {
		BasePropertiesManagerTest.loadJavaProjects(new String [] {
				"maven/" + MicroProfileMavenProjectName.microprofile_health_quickstart,
				"maven/" + MicroProfileMavenProjectName.microprofile_health_3
				});
	}

	@Test
	public void ImplementHealthCheck() throws Exception {
		IJavaProject javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.microprofile_health_quickstart);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/health/DontImplementHealthCheck.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d = d(9, 13, 37,
				"The class `org.acme.health.DontImplementHealthCheck` using the @Liveness, @Readiness or @Health annotation should implement the HealthCheck interface.",
				DiagnosticSeverity.Warning, MicroProfileHealthConstants.DIAGNOSTIC_SOURCE,
				MicroProfileHealthErrorCode.ImplementHealthCheck);
		assertJavaDiagnostics(diagnosticsParams, utils, //
				d);

		String uri = javaFile.getLocation().toFile().toURI().toString();
		MicroProfileJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
		codeActionParams.setResourceOperationSupported(true);
		codeActionParams.setCommandConfigurationUpdateSupported(true);
		assertJavaCodeAction(codeActionParams, utils, //
				ca(uri, "Let 'DontImplementHealthCheck' implement '@HealthCheck'",
						MicroProfileCodeActionId.ImplementHealthCheck, d, //
						te(2, 50, 9, 37, "\r\n\r\n" + //
								"import org.eclipse.microprofile.health.HealthCheck;\r\n" + //
								"import org.eclipse.microprofile.health.HealthCheckResponse;\r\n" + //
								"import org.eclipse.microprofile.health.Liveness;\r\n\r\n@Liveness\r\n" + //
								"@ApplicationScoped\r\n" + //
								"public class DontImplementHealthCheck implements HealthCheck")));
	}

	@Test
	public void HealthAnnotationMissing() throws Exception {
		IJavaProject javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.microprofile_health_quickstart);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/health/ImplementHealthCheck.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d = d(5, 13, 33,
                "The class `org.acme.health.ImplementHealthCheck` implementing the HealthCheck interface should use the @Liveness, @Readiness or @Health annotation.",
                DiagnosticSeverity.Warning, MicroProfileHealthConstants.DIAGNOSTIC_SOURCE,
                MicroProfileHealthErrorCode.HealthAnnotationMissing);
		assertJavaDiagnostics(diagnosticsParams, utils, //
				d);

		String uri = javaFile.getLocation().toFile().toURI().toString();
		MicroProfileJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
		assertJavaCodeAction(codeActionParams, utils, //
				ca(uri, "Insert @Health", MicroProfileCodeActionId.InsertMissingHealthAnnotation, d, //
						te(2, 0, 5, 0, "import org.eclipse.microprofile.health.Health;\r\n" + //
								"import org.eclipse.microprofile.health.HealthCheck;\r\n" + //
								"import org.eclipse.microprofile.health.HealthCheckResponse;\r\n\r\n" + //
								"@Health\r\n")),
				ca(uri, "Insert @Liveness", MicroProfileCodeActionId.InsertMissingHealthAnnotation, d, //
						te(3, 59, 5, 0, "\r\n" + //
								"import org.eclipse.microprofile.health.Liveness;\r\n\r\n" + //
								"@Liveness\r\n")), //
				ca(uri, "Insert @Readiness", MicroProfileCodeActionId.InsertMissingHealthAnnotation, d, //
						te(3, 59, 5, 0, "\r\n" + //
								"import org.eclipse.microprofile.health.Readiness;\r\n\r\n" + //
								"@Readiness\r\n")) //
		);
	}

	@Test
	public void healthAnnotationMissingv3() throws Exception {
		IJavaProject javaProject = ProjectUtils.getJavaProject(MicroProfileMavenProjectName.microprofile_health_3);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/MyLivenessCheck.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d = d(5, 13, 28,
                "The class `org.acme.MyLivenessCheck` implementing the HealthCheck interface should use the @Liveness, @Readiness or @Startup annotation.",
                DiagnosticSeverity.Warning, MicroProfileHealthConstants.DIAGNOSTIC_SOURCE,
                MicroProfileHealthErrorCode.HealthAnnotationMissing);
		assertJavaDiagnostics(diagnosticsParams, utils, //
				d);

		String uri = javaFile.getLocation().toFile().toURI().toString();
		MicroProfileJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
		assertJavaCodeAction(codeActionParams, utils, //
				ca(uri, "Insert @Liveness", MicroProfileCodeActionId.InsertMissingHealthAnnotation, d, //
						te(3, 59, 5, 0, "\n" + //
								"import org.eclipse.microprofile.health.Liveness;\n\n" + //
								"@Liveness\n")), //
				ca(uri, "Insert @Readiness", MicroProfileCodeActionId.InsertMissingHealthAnnotation, d, //
						te(3, 59, 5, 0, "\n" + //
								"import org.eclipse.microprofile.health.Readiness;\n\n" + //
								"@Readiness\n")), //
				ca(uri, "Insert @Startup", MicroProfileCodeActionId.InsertMissingHealthAnnotation, d, //
						te(3, 59, 5, 0, "\n" + //
								"import org.eclipse.microprofile.health.Startup;\n\n" + //
								"@Startup\n")) //
		);
	}

}
