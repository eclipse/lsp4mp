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
package org.eclipse.lsp4mp.jdt.internal.faulttolerance.java;

import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.FALLBACK_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants.FALLBACK_METHOD_FALLBACK_ANNOTATION_MEMBER;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4mp.jdt.core.java.completion.IJavaCompletionParticipant;
import org.eclipse.lsp4mp.jdt.core.java.completion.JavaCompletionContext;
import org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Completion for <code>fallbackMethod</code>
 *
 * @author datho7561
 */
public class MicroProfileFaultToleranceCompletionParticipant implements IJavaCompletionParticipant {

	private static final Pattern FALLBACK_METHOD_KEY_VALUE_PATTERN = Pattern
			.compile(FALLBACK_METHOD_FALLBACK_ANNOTATION_MEMBER + "\\s*=\\s*\"([^\"]*)\"");

	private static final String FALLBACK_ANNOTATION_SHORT_NAME = FALLBACK_ANNOTATION
			.substring(FALLBACK_ANNOTATION.lastIndexOf('.') + 1);

	@Override
	public boolean isAdaptedForCompletion(JavaCompletionContext context, IProgressMonitor monitor)
			throws CoreException {
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, FALLBACK_ANNOTATION) != null;
	}

	@Override
	public List<? extends CompletionItem> collectCompletionItems(JavaCompletionContext context,
			IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return null;
		}
		IAnnotation fallbackAnnotation = getFallbackAnnotation(context.getTypeRoot(), context.getOffset());
		if (fallbackAnnotation == null) {
			return null;
		}
		Range range = getCompletionReplaceRange(fallbackAnnotation, context.getUtils(), context.getOffset());
		if (range == null) {
			return null;
		}
		List<CompletionItem> completionItems = new ArrayList<>();
		for (IMethod method : context.getTypeRoot().findPrimaryType().getMethods()) {
			completionItems.add(makeMethodCompletionItem(method.getElementName(), range));
		}
		return completionItems;
	}

	/**
	 * Returns the <code>@Fallback</code> annotation as an IAnnotation or null if
	 * the offset is not in a <code>@Fallback</code> annotation
	 *
	 * @param typeRoot the type root of the class that is being checked for an
	 *                 annotation
	 * @param offset   the offset at which completion is triggered
	 * @return the <code>@Fallback</code> annotation as an IAnnotation or null if
	 *         the offset is not in a <code>@Fallback</code> annotation
	 * @throws JavaModelException if this error is thrown when accessing the model
	 */
	private static IAnnotation getFallbackAnnotation(ITypeRoot typeRoot, int offset) throws JavaModelException {
		IJavaElement element = typeRoot.getElementAt(offset);
		if (element.getElementType() != IJavaElement.METHOD) {
			return null;
		}
		IAnnotatable annotatable = (IAnnotatable) element;
		IAnnotation annotation = annotatable.getAnnotation(FALLBACK_ANNOTATION_SHORT_NAME);
		if (!AnnotationUtils.isMatchAnnotation(annotation, FALLBACK_ANNOTATION)) {
			return null;
		}
		return annotation;
	}

	/**
	 * Returns the range that should be replaced for completion, or null otherwise
	 *
	 * @param fallbackAnnotation the fallback annotation
	 * @param utils              the IJDTUtils
	 * @param triggerOffset      the offset in the document where completion was
	 *                           triggered
	 * @return the range that should be replaced for completion, or null otherwise
	 * @throws JavaModelException
	 */
	private static Range getCompletionReplaceRange(IAnnotation fallbackAnnotation, IJDTUtils utils, int triggerOffset)
			throws JavaModelException {
		ISourceRange range = fallbackAnnotation.getSourceRange();
		if (!SourceRange.isAvailable(range)) {
			return null;
		}
		int annotationStart = range.getOffset();
		String annotationSrc = fallbackAnnotation.getSource();
		Matcher m = FALLBACK_METHOD_KEY_VALUE_PATTERN.matcher(annotationSrc);
		if (!m.find()) {
			return null;
		}
		if (m.start(1) == -1 || m.end(1) == -1) {
			return null;
		}
		int start = m.start(1) + annotationStart;
		int length = m.end(1) - m.start(1);
		if (triggerOffset < start || start + length < triggerOffset) {
			return null;
		}
		return utils.toRange(fallbackAnnotation.getOpenable(), start, length);
	}

	/**
	 * Returns the method completion item given the name of the method
	 *
	 * @param methodName   the name of the method
	 * @param replaceRange the range in the document that should be replaced with
	 *                     the method name
	 * @return the method completion item given the name of the method
	 */
	private static CompletionItem makeMethodCompletionItem(String methodName, Range replaceRange) {
		CompletionItem completionItem = new CompletionItem();
		TextEdit textEdit = new TextEdit(replaceRange, methodName);
		completionItem.setTextEdit(Either.forLeft(textEdit));
		completionItem.setKind(CompletionItemKind.Method);
		completionItem.setLabel(methodName + "()");
		return completionItem;
	}

}
