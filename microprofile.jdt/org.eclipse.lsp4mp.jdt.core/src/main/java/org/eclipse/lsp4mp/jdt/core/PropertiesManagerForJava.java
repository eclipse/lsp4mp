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
package org.eclipse.lsp4mp.jdt.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.JavaFileInfo;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeActionParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeLensParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCompletionParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDefinitionParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsSettings;
import org.eclipse.lsp4mp.commons.MicroProfileJavaFileInfoParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaHoverParams;
import org.eclipse.lsp4mp.commons.MicroProfileDefinition;
import org.eclipse.lsp4mp.jdt.core.java.codelens.JavaCodeLensContext;
import org.eclipse.lsp4mp.jdt.core.java.completion.JavaCompletionContext;
import org.eclipse.lsp4mp.jdt.core.java.definition.JavaDefinitionContext;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4mp.jdt.core.java.hover.JavaHoverContext;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.core.java.JavaFeaturesRegistry;
import org.eclipse.lsp4mp.jdt.internal.core.java.codeaction.CodeActionHandler;
import org.eclipse.lsp4mp.jdt.internal.core.java.codelens.JavaCodeLensDefinition;
import org.eclipse.lsp4mp.jdt.internal.core.java.completion.JavaCompletionDefinition;
import org.eclipse.lsp4mp.jdt.internal.core.java.definition.JavaDefinitionDefinition;
import org.eclipse.lsp4mp.jdt.internal.core.java.diagnostics.JavaDiagnosticsDefinition;
import org.eclipse.lsp4mp.jdt.internal.core.java.hover.JavaHoverDefinition;

