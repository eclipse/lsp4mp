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
package org.eclipse.lsp4mp.jdt.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Tests for Yaml Utils
 *
 * @author datho7561
 */
public class YamlUtilsTests {

	@Test
	public void flattenEmptyYamlFile() {
		assertFlattenYamlKeys("");
	}

	@Test
	public void flattenOneLevelProperty() {
		assertFlattenYamlKeys("property: true", "property");
	}

	@Test
	public void flattenMultiLevelProperty() {
		assertFlattenYamlKeys( //
				"my:\n" + //
				"  interesting:\n" + //
				"    property: true", //
				"my.interesting.property");
	}

	@Test
	public void flattenMultipleProperties() {
		assertFlattenYamlKeys( //
				"my:\n" + //
				"  interesting:\n" + //
				"    property: true\n" + //
				"my:\n" + //
				"  other:\n" + //
				"    property: false\n" + //
				"three: 3", //
				"my.interesting.property", "my.other.property", "three");
	}

	@Test
	public void flattenPropertiesWithProfiles() {
		assertFlattenYamlKeys( //
				"\"%dev\":\n" + //
				"  my:\n" + //
				"    property: true\n" + //
				"\"%prod\":\n" + //
				"  my:\n" + //
				"    property: true\n", //
				"%dev.my.property", "%prod.my.property");
	}

	private static void assertFlattenYamlKeys(String yaml, String... expectedKeys) {
		Set<String> actualKeys = YamlUtils.flattenYamlMapToKeys(new Yaml().load(yaml));
		assertEquals(expectedKeys.length, actualKeys.size());
		for (String key: expectedKeys) {
			assertTrue(actualKeys.contains(key));
		}
	}

}
