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
package org.eclipse.lsp4mp.commons.metadata;

import java.util.List;

/**
 * A hint for a value.
 * 
 * @see https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html#value-hint
 */
public class ValueHint {

	private String value;

	private String description;

	private String sourceType;

	/**
	 * Returns the value.
	 *
	 * @return the value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the converted value by using the given converter.
	 *
	 * @param converterKind the converter
	 * @return the converted value by using the given converter.
	 */
	public String getValue(ConverterKind converterKind) {
		return ConverterKind.convert(getValue(), converterKind);
	}

	/**
	 * Returns the preferred value according the given converters.
	 *
	 * @param converterKinds supported converters and null otherwise.
	 *
	 * @return the preferred value according the given converters.
	 */
	public String getPreferredValue(List<ConverterKind> converterKinds) {
		ConverterKind preferredConverter = converterKinds != null && !converterKinds.isEmpty() ? converterKinds.get(0)
				: null;
		return getValue(preferredConverter);
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValueHint other = (ValueHint) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (sourceType == null) {
			if (other.sourceType != null)
				return false;
		} else if (!sourceType.equals(other.sourceType))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