/**
 * JDT MicroProfile manager for Java files.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesManagerForJava {

	private static final PropertiesManagerForJava INSTANCE = new PropertiesManagerForJava();

	public static PropertiesManagerForJava getInstance() {
		return INSTANCE;
	}

	private final CodeActionHandler codeActionHandler;

	private PropertiesManagerForJava() {
		this.codeActionHandler = new CodeActionHandler();
	}

	/**
	 * Returns the Java file information (ex : package name) from the given file URI
	 * and null otherwise.
	 *
	 * @param params  the file information parameters.
	 * @param utils   the utilities class
	 * @param monitor the monitor
	 * @return the Java file information (ex : package name) from the given file URI
	 *         and null otherwise.
	 */
	public JavaFileInfo fileInfo(MicroProfileJavaFileInfoParams params, IJDTUtils utils, IProgressMonitor monitor) {
		String uri = params.getUri();
		final ICompilationUnit unit = utils.resolveCompilationUnit(uri);
		if (unit != null && unit.exists()) {
			JavaFileInfo fileInfo = new JavaFileInfo();
			String packageName = unit.getParent() != null ? unit.getParent().getElementName() : "";
			fileInfo.setPackageName(packageName);
			return fileInfo;
		}
		return null;
	}

	/**
	 * Returns the codeAction list according the given codeAction parameters.
	 *
	 * @param params  the codeAction parameters
	 * @param utils   the utilities class
	 * @param monitor the monitor
	 * @return the codeAction list according the given codeAction parameters.
	 * @throws JavaModelException
	 */
	public List<? extends CodeAction> codeAction(MicroProfileJavaCodeActionParams params, IJDTUtils utils,
			IProgressMonitor monitor) throws JavaModelException {
		return codeActionHandler.codeAction(params, utils, monitor);
	}

	/**
	 * Returns the codelens list according the given codelens parameters.
	 *
	 * @param params  the codelens parameters
	 * @param utils   the utilities class
	 * @param monitor the monitor
	 * @return the codelens list according the given codelens parameters.
	 * @throws JavaModelException
	 */
	public List<? extends CodeLens> codeLens(MicroProfileJavaCodeLensParams params, IJDTUtils utils,
			IProgressMonitor monitor) throws JavaModelException {
		String uri = params.getUri();
		ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);
		if (typeRoot == null) {
			return Collections.emptyList();
		}
		List<CodeLens> lenses = new ArrayList<>();
		collectCodeLens(uri, typeRoot, utils, params, lenses, monitor);
		if (monitor.isCanceled()) {
			return Collections.emptyList();
		}
		return lenses;
	}

	private void collectCodeLens(String uri, ITypeRoot typeRoot, IJDTUtils utils, MicroProfileJavaCodeLensParams params,
			List<CodeLens> lenses, IProgressMonitor monitor) {
		// Collect all adapted codeLens participant
		JavaCodeLensContext context = new JavaCodeLensContext(uri, typeRoot, utils, params);
		List<JavaCodeLensDefinition> definitions = JavaFeaturesRegistry.getInstance().getJavaCodeLensDefinitions()
				.stream().filter(definition -> definition.isAdaptedForCodeLens(context, monitor))
				.collect(Collectors.toList());
		if (definitions.isEmpty()) {
			return;
		}

		// Begin, collect, end participants
		definitions.forEach(definition -> definition.beginCodeLens(context, monitor));
		definitions.forEach(definition -> {
			List<CodeLens> collectedLenses = definition.collectCodeLens(context, monitor);
			if (collectedLenses != null && !collectedLenses.isEmpty()) {
				lenses.addAll(collectedLenses);
			}
		});
		definitions.forEach(definition -> definition.endCodeLens(context, monitor));
	}

	/**
	 * Returns the CompletionItems given the completion item params
	 *
	 * @param params  the completion item params
	 * @param utils   the IJDTUtils
	 * @param monitor the progress monitors
	 * @return the CompletionItems for the given the completion item params
	 * @throws JavaModelException
	 */
	public CompletionList completion(MicroProfileJavaCompletionParams params, IJDTUtils utils,
			IProgressMonitor monitor) throws JavaModelException {
		String uri = params.getUri();
		ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);
		if (typeRoot == null) {
			return null;
		}

		Position completionPosition = params.getPosition();
		int completionOffset = utils.toOffset(typeRoot.getBuffer(), completionPosition.getLine(),
				completionPosition.getCharacter());

		List<CompletionItem> completionItems = new ArrayList<>();
		JavaCompletionContext completionContext = new JavaCompletionContext(uri, typeRoot, utils, completionOffset);

		List<JavaCompletionDefinition> completions = JavaFeaturesRegistry.getInstance().getJavaCompletionDefinitions()
				.stream().filter(completion -> completion.isAdaptedForCompletion(completionContext, monitor))
				.collect(Collectors.toList());

		if (completions.isEmpty()) {
			return null;
		}

		completions.forEach(completion -> {
			List<? extends CompletionItem> collectedCompletionItems = completion.collectCompletionItems(completionContext,
					monitor);
			if (collectedCompletionItems != null) {
				completionItems.addAll(collectedCompletionItems);
			}
		});

		if (monitor.isCanceled()) {
			return null;
		}
		CompletionList completionList = new CompletionList();
		completionList.setItems(completionItems);
		return completionList;
	}

	/**
	 * Returns the definition list according the given definition parameters.
	 *
	 * @param params  the definition parameters
	 * @param utils   the utilities class
	 * @param monitor the monitor
	 * @return the definition list according the given definition parameters.
	 * @throws JavaModelException
	 */
	public List<MicroProfileDefinition> definition(MicroProfileJavaDefinitionParams params, IJDTUtils utils,
			IProgressMonitor monitor) throws JavaModelException {
		String uri = params.getUri();
		ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);
		if (typeRoot == null) {
			return Collections.emptyList();
		}

		Position hyperlinkedPosition = params.getPosition();
		int definitionOffset = utils.toOffset(typeRoot.getBuffer(), hyperlinkedPosition.getLine(),
				hyperlinkedPosition.getCharacter());
		IJavaElement hyperlinkedElement = getHoveredElement(typeRoot, definitionOffset);

		List<MicroProfileDefinition> locations = new ArrayList<>();
		collectDefinition(uri, typeRoot, hyperlinkedElement, utils, hyperlinkedPosition, locations, monitor);
		if (monitor.isCanceled()) {
			return Collections.emptyList();
		}
		return locations;
	}

	private void collectDefinition(String uri, ITypeRoot typeRoot, IJavaElement hyperlinkedElement, IJDTUtils utils,
			Position hyperlinkedPosition, List<MicroProfileDefinition> locations, IProgressMonitor monitor) {
		// Collect all adapted definition participant
		JavaDefinitionContext context = new JavaDefinitionContext(uri, typeRoot, utils, hyperlinkedElement,
				hyperlinkedPosition);
		List<JavaDefinitionDefinition> definitions = JavaFeaturesRegistry.getInstance().getJavaDefinitionDefinitions()
				.stream().filter(definition -> definition.isAdaptedForDefinition(context, monitor))
				.collect(Collectors.toList());
		if (definitions.isEmpty()) {
			return;
		}

		// Begin, collect, end participants
		definitions.forEach(definition -> definition.beginDefinition(context, monitor));
		definitions.forEach(definition -> {
			List<MicroProfileDefinition> collectedDefinitions = definition.collectDefinitions(context, monitor);
			if (collectedDefinitions != null && !collectedDefinitions.isEmpty()) {
				locations.addAll(collectedDefinitions);
			}
		});
		definitions.forEach(definition -> definition.endDefinition(context, monitor));
	}

	/**
	 * Returns diagnostics for the given uris list.
	 *
	 * @param params the diagnostics parameters
	 * @param utils  the utilities class
	 * @return diagnostics for the given uris list.
	 * @throws JavaModelException
	 */
	public List<PublishDiagnosticsParams> diagnostics(MicroProfileJavaDiagnosticsParams params, IJDTUtils utils,
			IProgressMonitor monitor) throws JavaModelException {
		List<String> uris = params.getUris();
		if (uris == null) {
			return Collections.emptyList();
		}
		DocumentFormat documentFormat = params.getDocumentFormat();
		List<PublishDiagnosticsParams> publishDiagnostics = new ArrayList<PublishDiagnosticsParams>();
		for (String uri : uris) {
			List<Diagnostic> diagnostics = new ArrayList<>();
			PublishDiagnosticsParams publishDiagnostic = new PublishDiagnosticsParams(uri, diagnostics);
			publishDiagnostics.add(publishDiagnostic);
			collectDiagnostics(uri, utils, documentFormat, params.getSettings(), diagnostics, monitor);
		}
		if (monitor.isCanceled()) {
			return Collections.emptyList();
		}
		return publishDiagnostics;
	}

	private void collectDiagnostics(String uri, IJDTUtils utils, DocumentFormat documentFormat, MicroProfileJavaDiagnosticsSettings settings,
			List<Diagnostic> diagnostics, IProgressMonitor monitor) {
		ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);
		if (typeRoot == null) {
			return;
		}

		// Collect all adapted diagnostics participant
		JavaDiagnosticsContext context = new JavaDiagnosticsContext(uri, typeRoot, utils, documentFormat, settings);
		List<JavaDiagnosticsDefinition> definitions = JavaFeaturesRegistry.getInstance().getJavaDiagnosticsDefinitions()
				.stream().filter(definition -> definition.isAdaptedForDiagnostics(context, monitor))
				.collect(Collectors.toList());
		if (definitions.isEmpty()) {
			return;
		}

		// Begin, collect, end participants
		definitions.forEach(definition -> definition.beginDiagnostics(context, monitor));
		definitions.forEach(definition -> {
			List<Diagnostic> collectedDiagnostics = definition.collectDiagnostics(context, monitor);
			if (collectedDiagnostics != null && !collectedDiagnostics.isEmpty()) {
				diagnostics.addAll(collectedDiagnostics);
			}
		});
		definitions.forEach(definition -> definition.endDiagnostics(context, monitor));
	}

	/**
	 * Returns the hover information according to the given <code>params</code>
	 *
	 * @param params  the hover parameters
	 * @param utils   the utilities class
	 * @param monitor the monitor
	 * @return the hover information according to the given <code>params</code>
	 * @throws JavaModelException
	 */
	public Hover hover(MicroProfileJavaHoverParams params, IJDTUtils utils, IProgressMonitor monitor)
			throws JavaModelException {
		String uri = params.getUri();
		ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);
		if (typeRoot == null) {
			return null;
		}

		Position hoverPosition = params.getPosition();
		int hoveredOffset = utils.toOffset(typeRoot.getBuffer(), hoverPosition.getLine(), hoverPosition.getCharacter());
		IJavaElement hoverElement = getHoveredElement(typeRoot, hoveredOffset);

		DocumentFormat documentFormat = params.getDocumentFormat();
		boolean surroundEqualsWithSpaces = params.isSurroundEqualsWithSpaces();
		List<Hover> hovers = new ArrayList<>();
		collectHover(uri, typeRoot, hoverElement, utils, hoverPosition, documentFormat, surroundEqualsWithSpaces, hovers, monitor);
		if (hovers.isEmpty()) {
			return null;
		}
		if (monitor.isCanceled()) {
			return null;
		}
		// TODO : aggregate the hover
		return hovers.get(0);
	}

	/**
	 * Returns the hovered element from the given <code>typeRoot</code> and
	 * <code>offset</code>. Returns null otherwise
	 *
	 * @param typeRoot the typeRoot
	 * @param offset   the offset representing the hover location
	 * @return the hovered element from the given <code>typeRoot</code> and
	 *         <code>offset</code>. Returns null otherwise
	 * @throws JavaModelException
	 */
	private IJavaElement getHoveredElement(ITypeRoot typeRoot, int offset) throws JavaModelException {
		IJavaElement hoverElement = typeRoot.getElementAt(offset);
		if (hoverElement == null) {
			return null;
		}
		if (hoverElement.getElementType() == IJavaElement.METHOD) {
			hoverElement = getHoveredMethodParameter((IMethod) hoverElement, offset);
		}
		return hoverElement;
	}

	/**
	 * Returns the parameter element from the given <code>method</code> that
	 * contains the given <code>offset</code>.
	 *
	 * Returns the given <code>method</code> if the correct parameter element cannot
	 * be found
	 *
	 * @param method the method
	 * @param offset the offset
	 * @return the parameter element from the given <code>method</code> that
	 *         contains the given <code>offset</code>
	 * @throws JavaModelException
	 */
	private IJavaElement getHoveredMethodParameter(IMethod method, int offset) throws JavaModelException {
		ILocalVariable[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			ISourceRange range = parameters[i].getSourceRange();
			int start = range.getOffset();
			int end = start + range.getLength();
			if (start <= offset && offset <= end) {
				return parameters[i];
			}
		}
		return method;
	}

	private void collectHover(String uri, ITypeRoot typeRoot, IJavaElement hoverElement, IJDTUtils utils,
			Position hoverPosition, DocumentFormat documentFormat, boolean surroundEqualsWithSpaces, List<Hover> hovers, IProgressMonitor monitor) {
		// Collect all adapted hover participant
		JavaHoverContext context = new JavaHoverContext(uri, typeRoot, utils, hoverElement, hoverPosition,
				documentFormat, surroundEqualsWithSpaces);
		List<JavaHoverDefinition> definitions = JavaFeaturesRegistry.getInstance().getJavaHoverDefinitions().stream()
				.filter(definition -> definition.isAdaptedForHover(context, monitor)).collect(Collectors.toList());
		if (definitions.isEmpty()) {
			return;
		}

		// Begin, collect, end participants
		definitions.forEach(definition -> definition.beginHover(context, monitor));
		definitions.forEach(definition -> {
			Hover hover = definition.collectHover(context, monitor);
			if (hover != null) {
				hovers.add(hover);
			}
		});
		definitions.forEach(definition -> definition.endHover(context, monitor));
	}

	/**
	 * Given the uri returns a {@link ITypeRoot}. May return null if it can not
	 * associate the uri with a Java file ot class file.
	 *
	 * @param uri
	 * @param utils   JDT LS utilities
	 * @param monitor the progress monitor
	 * @return compilation unit
	 */
	private static ITypeRoot resolveTypeRoot(String uri, IJDTUtils utils, IProgressMonitor monitor) {
		utils.waitForLifecycleJobs(monitor);
		final ICompilationUnit unit = utils.resolveCompilationUnit(uri);
		IClassFile classFile = null;
		if (unit == null) {
			classFile = utils.resolveClassFile(uri);
			if (classFile == null) {
				return null;
			}
		} else {
			if (!unit.getResource().exists() || monitor.isCanceled()) {
				return null;
			}
		}
		return unit != null ? unit : classFile;
	}

}