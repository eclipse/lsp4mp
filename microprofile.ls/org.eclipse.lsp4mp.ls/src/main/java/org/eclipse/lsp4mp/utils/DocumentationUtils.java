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
package org.eclipse.lsp4mp.utils;

import static org.eclipse.lsp4mp.utils.PropertiesFileUtils.formatPropertyForMarkdown;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ValueHint;

/**
 * Utility for documentation.
 *
 */
public class DocumentationUtils {

	private DocumentationUtils() {

	}

	/**
	 * Returns the documentation of the given MicroProfile property.
	 *
	 * @param item     the MicroProfile property.
	 * @param profile  the profile
	 * @param value    the value of the property, or null if it is not known
	 * @param markdown true if documentation must be formatted as markdown and false
	 *                 otherwise.
	 * @return the documentation of the given MicroProfile property.
	 */
	public static MarkupContent getDocumentation(ItemMetadata item, String profile, String value, boolean markdown) {

		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("**");
		}
		documentation.append(markdown ? formatPropertyForMarkdown(item.getName()) : item.getName());
		if (markdown) {
			documentation.append("**");
		}
		documentation.append(System.lineSeparator());

		// Description
		String description = item.getDescription();
		if (description != null) {
			documentation.append(System.lineSeparator());
			documentation.append(description);
			documentation.append(System.lineSeparator());
		}

		// Profile
		addParameter("Profile", profile, documentation, markdown);

		// Type
		addParameter("Type", item.getType(), documentation, markdown);

		// Default value
		addParameter("Default", item.getDefaultValue(), documentation, markdown);

		// Value
		addParameter("Value", value, documentation, markdown);

		// Config Phase
		addParameter("Phase", getPhaseLabel(item.getPhase()), documentation, markdown);

		// Extension name
		addParameter("Extension", item.getExtensionName(), documentation, markdown);

		return new MarkupContent(markdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, documentation.toString());
	}

	/**
	 * Returns the documentation for the given unrecognized MicroProfile property.
	 *
	 * @param profile  the profile
	 * @param key      the property key
	 * @param value    the resolved property value
	 * @param markdown true if the client supports rendering markdown, false
	 *                 otherwise
	 * @return the documentation for the given unrecognized MicroProfile property
	 */
	public static MarkupContent getDocumentation(String profile, String key, String value, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("**");
		}
		documentation.append(markdown ? formatPropertyForMarkdown(key) : key);
		if (markdown) {
			documentation.append("**");
		}
		documentation.append(System.lineSeparator());

		// Profile
		addParameter("Profile", profile, documentation, markdown);

		// Value
		addParameter("Value", value, documentation, markdown);

		return new MarkupContent(markdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, documentation.toString());
	}

	private static String getPhaseLabel(int phase) {
		switch (phase) {
		case ItemMetadata.CONFIG_PHASE_BUILD_TIME:
			return "buildtime";
		case ItemMetadata.CONFIG_PHASE_RUN_TIME:
			return "runtime";
		case ItemMetadata.CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED:
			return "buildtime & runtime";
		case ItemMetadata.CONFIG_PHASE_BOOTSTRAP:
			return "bootstrap";
		default:
			return null;
		}
	}

	private static void addParameter(String name, String value, StringBuilder documentation, boolean markdown) {
		if (value != null) {
			documentation.append(System.lineSeparator());
			if (markdown) {
				documentation.append(" * ");
			}
			documentation.append(name);
			documentation.append(": ");
			if (markdown) {
				documentation.append("`");
			}
			documentation.append(value);
			if (markdown) {
				documentation.append("`");
			}
		}
	}

	/**
	 * Returns the documentation of the given enumeration item.
	 *
	 * @param item     the enumeration item
	 * @param markdown true if documentation must be formatted as markdown and false
	 *                 otherwise.
	 * @return the documentation of the given enumeration item.
	 */
	public static MarkupContent getDocumentation(ValueHint item, boolean markdown) {

		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("**");
		}
		documentation.append(markdown ? formatPropertyForMarkdown(item.getValue()) : item.getValue());
		if (markdown) {
			documentation.append("**");
		}
		documentation.append(System.lineSeparator());

		// Javadoc
		String javaDoc = item.getDescription();
		if (javaDoc != null) {
			documentation.append(System.lineSeparator());
			documentation.append(javaDoc);
			documentation.append(System.lineSeparator());
		}

		return new MarkupContent(markdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, documentation.toString());
	}

	/**
	 * Returns the documentation content of <code>documentation</code>. Returns null
	 * if documentation content does not exist.
	 *
	 * @param documentation contains documentation content
	 * @return the documentation content of <code>documentation</code>.
	 */
	public static String getDocumentationTextFromEither(Either<String, MarkupContent> documentation) {

		if (documentation == null) {
			return null;
		}

		if (documentation.isRight()) {
			return documentation.getRight().getValue();
		}

		return documentation.getLeft();
	}
}
