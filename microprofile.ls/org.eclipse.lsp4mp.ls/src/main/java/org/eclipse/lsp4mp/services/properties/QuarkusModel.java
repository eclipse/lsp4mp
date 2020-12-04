/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.services.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ValueHint;

/**
 * Maintains default profiles and boolean types.
 */
public class QuarkusModel {

	private QuarkusModel() {
	}

	public static final ItemHint DEFAULT_PROFILES = createDefaultProfiles();

	public static final ItemHint BOOLEAN_ENUMS = createBooleanEnums();

	public static List<String> getDefaultProfileNames() {
		return DEFAULT_PROFILES.getValues().stream().map(ValueHint::getValue).collect(Collectors.toList());
	}

	private static ItemHint createDefaultProfiles() {
		ItemHint item = createItem("quarkus.profiles");
		item.getValues().add(createValue("dev", "Profile activated when in development mode (quarkus:dev)."));
		item.getValues().add(createValue("prod", "The default profile when not running in development or test mode."));
		item.getValues().add(createValue("test", "Profile activated when running tests."));
		return item;
	}

	private static ItemHint createItem(String name) {
		ItemHint item = new ItemHint();
		item.setName(name);
		item.setValues(new ArrayList<>());
		return item;
	}

	private static ValueHint createValue(String value, String description) {
		ValueHint valueHint = new ValueHint();
		valueHint.setValue(value);
		valueHint.setDescription(description);
		return valueHint;
	}

	private static ItemHint createBooleanEnums() {
		ItemHint item = createItem("quarkus.boolean");
		item.getValues().add(createValue("false", null));
		item.getValues().add(createValue("true", null));
		return item;
	}
}