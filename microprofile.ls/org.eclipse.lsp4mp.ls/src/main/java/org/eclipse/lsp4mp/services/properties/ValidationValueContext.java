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

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.utils.PositionUtils;

/**
 * Validation property value context.
 */
public class ValidationValueContext extends AbstractValidationContext {

	private String value;

	private int start;
	private int end;

	public ValidationValueContext(PropertiesFileValidator validator) {
		super(validator);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void addDiagnostic(String message, DiagnosticSeverity severity) {
		addDiagnostic(message, severity, ValidationType.value.name());
	}

	public void addDiagnostic(String message, DiagnosticSeverity severity, String code) {
		Range range = PositionUtils.createRange(start, end, getPropertiesModel().getDocument());
		addDiagnostic(message, range, severity, code);
	}

}
