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

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4mp.services.properties.ValidationKeyContext;
import org.eclipse.lsp4mp.services.properties.ValidationValueContext;
import org.eclipse.lsp4mp.services.properties.extensions.participants.IPropertyValidatorParticipant;

public class CustomPropertyValidatorParticipant implements IPropertyValidatorParticipant {

	@Override
	public boolean validatePropertyKey(ValidationKeyContext context, CancelChecker cancelChecker) {
		if (!("foo".equals(context.getPropertyName()) || "baz".equals(context.getPropertyName()))) {
			return false;
		}
		// Don't validate the property key for foo and baz
		return true;
	}

	@Override
	public boolean validatePropertyValue(ValidationValueContext context, CancelChecker cancelChecker) {
		if ("foo".equals(context.getPropertyName())) {
			// foo expects bar as value
			if (!"bar".equals(context.getValue())) {
				context.addDiagnostic("Expected 'bar'", DiagnosticSeverity.Warning);
			}
			return true;
		}
		if ("baz".equals(context.getPropertyName())) {
			// baz can have any value
			return true;
		}
		return false;
	}
}
