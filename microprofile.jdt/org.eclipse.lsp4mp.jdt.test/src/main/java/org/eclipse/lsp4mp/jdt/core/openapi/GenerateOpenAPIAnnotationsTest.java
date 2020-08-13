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
package org.eclipse.lsp4mp.jdt.core.openapi;

import static org.eclipse.lsp4mp.jdt.internal.core.java.MicroProfileForJavaAssert.assertJavaCodeAction;
import static org.eclipse.lsp4mp.jdt.internal.core.java.MicroProfileForJavaAssert.ca;
import static org.eclipse.lsp4mp.jdt.internal.core.java.MicroProfileForJavaAssert.createCodeActionParams;
import static org.eclipse.lsp4mp.jdt.internal.core.java.MicroProfileForJavaAssert.te;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeActionParams;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.junit.Test;

/**
 * Code action for generating MicroProfile OpenAPI annotations.
 *
 * @author Benson Ning
 *
 */
public class GenerateOpenAPIAnnotationsTest extends BasePropertiesManagerTest {

	@Test
	public void GenerateOpenAPIAnnotationsAction() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.microprofile_openapi);
		IJDTUtils utils = JDT_UTILS;

		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/openapi/NoOperationAnnotation.java"));
		String uri = javaFile.getLocation().toFile().toURI().toString();
		Diagnostic d = new Diagnostic();
		Position start = new Position(8, 23);
		d.setRange(new Range(start, start));
		MicroProfileJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);

		String newText = "\n\nimport org.eclipse.microprofile.openapi.annotations.Operation;" +
						 "\n\n@RequestScoped\n@Path(\"/systems\")\npublic class NoOperationAnnotation {" +
						 "\n\n\t@Operation(summary = \"\", description = \"\")\n\t@GET\n" +
						 "\tpublic Response getMyInformation(String hostname) {\n\t\treturn " +
						 "Response.ok(listContents()).build();\n\t}\n\n\t@Operation(summary = \"\", " +
						 "description = \"\")\n\t";
		assertJavaCodeAction(codeActionParams, utils,
				ca(uri, "Generate OpenAPI Annotations", d,
						te(6, 33, 17, 1, newText))
		);
	}

}
