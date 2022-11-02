/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.internal.jaxrs.java;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.java.symbols.IJavaWorkspaceSymbolsParticipant;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsContext;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils;
import org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.jaxrs.JaxRsConstants;
import org.eclipse.lsp4mp.jdt.internal.restclient.MicroProfileRestClientConstants;

/**
 * Collects workspace symbols for JAX-RS REST endpoints.
 */
public class JaxRsWorkspaceSymbolParticipant implements IJavaWorkspaceSymbolsParticipant {

	private static final Logger LOGGER = Logger.getLogger(JaxRsWorkspaceSymbolParticipant.class.getName());

	private static final SearchPattern SEARCH_PATTERN;
	static {
		SearchPattern leftPattern = null;
		for (String annotation : JaxRsConstants.HTTP_METHOD_ANNOTATIONS) {
			if (leftPattern == null) {
				leftPattern = annotationSearchPattern(annotation);
			} else {
				leftPattern = SearchPattern.createOrPattern(leftPattern, annotationSearchPattern(annotation));
			}
		}
		SEARCH_PATTERN = leftPattern;
	}

	@Override
	public void collectSymbols(IJavaProject project, IJDTUtils utils, List<SymbolInformation> symbols, IProgressMonitor monitor) {
		String applicationPrefix = getJaxApplicationPath(project);
		SearchEngine engine = new SearchEngine();
		IJavaSearchScope scope = BasicSearchEngine.createJavaSearchScope(true, new IJavaElement[] { project },
				IJavaSearchScope.SOURCES);
		Set<IAnnotatable> annotatables = new HashSet<>();
		try {
			engine.search(SEARCH_PATTERN, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
					new SearchRequestor() {

						@Override
						public void acceptSearchMatch(SearchMatch match) throws CoreException {
							if (match.isInsideDocComment()) {
								return;
							}

							if (match.getElement() instanceof IAnnotation) {
								annotatables.add((IAnnotatable) ((IAnnotation) match.getElement()).getParent());
							} else if (match.getElement() instanceof IAnnotatable) {
								annotatables.add((IAnnotatable) match.getElement());
							}
						}

					}, null);
		} catch (CoreException | ClassCastException e) {
			LOGGER.log(Level.SEVERE,
					"While collecting symbols for project " + project.getResource().getLocationURI().toString(), e);
		}
		annotatables
				.forEach(annotatable -> collectSymbolFromAnnotatable(symbols, annotatable, applicationPrefix, utils));
	}

	private void collectSymbolFromAnnotatable(List<SymbolInformation> symbols, IAnnotatable annotatable,
			String applicationPrefix, IJDTUtils utils) {

		if (!(annotatable instanceof IMethod)) {
			return;
		}

		IMethod method = (IMethod) annotatable;
		try {
			if (!Flags.isPublic(method.getFlags())) {
				return;
			}
		} catch (JavaModelException e) {
			return;
		}

		String endpointUri = getEndpointUriFromAnnotatable(annotatable, applicationPrefix);
		String httpMethods = getHttpMethodsFromAnnotatable(annotatable);
		if (endpointUri == null || endpointUri.isEmpty() || httpMethods.isEmpty()) {
			return;
		}

		Location location = getLocationFromAnnotatable(method, utils);
		if (location == null) {
			return;
		}

		StringBuilder nameBuilder = new StringBuilder("@");
		nameBuilder.append(endpointUri);
		nameBuilder.append(": ");
		nameBuilder.append(httpMethods);

		SymbolInformation symbol = new SymbolInformation();
		symbol.setName(nameBuilder.toString());
		symbol.setKind(SymbolKind.Method);
		symbol.setLocation(location);

		symbols.add(symbol);
	}

	private static Location getLocationFromAnnotatable(IMethod method, IJDTUtils utils) {
		try {
			String uri = method.getResource().getLocationURI().toString();
			ISourceRange r = method.getNameRange();
			Range range = utils.toRange(method.getOpenable(), r.getOffset(), r.getLength());
			if (range == null) {
				return null;
			}
			return new Location(uri, range);
		} catch (JavaModelException e) {
			return null;
		}
	}

	private static String getEndpointUriFromAnnotatable(IAnnotatable annotatable, String applicationPrefix) {
		StringBuilder builder = new StringBuilder();
		IAnnotatable current = annotatable;

		while (current != null && current != (IAnnotatable) ((IJavaElement) current).getAncestor(IJavaElement.TYPE)) {
			if (hasRestClientAnnotation(current)) {
				// This is not an endpoint, it's an interface to an outside service
				return null;
			}
			prependPathSegment(builder, current);
			current = (IAnnotatable) ((IJavaElement) current).getAncestor(IJavaElement.TYPE);
		}
		if (hasRestClientAnnotation(current)) {
			return null;
		}
		if (current != null) {
			prependPathSegment(builder, current);
		}
		if (applicationPrefix != null) {
			builder.insert(0, applicationPrefix);
			if (applicationPrefix.charAt(0) != '/') {
				builder.insert(0, '/');
			}
		}
		return builder.toString();
	}

	private static void prependPathSegment(StringBuilder builder, IAnnotatable current) {
		String pathValue = null;
		try {
			pathValue = JaxRsUtils.getJaxRsPathValue(current);
		} catch (JavaModelException e) {
		}
		if (pathValue != null) {
			builder.insert(0, pathValue);
			if (pathValue.charAt(0) != '/') {
				builder.insert(0, "/");
			}
		}
	}

	private static String getHttpMethodsFromAnnotatable(IAnnotatable annotatable) {
		StringBuilder builder = new StringBuilder();
		for (String methodName : JaxRsConstants.HTTP_METHOD_ANNOTATIONS) {
			if (hasAnnotation(annotatable, methodName)) {
				if (builder.isEmpty()) {
					builder.append(methodName.substring(methodName.lastIndexOf('.') + 1));
				} else {
					builder.append("|");
					builder.append(methodName.substring(methodName.lastIndexOf('.') + 1));
				}
			}
		}
		return builder.toString();
	}

	private static final SearchPattern annotationSearchPattern(String annotationFQN) {
		return SearchPattern.createPattern(annotationFQN, IJavaSearchConstants.ANNOTATION_TYPE,
				IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH);
	}

	private static boolean hasRestClientAnnotation(IAnnotatable annotatable) {
		return hasAnnotation(annotatable, MicroProfileRestClientConstants.REGISTER_REST_CLIENT_ANNOTATION);
	}

	private static boolean hasAnnotation(IAnnotatable annotatable, String annotationSimpleName) {
		try {
			return AnnotationUtils.hasAnnotation(annotatable, annotationSimpleName);
		} catch (JavaModelException e) {
			return false;
		}
	}

	private static String getJaxApplicationPath(IJavaProject project) {
		JaxRsContext jaxContext = new JaxRsContext(project);
		try {
			String prefix = jaxContext.getApplicationPath(null);
			if (prefix == null || prefix.isEmpty()) {
				return null;
			}
			return prefix;
		} catch (CoreException e) {
			return null;
		}
	}

}
