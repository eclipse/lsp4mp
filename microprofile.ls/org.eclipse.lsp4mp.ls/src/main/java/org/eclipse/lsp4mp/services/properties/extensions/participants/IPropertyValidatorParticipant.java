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
package org.eclipse.lsp4mp.services.properties.extensions.participants;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4mp.services.properties.ValidationKeyContext;
import org.eclipse.lsp4mp.services.properties.ValidationValueContext;

/**
 * Property validator participant API used to support custom validation of
 * property key and value.
 */
public interface IPropertyValidatorParticipant {

	/**
	 * Validate the given property key
	 * {@link ValidationKeyContext#getPropertyName()} by adding custom LSP
	 * {@link Diagnostic} by using {@link ValidationKeyContext#addDiagnostic}.
	 * 
	 * @param context       the validation key context.
	 * @param cancelChecker the cancel checker.
	 * 
	 * @return true if the validator must override the standard validation key and
	 *         false otherwise.
	 */
	default boolean validatePropertyKey(ValidationKeyContext context, CancelChecker cancelChecker) {
		return false;
	}

	/**
	 * Validate the given property value {@link ValidationValueContext#getValue()}
	 * of the property {@link ValidationKeyContext#getPropertyName()} by adding
	 * custom LSP {@link Diagnostic} by using
	 * {@link ValidationValueContext#addDiagnostic}.
	 * 
	 * @param context       the validation value context.
	 * @param cancelChecker the cancel checker.
	 * @return true if the validator must override the standard validation value and
	 *         false otherwise.
	 */
	default boolean validatePropertyValue(ValidationValueContext context, CancelChecker cancelChecker) {
		return false;
	}
}
