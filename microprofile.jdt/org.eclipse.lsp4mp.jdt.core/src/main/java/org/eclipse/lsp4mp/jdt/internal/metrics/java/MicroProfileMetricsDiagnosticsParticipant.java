/*******************************************************************************
* Copyright (c) 2020 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.metrics.java;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;
import org.eclipse.lsp4mp.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4mp.jdt.internal.metrics.java.MicroProfileMetricsErrorCode;

import static org.eclipse.lsp4mp.jdt.internal.metrics.MicroProfileMetricsConstants.METRIC_ID;
import static org.eclipse.lsp4mp.jdt.internal.metrics.MicroProfileMetricsConstants.GAUGE_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.metrics.MicroProfileMetricsConstants.REQUEST_SCOPED_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.metrics.MicroProfileMetricsConstants.SESSION_SCOPED_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.metrics.MicroProfileMetricsConstants.DEPENDENT_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.metrics.MicroProfileMetricsConstants.DIAGNOSTIC_SOURCE;

/**
 * 
 * MicroProfile Metrics Diagnostics
 * <ul>
 * <li>Diagnostic 1: display @Gauge annotation diagnostic message if the
 * underlying bean is annotated with @RequestScoped, @SessionScoped
 * or @Dependent. Suggest that @AnnotationScoped is used instead.</li>
 * </ul>
 * 
 * 
 * @author Kathryn Kodama
 * 
 * @See https://github.com/eclipse/microprofile-metrics
 */
public class MicroProfileMetricsDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

	@Override
	public boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		// Collection of diagnostics for MicroProfile Metrics is done only if
		// microprofile-metrics is on the classpath
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, METRIC_ID) != null;
	}

	@Override
	public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		ITypeRoot typeRoot = context.getTypeRoot();
		IJavaElement[] elements = typeRoot.getChildren();
		List<Diagnostic> diagnostics = new ArrayList<>();
		collectDiagnostics(elements, diagnostics, context, monitor);
		return diagnostics;
	}

	private static void collectDiagnostics(IJavaElement[] elements, List<Diagnostic> diagnostics,
			JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
		for (IJavaElement element : elements) {
			if (monitor.isCanceled()) {
				return;
			}
			if (element.getElementType() == IJavaElement.TYPE) {
				IType type = (IType) element;
				if (!type.isInterface()) {
					validateClassType(type, diagnostics, context, monitor);
				}
				continue;
			}
		}
	}

	private static void validateClassType(IType classType, List<Diagnostic> diagnostics, JavaDiagnosticsContext context,
			IProgressMonitor monitor) throws CoreException {
		String uri = context.getUri();
		IJDTUtils utils = context.getUtils();
		DocumentFormat documentFormat = context.getDocumentFormat();
		boolean hasInvalidScopeAnnotation = AnnotationUtils.hasAnnotation(classType, REQUEST_SCOPED_ANNOTATION)
				|| AnnotationUtils.hasAnnotation(classType, SESSION_SCOPED_ANNOTATION)
				|| AnnotationUtils.hasAnnotation(classType, DEPENDENT_ANNOTATION);
		// check for Gauge annotation for Diagnostic 1 only if the class has an invalid
		// scope annotation
		if (hasInvalidScopeAnnotation) {
			for (IJavaElement element : classType.getChildren()) {
				if (monitor.isCanceled()) {
					return;
				}
				if (element.getElementType() == IJavaElement.METHOD) {
					IMethod method = (IMethod) element;
					validateMethod(classType, method, diagnostics, context);
				}
			}
		}
	}

	private static void validateMethod(IType classType, IMethod method, List<Diagnostic> diagnostics,
			JavaDiagnosticsContext context) throws CoreException {
		String uri = context.getUri();
		DocumentFormat documentFormat = context.getDocumentFormat();
		boolean hasGaugeAnnotation = AnnotationUtils.hasAnnotation(method, GAUGE_ANNOTATION);

		// Diagnostic 1: display @Gauge annotation diagnostic message if
		// the underlying bean is annotated with @RequestScoped, @SessionScoped or
		// @Dependent.
		// Suggest that @AnnotationScoped is used instead.</li>
		if (hasGaugeAnnotation) {
			Range cdiBeanRange = PositionUtils.toNameRange(classType, context.getUtils());
			Diagnostic d = context.createDiagnostic(uri, createDiagnostic1Message(classType, documentFormat),
					cdiBeanRange, DIAGNOSTIC_SOURCE, MicroProfileMetricsErrorCode.ApplicationScopedAnnotationMissing);
			diagnostics.add(d);
		}
	}

	private static String createDiagnostic1Message(IType classType, DocumentFormat documentFormat) {
		StringBuilder message = new StringBuilder("The class ");
		if (DocumentFormat.Markdown.equals(documentFormat)) {
			message.append("`");
		}
		message.append(classType.getFullyQualifiedName());
		if (DocumentFormat.Markdown.equals(documentFormat)) {
			message.append("`");
		}
		message.append(
				" using the @Gauge annotation should use the @ApplicationScoped annotation. The @Gauge annotation does not"
						+ " support multiple instances of the underlying bean to be created.");
		return message.toString();
	}

}