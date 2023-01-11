/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.internal.core.ls;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Tests for {@link ArgumentUtils}.
 */
public class ArgumentUtilsTest {

	private Map<String, Object> testMap;

	@Before
	public void setup() {
		testMap = new HashMap<>();
		HashMap<String, Object> childMap = new HashMap<>();
		childMap.put("key", "value");
		testMap.put("child", childMap);
	}

	@Test
	public void testGetObjectAsJson() {
		JsonObject obj = ArgumentUtils.getObjectAsJson(testMap, "child");
		Assert.assertEquals(obj.keySet().size(), 1);
		Assert.assertEquals(obj.get("key"), new JsonPrimitive("value"));
	}

	@Test
	public void testGetObject() {
		Map<String, Object> obj = ArgumentUtils.getObject(testMap, "child");
		Assert.assertEquals(obj.keySet().size(), 1);
		Assert.assertEquals(obj.get("key"), "value");
	}

}
