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
package org.eclipse.lsp4mp.commons;

/**
 * MicroProfile property documentation parameters to retrieve the documentation
 * of the MicroProfile property in Java class field or Java method.
 */
public class MicroProfilePropertyDocumentationParams {

	private String uri;

	private String sourceType;

	private String sourceField;

	private String sourceMethod;

	private DocumentFormat documentFormat;

	/**
	 * Returns the properties file URI.
	 *
	 * @return the properties file URI
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the properties file URI
	 *
	 * @param uri the properties file URI
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

	/**
	 * Returns the document format for this property documentation request.
	 *
	 * @return the document format for this property documentation request
	 */
	public DocumentFormat getDocumentFormat() {
		return documentFormat;
	}

	/**
	 * Sets which documentation format the documentation should be returned as.
	 *
	 * @param documentFormat the document format that the documentation should be
	 *                       returned as
	 */
	public void setDocumentFormat(DocumentFormat documentFormat) {
		this.documentFormat = documentFormat;
	}

}
