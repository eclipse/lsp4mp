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
package org.eclipse.lsp4mp.commons;

import org.eclipse.lsp4j.LocationLink;

/**
 * MicroProfile definition.
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfileDefinition {

	private LocationLink location;

	private String selectPropertyName;

	/**
	 * Returns the location link.
	 * 
	 * <p>
	 * In the case of properties file like properties file or yaml files, the target
	 * range is not filled. It's the MicroProfile LS which must retrieve the proper
	 * range by using the {@link MicroProfileDefinition#getSelectPropertyName()}
	 * information.
	 * </p>
	 * 
	 * @return the location link.
	 */
	public LocationLink getLocation() {
		return location;
	}

	/**
	 * Set the location link.
	 * 
	 * <p>
	 * In the case of properties file like properties file or yaml files, the target
	 * range is not filled. It's the MicroProfile LS which must retrieve the proper
	 * range by using the {@link MicroProfileDefinition#getSelectPropertyName()}
	 * information.
	 * </p>
	 * 
	 * @param location the location link.
	 */
	public void setLocation(LocationLink location) {
		this.location = location;
	}

	/**
	 * Returns the property name to select in the case of properties or yaml files
	 * and null otherwise (Java files).
	 * 
	 * @return the property name to select in the case of properties or yaml files
	 *         and null otherwise (Java files).
	 */
	public String getSelectPropertyName() {
		return selectPropertyName;
	}

	/**
	 * Set the property name to select in the case of properties or yaml files and
	 * null otherwise (Java files).
	 * 
	 * @param selectPropertyName the property name to select in the case of
	 *                           properties or yaml files and null otherwise (Java
	 *                           files).
	 */
	public void setSelectPropertyName(String selectPropertyName) {
		this.selectPropertyName = selectPropertyName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((selectPropertyName == null) ? 0 : selectPropertyName.hashCode());
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
		MicroProfileDefinition other = (MicroProfileDefinition) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (selectPropertyName == null) {
			if (other.selectPropertyName != null)
				return false;
		} else if (!selectPropertyName.equals(other.selectPropertyName))
			return false;
		return true;
	}

}
