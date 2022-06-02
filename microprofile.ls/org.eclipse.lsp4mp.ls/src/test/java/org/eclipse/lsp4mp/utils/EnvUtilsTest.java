/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link EnvUtils}.
 *
 */
public class EnvUtilsTest {

	@Test
	public void wellFormedEnv() {
		String envVar = "ENV";
		Assert.assertTrue(EnvUtils.isEnvVariable(envVar));
	}

	@Test
	public void wellFormedEnvUnderscore() {
		String envVar = "ENV_A";
		Assert.assertTrue(EnvUtils.isEnvVariable(envVar));
	}

	@Test
	public void wellFormedEnvDigit() {
		String envVar = "ENV1";
		Assert.assertTrue(EnvUtils.isEnvVariable(envVar));
	}

	@Test
	public void malFormedEnvLowerCase() {
		String envVar = "env";
		Assert.assertFalse(EnvUtils.isEnvVariable(envVar));
	}

	@Test
	public void malFormedEnvMixedCase() {
		String envVar = "eNV";
		Assert.assertFalse(EnvUtils.isEnvVariable(envVar));
	}

}