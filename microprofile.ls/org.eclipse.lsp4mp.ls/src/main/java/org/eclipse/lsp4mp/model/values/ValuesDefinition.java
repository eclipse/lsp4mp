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
package org.eclipse.lsp4mp.model.values;

import java.util.List;

import org.eclipse.lsp4mp.commons.metadata.ItemHint.ValueHint;

/**
 * Definition for values. A values definition gives the capability to share
 * values between several {@link PropertyMatcher}.
 *
 * @author Angelo ZERR
 *
 */
public class ValuesDefinition {

	private String id;

	private List<ValueHint> values;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ValueHint> getValues() {
		return values;
	}

	public void setValues(List<ValueHint> values) {
		this.values = values;
	}

}
