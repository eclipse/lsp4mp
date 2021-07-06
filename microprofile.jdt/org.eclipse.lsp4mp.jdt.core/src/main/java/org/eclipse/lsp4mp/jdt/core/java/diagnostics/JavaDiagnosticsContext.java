/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.core.java.diagnostics;

import java.util.Collections;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsSettings;
import org.eclipse.lsp4mp.jdt.core.java.AbtractJavaContext;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;

/**
 * Java diagnostics context for a given compilation unit.
 *
 * @author Angelo ZERR
 *
 */
public class JavaDiagnosticsContext extends AbtractJavaContext {

	private final DocumentFormat documentFormat;

	private final MicroProfileJavaDiagnosticsSettings settings;

	public JavaDiagnosticsContext(String uri, ITypeRoot typeRoot, IJDTUtils utils, DocumentFormat documentFormat, MicroProfileJavaDiagnosticsSettings settings) {
		super(uri, typeRoot, utils);
		this.documentFormat = documentFormat;
		if (settings == null) {
			this.settings = new MicroProfileJavaDiagnosticsSettings(Collections.emptyList());
		} else {
			this.settings = settings;
		}
	}

	public DocumentFormat getDocumentFormat() {
		return documentFormat;
	}

	/**
	 * Returns the MicroProfileJavaDiagnosticsSettings.
	 *
	 * Should not be null.
	 *
	 * @return the MicroProfileJavaDiagnosticsSettings
	 */
	public MicroProfileJavaDiagnosticsSettings getSettings() {
		return this.settings;
	}

	public Diagnostic createDiagnostic(String uri, String message, Range range, String source, IJavaErrorCode code) {
		Diagnostic diagnostic = new Diagnostic();
		diagnostic.setSource(source);
		diagnostic.setMessage(message);
		diagnostic.setSeverity(DiagnosticSeverity.Warning);
		diagnostic.setRange(range);
		if (code != null) {
			diagnostic.setCode(code.getCode());
		}
		return diagnostic;
	}

}
