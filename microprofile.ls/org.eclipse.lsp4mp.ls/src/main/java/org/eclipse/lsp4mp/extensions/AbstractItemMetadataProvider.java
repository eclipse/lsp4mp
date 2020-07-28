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
package org.eclipse.lsp4mp.extensions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.model.PropertiesModel;

/**
 * Abstract class for {@link ItemMetadataProvider}.
 * 
 */
public abstract class AbstractItemMetadataProvider implements ItemMetadataProvider {

	private final ExtendedMicroProfileProjectInfo projectInfo;

	private List<ItemMetadata> properties;

	public AbstractItemMetadataProvider(ExtendedMicroProfileProjectInfo projectInfo) {
		this.projectInfo = projectInfo;
		this.properties = new ArrayList<>();
	}

	protected ExtendedMicroProfileProjectInfo getProjectInfo() {
		return projectInfo;
	}

	@Override
	public List<ItemMetadata> getProperties() {
		return properties;
	}

	@Override
	public void update(PropertiesModel document) {
		getProperties().clear();
		doUpdate(document);
	}

	protected abstract void doUpdate(PropertiesModel document);
}
