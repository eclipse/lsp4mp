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
package org.eclipse.lsp4mp.services.properties.extensions;

import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.d;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.getDefaultMicroProfileProjectInfo;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.services.properties.ValidationType;
import org.junit.Test;

/**
 * Test with custom property validation.
 *
 * @author Angelo ZERR
 *
 */
public class CustomPropertyDiagnosticsTest {

	@Test
	public void customValidation() throws BadLocationException {
		String value = "foo=no-bar\n" + // don't report error for 'foo' property key + check if foo=bar
				"baz=any values"; // don't report error for 'baz' property key + support any value
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(),
				d(0, 4, 10, "Expected 'bar'", DiagnosticSeverity.Warning, ValidationType.value));
	}
}
