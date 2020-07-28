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

import java.util.List;

import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.model.PropertiesModel;

/**
 * {@link ItemMetadata} provider to provide custom properties build on MicroProfile LS side.
 *
 */
public interface ItemMetadataProvider {

	/**
	 * 
	 * @param document
	 */
	void update(PropertiesModel document);

	List<ItemMetadata> getProperties();

	boolean isAvailable();
}
