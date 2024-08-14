/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4mp.extensions.sysenv;

import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.assertHoverMarkdown;

import org.eclipse.lsp4mp.utils.EnvUtils;
import org.junit.Test;

/**
 * Test with System/Environment variables hover in
 * 'microprofile-config.properties' file.
 *
 */
public class SysEnvHoverTest {

	@Test
	public void systemPropertyKey() throws Exception {
		String value = "user.n|ame = FOO";
		String hoverLabel = "**user.name**" + System.lineSeparator() + System.lineSeparator() + //
				" * Type: `java.lang.String`" + System.lineSeparator() + //
				" * Default: `" + System.getProperty("user.name") + "`" + System.lineSeparator() + //
				" * Value: `FOO`" + System.lineSeparator() + //
				" * Extension: `System property`";
		assertHoverMarkdown(value, hoverLabel, 0);
	}

	@Test
	public void systemPropertyExpression() throws Exception {
		String value = "foo = ${user|.name}";
		String hoverLabel = System.getProperty("user.name");
		assertHoverMarkdown(value, hoverLabel, 6);
	}

	@Test
	public void environmentVariableKey() throws Exception {
		String value = "PA|TH = FOO";
		String hoverLabel = "**" + (EnvUtils.isWindows ? "Path" : "PATH") + "**" + System.lineSeparator()
				+ System.lineSeparator() + //
				" * Type: `java.lang.String`" + System.lineSeparator() + //
				" * Default: `" + System.getenv("PATH") + "`" + System.lineSeparator() + //
				" * Value: `FOO`" + System.lineSeparator() + //
				" * Extension: `Environment variable`";
		assertHoverMarkdown(value, hoverLabel, 0);

		if (EnvUtils.isWindows) {
			value = "Pa|TH = FOO";
			hoverLabel = "**Path**" + System.lineSeparator() + System.lineSeparator() + //
					" * Type: `java.lang.String`" + System.lineSeparator() + //
					" * Default: `" + System.getenv("PATH") + "`" + System.lineSeparator() + //
					" * Value: `FOO`" + System.lineSeparator() + //
					" * Extension: `Environment variable`";
			assertHoverMarkdown(value, hoverLabel, 0);
		}
	}

	@Test
	public void environmentVariableExpression() throws Exception {
		String value = "foo = ${PA|TH}";
		String hoverLabel = System.getenv("PATH");
		assertHoverMarkdown(value, hoverLabel, 6);
	}
}
