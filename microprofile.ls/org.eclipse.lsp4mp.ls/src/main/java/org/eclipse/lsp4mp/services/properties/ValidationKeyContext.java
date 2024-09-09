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
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.utils.PositionUtils;

/**
 * Validation property key context.
 */
public class ValidationKeyContext extends AbstractValidationContext {

	private Property property;

	public ValidationKeyContext(PropertiesFileValidator validator) {
		super(validator);
	}

	public Property getProperty() {
		return property;
	}

	void setProperty(Property property) {
		this.property = property;
	}

	public Diagnostic addDiagnostic(String message, DiagnosticSeverity severity) {
		return addDiagnostic(message, severity, ValidationType.unknown.name());
	}

	public Diagnostic addDiagnostic(String message, DiagnosticSeverity severity, String code) {
		Range range = PositionUtils.createRange(property.getStart(), property.getEnd(),
				getPropertiesModel().getDocument());
		return addDiagnostic(message, range, severity, code);
	}

}
