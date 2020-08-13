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
package org.eclipse.lsp4mp.commons;

/**
 * MicroProfile property definition parameters to retrieve the definition of the
 * MicroProfile property in Java class field or Java method.
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfilePropertyDefinitionParams {

	private String uri;

	private String sourceType;

	private String sourceField;

	private String sourceMethod;

	/**
	 * Returns the application.properties URI.
	 *
	 * @return the application.properties URI
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the application.properties URI
	 *
	 * @param uri the application.properties URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getSourceField() {
		return sourceField;
	}

	public void setSourceField(String sourceField) {
		this.sourceField = sourceField;
	}

	public String getSourceMethod() {
		return sourceMethod;
	}

	public void setSourceMethod(String sourceMethod) {
		this.sourceMethod = sourceMethod;
	}

}
