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
package org.eclipse.lsp4mp.services.properties;

import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4mp.ls.commons.CodeActionFactory;

/**
 * Properties file validation types.
 *
 * @author Angelo ZERR
 *
 */
public enum ValidationType {

	syntax, unknown, duplicate, value, required, requiredValue, expression;

	/**
	 * Returns true if the given code matches the validation type and false
	 * otherwise.
	 *
	 * @param code the diagnostic code.
	 * @return true if the given code matches the validation type and false
	 *         otherwise.
	 */
	public boolean isValidationType(Either<String, Number> code) {
		return CodeActionFactory.isDiagnosticCode(code, name());
	}

}
