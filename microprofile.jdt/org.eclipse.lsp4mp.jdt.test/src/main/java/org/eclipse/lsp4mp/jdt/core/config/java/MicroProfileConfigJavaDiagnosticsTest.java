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
package org.eclipse.lsp4mp.jdt.core.config.java;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaCodeAction;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.ca;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.createCodeActionParams;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.d;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.te;
import static org.eclipse.lsp4mp.jdt.internal.config.java.MicroProfileConfigASTValidator.setDataForUnassigned;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeActionParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsSettings;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.config.java.MicroProfileConfigErrorCode;
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

		Diagnostic d1 = d(8, 53, 58, "'foo' does not match the expected type of 'int'.", DiagnosticSeverity.Error,
				MicroProfileConfigConstants.MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE,
				MicroProfileConfigErrorCode.DEFAULT_VALUE_IS_WRONG_TYPE);

		Diagnostic d2 = d(11, 53, 58, "'bar' does not match the expected type of 'Integer'.", DiagnosticSeverity.Error,
				MicroProfileConfigConstants.MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE,
				MicroProfileConfigErrorCode.DEFAULT_VALUE_IS_WRONG_TYPE);

		Diagnostic d3 = d(17, 53, 58, "'128' does not match the expected type of 'byte'.", DiagnosticSeverity.Error,
				MicroProfileConfigConstants.MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE,
				MicroProfileConfigErrorCode.DEFAULT_VALUE_IS_WRONG_TYPE);
		Diagnostic d4 = d(32, 27, 38,
				"The property 'greeting9' is not assigned a value in any config file, and must be assigned at runtime.",
				DiagnosticSeverity.Warning, MicroProfileConfigConstants.MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE,
				MicroProfileConfigErrorCode.NO_VALUE_ASSIGNED_TO_PROPERTY);
		setDataForUnassigned("greeting9", d4);

		assertJavaDiagnostics(diagnosticsParams, utils, //
				d1, d2, d3, d4);
	}

	@Test
	public void noValueAssignedWithIgnore() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_quickstart);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/config/DefaultValueResource.java"));
		diagnosticsParams.setSettings(new MicroProfileJavaDiagnosticsSettings(Arrays.asList("greeting?")));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d1 = d(8, 53, 58, "'foo' does not match the expected type of 'int'.", DiagnosticSeverity.Error,
				MicroProfileConfigConstants.MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE,
				MicroProfileConfigErrorCode.DEFAULT_VALUE_IS_WRONG_TYPE);

		Diagnostic d2 = d(11, 53, 58, "'bar' does not match the expected type of 'Integer'.", DiagnosticSeverity.Error,
				MicroProfileConfigConstants.MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE,
				MicroProfileConfigErrorCode.DEFAULT_VALUE_IS_WRONG_TYPE);

		Diagnostic d3 = d(17, 53, 58, "'128' does not match the expected type of 'byte'.", DiagnosticSeverity.Error,
				MicroProfileConfigConstants.MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE,
				MicroProfileConfigErrorCode.DEFAULT_VALUE_IS_WRONG_TYPE);

		assertJavaDiagnostics(diagnosticsParams, utils, //
				d1, d2, d3);
	}

	@Test
	public void unassignedWithConfigProperties() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_configproperties);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/Details.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d = d(13, 32, 46,
				"The property 'server.old.location' is not assigned a value in any config file, and must be assigned at runtime.",
				DiagnosticSeverity.Warning, MicroProfileConfigConstants.MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE,
				MicroProfileConfigErrorCode.NO_VALUE_ASSIGNED_TO_PROPERTY);
		setDataForUnassigned("server.old.location", d);

		assertJavaDiagnostics(diagnosticsParams, utils, d);
	}

	@Test
	public void codeActionForUnassigned() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_quickstart);
		IJDTUtils utils = JDT_UTILS;

		IFile propertiesFile = javaProject.getProject()
				.getFile(new Path("src/main/resources/META-INF/microprofile-config.properties"));

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/config/UnassignedValue.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d1 = d(8, 24, 29,
				"The property 'foo' is not assigned a value in any config file, and must be assigned at runtime.",
				DiagnosticSeverity.Warning, MicroProfileConfigConstants.MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE,
				MicroProfileConfigErrorCode.NO_VALUE_ASSIGNED_TO_PROPERTY);
		setDataForUnassigned("foo", d1);
		Diagnostic d2 = d(14, 25, 30,
				"The property 'server.url' is not assigned a value in any config file, and must be assigned at runtime.",
				DiagnosticSeverity.Warning, MicroProfileConfigConstants.MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE,
				MicroProfileConfigErrorCode.NO_VALUE_ASSIGNED_TO_PROPERTY);
		setDataForUnassigned("server.url", d2);

		assertJavaDiagnostics(diagnosticsParams, utils, d1, d2);

		String javaUri = fixURI(javaFile.getLocation().toFile().toURI().toString());
		String propertiesUri = fixURI(propertiesFile.getLocation().toFile().toURI().toString());
		MicroProfileJavaCodeActionParams codeActionParams1 = createCodeActionParams(javaUri, d1);
		assertJavaCodeAction(codeActionParams1, utils, //
				ca(javaUri, "Insert 'defaultValue' attribute", d1, //
						te(8, 29, 8, 29, ", defaultValue = \"\"")),
				ca(propertiesUri, "Insert 'foo' property in 'META-INF/microprofile-config.properties'", d1, //
						te(0, 0, 0, 0, "foo=\r\n")));

		MicroProfileJavaCodeActionParams codeActionParams2 = createCodeActionParams(javaUri, d2);
		assertJavaCodeAction(codeActionParams2, utils, //
				ca(javaUri, "Insert 'defaultValue' attribute", d1, //
						te(14, 30, 14, 30, ", defaultValue = \"\"")),
				ca(propertiesUri, "Insert 'server.url' property in 'META-INF/microprofile-config.properties'", d2, //
						te(0, 0, 0, 0, "server.url=\r\n")));

	}

	private static String fixURI(String uriString) {
		return uriString.replaceFirst("file:/([^/])", "file:///$1");
	}
}
