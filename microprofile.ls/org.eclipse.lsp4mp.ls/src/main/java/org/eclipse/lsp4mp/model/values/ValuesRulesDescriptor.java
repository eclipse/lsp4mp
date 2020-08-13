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

/**
 * Descriptor which declares list of values rules {@link ValuesRule}.
 *
 * @author Angelo ZERR
 *
 */
public class ValuesRulesDescriptor {

	private List<ValuesDefinition> definitions;

	private List<ValuesRule> rules;

	public List<ValuesDefinition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<ValuesDefinition> definitions) {
		this.definitions = definitions;
	}

	/**
	 * Returns the values rules list.
	 *
	 * @return the values rules list.
	 */
	public List<ValuesRule> getRules() {
		return rules;
	}

	/**
	 * Set the values rules list.
	 *
	 * @param rules the values rules list.
	 */
	public void setRules(List<ValuesRule> rules) {
		this.rules = rules;
	}

}
