/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
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

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.model.PropertiesModel;

/**
 * Base class for property key/value validation context.
 */
public abstract class AbstractValidationContext {

	private final PropertiesFileValidator validator;

	private String propertyName;
	private ItemMetadata metadata;

	private PropertiesModel propertiesModel;

	public AbstractValidationContext(PropertiesFileValidator validator) {
		this.validator = validator;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public ItemMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(ItemMetadata metadata) {
		this.metadata = metadata;
	}

	public PropertiesModel getPropertiesModel() {
		return propertiesModel;
	}

	public void setPropertiesModel(PropertiesModel propertiesModel) {
		this.propertiesModel = propertiesModel;
	}

	public Diagnostic addDiagnostic(String message, Range range, DiagnosticSeverity severity, String code) {
		return validator.addDiagnostic(message, range, severity, code);
	}

}
