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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.extensions.ExtendedMicroProfileProjectInfo;
import org.eclipse.lsp4mp.ls.api.MicroProfilePropertyDefinitionProvider;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.settings.MicroProfileCommandCapabilities;
import org.eclipse.lsp4mp.settings.MicroProfileCompletionSettings;
import org.eclipse.lsp4mp.settings.MicroProfileFormattingSettings;
import org.eclipse.lsp4mp.settings.MicroProfileHoverSettings;
import org.eclipse.lsp4mp.settings.MicroProfileValidationSettings;

/**
 * The properties file language service.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesFileLanguageService {

	private final PropertiesFileCompletions completions;
	private final PropertiesFileSymbolsProvider symbolsProvider;
	private final PropertiesFileHover hover;
	private final PropertiesFileDefinition definition;
	private final PropertiesFileDiagnostics diagnostics;
	private final PropertiesFileFormatter formatter;
	private final PropertiesFileCodeActions codeActions;
	private final PropertiesFileDocumentHighlight documentHighlight;

	public PropertiesFileLanguageService() {
		this.completions = new PropertiesFileCompletions();
		this.symbolsProvider = new PropertiesFileSymbolsProvider();
		this.hover = new PropertiesFileHover();
		this.definition = new PropertiesFileDefinition();
		this.diagnostics = new PropertiesFileDiagnostics();
		this.formatter = new PropertiesFileFormatter();
		this.codeActions = new PropertiesFileCodeActions();
		this.documentHighlight = new PropertiesFileDocumentHighlight();
	}

	/**
	 * Returns completion list for the given position
	 *
	 * @param document           the properties model document
	 * @param position           the position where completion was triggered
	 * @param projectInfo        the MicroProfile project information
	 * @param completionSettings the completion settings
	 * @param cancelChecker      the cancel checker
	 * @return completion list for the given position
	 */
	public CompletionList doComplete(PropertiesModel document, Position position, MicroProfileProjectInfo projectInfo,
			MicroProfileCompletionSettings completionSettings, MicroProfileFormattingSettings formattingSettings,
			CancelChecker cancelChecker) {
		updateProperties(projectInfo, document);
		return completions.doComplete(document, position, projectInfo, completionSettings, formattingSettings,
				cancelChecker);
	}

	/**
	 * Returns Hover object for the currently hovered token
	 *
	 * @param document      the properties model document
	 * @param position      the hover position
	 * @param projectInfo   the MicroProfile project information
	 * @param hoverSettings the hover settings
	 * @return Hover object for the currently hovered token
	 */
	public Hover doHover(PropertiesModel document, Position position, MicroProfileProjectInfo projectInfo,
			MicroProfileHoverSettings hoverSettings) {
		updateProperties(projectInfo, document);
		return hover.doHover(document, position, projectInfo, hoverSettings);
	}

	/**
	 * Returns symbol information list for the given properties model.
	 *
	 * @param document      the properties model document
	 * @param cancelChecker the cancel checker
	 * @return symbol information list for the given properties model.
	 */
	public List<SymbolInformation> findSymbolInformations(PropertiesModel document, CancelChecker cancelChecker) {
		return symbolsProvider.findSymbolInformations(document, cancelChecker);
	}

	/**
	 * Returns document symbol list for the given properties model.
	 *
	 * @param document      the properties model document
	 * @param cancelChecker the cancel checker
	 * @return document symbol list for the given properties model.
	 */
	public List<DocumentSymbol> findDocumentSymbols(PropertiesModel document, CancelChecker cancelChecker) {
		return symbolsProvider.findDocumentSymbols(document, cancelChecker);
	}

	/**
	 * Returns as promise the Java field definition location of the property at the
	 * given <code>position</code> of the given application.properties
	 * <code>document</code>.
	 *
	 * @param document              the properties model.
	 * @param position              the position where definition was triggered
	 * @param projectInfo           the MicroProfile project info
	 * @param provider              the MicroProfile property definition provider.
	 * @param definitionLinkSupport true if {@link LocationLink} must be returned
	 *                              and false otherwise.
	 * @return as promise the Java field definition location of the property at the
	 *         given <code>position</code> of the given application.properties
	 *         <code>document</code>.
	 */
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> findDefinition(
			PropertiesModel document, Position position, MicroProfileProjectInfo projectInfo,
			MicroProfilePropertyDefinitionProvider provider, boolean definitionLinkSupport) {
		updateProperties(projectInfo, document);
		CompletableFuture<List<LocationLink>> definitionLocationLinks = definition.findDefinition(document, position,
				projectInfo, provider);
		if (definitionLinkSupport) {
			return definitionLocationLinks.thenApply((List<LocationLink> resolvedLinks) -> {
				return Either.forRight(resolvedLinks);
			});
		}
		return definitionLocationLinks.thenApply((List<LocationLink> resolvedLinks) -> {
			return Either.forLeft(resolvedLinks.stream().map((link) -> {
				return new Location(link.getTargetUri(), link.getTargetRange());
			}).collect(Collectors.toList()));
		});
	}

	/**
	 * Returns a <code>List<TextEdit></code> that formats the application.properties
	 * file represented by <code>document</code>
	 *
	 * @param document           the properties model
	 * @param formattingSettings the client's formatting settings
	 * @return a <code>List<TextEdit></code> that formats the application.properties
	 *         file represented by <code>document</code>
	 */
	public List<? extends TextEdit> doFormat(PropertiesModel document,
			MicroProfileFormattingSettings formattingSettings) {
		return formatter.format(document, formattingSettings);
	}

	/**
	 * Returns a <code>List<TextEdit></code> that formats the application.properties
	 * file represented by <code>document</code>, for the given <code>range</code>
	 *
	 * @param document           the properties model
	 * @param range              the range specifying the lines to format
	 * @param formattingSettings the client's formatting settings
	 * @return
	 */
	public List<? extends TextEdit> doRangeFormat(PropertiesModel document, Range range,
			MicroProfileFormattingSettings formattingSettings) {
		return formatter.format(document, range, formattingSettings);
	}

	/**
	 * Validate the given application.properties <code>document</code> by using the
	 * given MicroProfile properties metadata <code>projectInfo</code>.
	 *
	 * @param document           the properties model.
	 * @param projectInfo        the MicroProfile project info.
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(PropertiesModel document, MicroProfileProjectInfo projectInfo,
			MicroProfileValidationSettings validationSettings, CancelChecker cancelChecker) {
		updateProperties(projectInfo, document);
		return diagnostics.doDiagnostics(document, projectInfo, validationSettings, cancelChecker);
	}

	/**
	 * Returns code actions for the given diagnostics of the application.properties
	 * <code>document</code> by using the given MicroProfile properties metadata
	 * <code>projectInfo</code>.
	 *
	 * @param context             the code action context
	 * @param range               the range
	 * @param document            the properties model.
	 * @param projectInfo         the MicroProfile project info
	 * @param formattingSettings  the formatting settings.
	 * @param commandCapabilities the command capabilities
	 * @return the result of the code actions.
	 */
	public List<CodeAction> doCodeActions(CodeActionContext context, Range range, PropertiesModel document,
			MicroProfileProjectInfo projectInfo, MicroProfileFormattingSettings formattingSettings,
			MicroProfileCommandCapabilities commandCapabilities) {
		updateProperties(projectInfo, document);
		return codeActions.doCodeActions(context, range, document, projectInfo, formattingSettings,
				commandCapabilities);
	}

	public List<? extends DocumentHighlight> findDocumentHighlight(PropertiesModel document, Position position) {
		return documentHighlight.findDocumentHighlight(document, position);
	}

	private void updateProperties(MicroProfileProjectInfo projectInfo, PropertiesModel document) {
		if (projectInfo instanceof ExtendedMicroProfileProjectInfo) {
			((ExtendedMicroProfileProjectInfo) projectInfo).updateCustomProperties(document);
		}
	}

}
