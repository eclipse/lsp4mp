/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
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

import java.text.MessageFormat;
import java.util.Collections;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4mp.ls.commons.client.CommandKind;
import org.eclipse.lsp4mp.ls.commons.client.ConfigurationItemEdit;
import org.eclipse.lsp4mp.ls.commons.client.ConfigurationItemEditType;

/**
 * Specific code action factory for MicroProfile.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileCodeActionFactory {

	// Unassigned messages

	private static final String UNASSIGNED_EXCLUDED_SECTION = "microprofile.tools.validation.unassigned.excluded";

	private static final String UNASSIGNED_EXCLUDE_CODE_ACTION_TITLE = "Exclude ''{0}'' from property validation?";

	private static final String UNASSIGNED_EXCLUDE_COMMAND_TITLE = "Add ''{0}'' to unassigned excluded array";

	// Unknown messages

	private static final String UNKNOWN_EXCLUDED_SECTION = "microprofile.tools.validation.unknown.excluded";

	private static final String UNKNOWN_EXCLUDE_CODE_ACTION_TITLE = "Exclude ''{0}'' from unknown property validation?";

	private static final String UNKNOWN_EXCLUDE_COMMAND_TITLE = "Add ''{0}'' to unknown excluded array";

	/**
	 * Returns a code action for <code>diagnostic</code> that causes
	 * <code>item</code> to be added to
	 * <code>microprofile.tools.validation.unassigned.excluded</code> client
	 * configuration
	 *
	 * @param item       the item to add to the client configuration array
	 * @param diagnostic the diagnostic for the <code>CodeAction</code>
	 * @return a code action that causes <code>item</code> to be added to
	 *         <code>microprofile.tools.validation.unassigned.excluded</code> client
	 *         configuration
	 */
	public static CodeAction createAddToUnassignedExcludedCodeAction(String item, Diagnostic diagnostic) {
		String codeActionTitle = MessageFormat.format(UNASSIGNED_EXCLUDE_CODE_ACTION_TITLE, item);
		String commandTitle = MessageFormat.format(UNASSIGNED_EXCLUDE_COMMAND_TITLE, item);
		ConfigurationItemEditType editType = ConfigurationItemEditType.add;
		return createConfigurationUpdateCodeAction(codeActionTitle, commandTitle, UNASSIGNED_EXCLUDED_SECTION, editType,
				item, diagnostic);
	}

	/**
	 * Returns a code action for <code>diagnostic</code> that causes
	 * <code>item</code> to be added to
	 * <code>microprofile.tools.validation.unknown.excluded</code> client
	 * configuration
	 *
	 * @param item       the item to add to the client configuration array
	 * @param diagnostic the diagnostic for the <code>CodeAction</code>
	 * @return a code action that causes <code>item</code> to be added to
	 *         <code>microprofile.tools.validation.unknown.excluded</code> client
	 *         configuration
	 */
	public static CodeAction createAddToUnknownExcludedCodeAction(String item, Diagnostic diagnostic) {
		String codeActionTitle = MessageFormat.format(UNKNOWN_EXCLUDE_CODE_ACTION_TITLE, item);
		String commandTitle = MessageFormat.format(UNKNOWN_EXCLUDE_COMMAND_TITLE, item);
		ConfigurationItemEditType editType = ConfigurationItemEditType.add;
		return createConfigurationUpdateCodeAction(codeActionTitle, commandTitle, UNKNOWN_EXCLUDED_SECTION, editType,
				item, diagnostic);
	}

	private static CodeAction createConfigurationUpdateCodeAction(String codeActionTitle, String commandTitle,
			String section, ConfigurationItemEditType editType, String item, Diagnostic diagnostic) {
		CodeAction updateCodeAction = new CodeAction(codeActionTitle);

		ConfigurationItemEdit configItemEdit = new ConfigurationItemEdit(section, editType, item);

		Command command = new Command(commandTitle, CommandKind.COMMAND_CONFIGURATION_UPDATE,
				Collections.singletonList(configItemEdit));
		updateCodeAction.setCommand(command);
		updateCodeAction.setKind(CodeActionKind.QuickFix);
		updateCodeAction.setDiagnostics(Collections.singletonList(diagnostic));
		return updateCodeAction;
	}
}
