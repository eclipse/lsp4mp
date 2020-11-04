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
package org.eclipse.lsp4mp.commons.metadata;

import java.util.List;

/**
 * Configuration item hint.
 *
 * @author Angelo ZERR
 *
 * @see https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html
 */
public class ItemHint extends ItemBase {

	private List<ValueHint> values;

	private List<ValueProvider> providers;

	public List<ValueHint> getValues() {
		return values;
	}

	public void setValues(List<ValueHint> values) {
		this.values = values;
	}

	public void setProviders(List<ValueProvider> providers) {
		this.providers = providers;
	}

	public List<ValueProvider> getProviders() {
		return providers;
	}

	/**
	 * Returns the value hint from the given <code>value</code> and supported
	 * converters <code>converterKinds</code> and null otherwise.
	 *
	 * @param value          the value
	 * @param converterKinds the supported converters.
	 * @return the value hint from the given <code>value</code> and supported
	 *         converters <code>converterKinds</code> and null otherwise.
	 */
	public ValueHint getValue(String value, List<ConverterKind> converterKinds) {
		if (values == null || value == null) {
			return null;
		}
		for (ValueHint valueHint : values) {
			if (converterKinds != null) {
				for (ConverterKind converterKind : converterKinds) {
					if (value.equals(valueHint.getValue(converterKind))) {
						return valueHint;
					}
				}
			} else if (value.equals(valueHint.getValue())) {
				return valueHint;
			}

		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemHint other = (ItemHint) obj;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

}
