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
package org.eclipse.lsp4mp.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

/**
 * Test for {@link MicroProfileExtensionSettings}
 *
 */
public class MicroProfileExtensionSettingsTest {

	@Test
	public void withoutValidationUserSettings() {
		// This test uses the src\test\resources\META-INF\lsp4mp\settings.json
		// which defines an exclusion for 'camel.*' property

		MicroProfileGeneralClientSettings clientSettings = new MicroProfileGeneralClientSettings();
		MicroProfileExtensionSettings extensionSettings = new MicroProfileExtensionSettings();
		// Here client settings has none validation settings
		assertNull(clientSettings.getValidation());

		extensionSettings.merge(clientSettings);
		assertNotNull(clientSettings.getValidation());
		assertNotNull(clientSettings.getValidation().getUnknown());
		assertNotNull(clientSettings.getValidation().getUnknown().getExcluded());
		assertEquals(1, clientSettings.getValidation().getUnknown().getExcluded().size());
		assertEquals("camel.*", clientSettings.getValidation().getUnknown().getExcluded().get(0));
	}

	@Test
	public void withValidationUserSettings() {
		// This test uses the src\test\resources\META-INF\lsp4mp\settings.json
		// which defines an exclusion for 'camel.*' property

		MicroProfileGeneralClientSettings clientSettings = new MicroProfileGeneralClientSettings();
		clientSettings.setValidation(new MicroProfileValidationSettings());
		clientSettings.getValidation().setUnknown(new MicroProfileValidationTypeSettings());
		clientSettings.getValidation().getUnknown().setExcluded(new ArrayList<>(Arrays.asList("mp.*")));

		MicroProfileExtensionSettings extensionSettings = new MicroProfileExtensionSettings();
		// Here client settings has user validation settings
		assertNotNull(clientSettings.getValidation());
		// Here client settings has just one excluded property
		assertNotNull(clientSettings.getValidation());
		assertNotNull(clientSettings.getValidation().getUnknown());
		assertNotNull(clientSettings.getValidation().getUnknown().getExcluded());
		assertEquals(1, clientSettings.getValidation().getUnknown().getExcluded().size());

		extensionSettings.merge(clientSettings);
		assertNotNull(clientSettings.getValidation());
		assertNotNull(clientSettings.getValidation().getUnknown());
		assertNotNull(clientSettings.getValidation().getUnknown().getExcluded());
		assertEquals(2, clientSettings.getValidation().getUnknown().getExcluded().size());
		assertEquals("mp.*", clientSettings.getValidation().getUnknown().getExcluded().get(0));
		assertEquals("camel.*", clientSettings.getValidation().getUnknown().getExcluded().get(1));
	}

}
