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
package org.eclipse.lsp4mp.services.properties;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.utils.IConfigSourcePropertiesProvider;
import org.eclipse.lsp4mp.commons.utils.StringUtils;

/**
 * Adapts a list of <code>ItemMetadata</code> to <code>IConfigSourcePropertiesProvider</code>
 *
 * @author datho7561
 */
class PropertiesInfoPropertiesProvider implements IConfigSourcePropertiesProvider {

	List<ItemMetadata> properties;

	public PropertiesInfoPropertiesProvider(List<ItemMetadata> properties) {
		this.properties = properties;
	}

	@Override
	public Set<String> keys() {
		return properties.stream() //
				.filter(item -> StringUtils.hasText(item.getDefaultValue()))
				.map(item -> item.getName()) //
				.filter(Objects::nonNull) //
				.collect(Collectors.toSet());
	}

	@Override
	public boolean hasKey(String key) {
		if (key == null) {
			return false;
		}
		for (ItemMetadata item : properties) {
			if (key.equals(item.getName()) && StringUtils.hasText(item.getDefaultValue())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getValue(String key) {
		if (key == null) {
			return null;
		}
		for (ItemMetadata item : properties) {
			if (key.equals(item.getName()) && StringUtils.hasText(item.getDefaultValue())) {
				return item.getDefaultValue();
			}
		}
		return null;
	}

}