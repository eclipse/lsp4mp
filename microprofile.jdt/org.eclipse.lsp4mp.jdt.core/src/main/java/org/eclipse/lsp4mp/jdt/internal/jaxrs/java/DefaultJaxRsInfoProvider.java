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

import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsConstants.JAKARTA_WS_RS_PATH_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsConstants.JAVAX_WS_RS_PATH_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils.getJaxRsPathValue;
import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils.isJaxRsRequestMethod;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.hasAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.overlaps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.lsp4mp.jdt.core.jaxrs.HttpMethod;
import org.eclipse.lsp4mp.jdt.core.jaxrs.IJaxRsInfoProvider;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsConstants;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsContext;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsMethodInfo;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Locates JAX-RS methods in a project or class file using the default semantics.
 */
public class DefaultJaxRsInfoProvider implements IJaxRsInfoProvider {

	private static final Logger LOGGER = Logger.getLogger(DefaultJaxRsInfoProvider.class.getName());

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
	public boolean canProvideJaxRsMethodInfoForClass(ITypeRoot typeRoot, IProgressMonitor monitor) {
		IJavaProject javaProject = typeRoot.getJavaProject();
		return JDTTypeUtils.findType(javaProject, JAVAX_WS_RS_PATH_ANNOTATION) != null
				|| JDTTypeUtils.findType(javaProject, JAKARTA_WS_RS_PATH_ANNOTATION) != null;

	}

	@Override
	public Set<ITypeRoot> getAllJaxRsClasses(IJavaProject javaProject, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return Collections.emptySet();
		}
		Set<ITypeRoot> jaxRsClasses = new HashSet<>();
		SearchEngine engine = new SearchEngine();
		IJavaSearchScope scope = BasicSearchEngine.createJavaSearchScope(true, new IJavaElement[] { javaProject },
				IJavaSearchScope.SOURCES);

		try {
			engine.search(SEARCH_PATTERN, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
					new SearchRequestor() {

						@Override
						public void acceptSearchMatch(SearchMatch match) throws CoreException {
							if (match.isInsideDocComment()) {
								return;
							}

							if (match.getElement() instanceof IJavaElement element) {
								jaxRsClasses.add(getTypeRoot(element));
							}
						}

					}, monitor);
		} catch (CoreException | ClassCastException e) {
			LOGGER.log(Level.SEVERE, "While collecting JAX-RS method information for project "
					+ javaProject.getResource().getLocationURI().toString(), e);
		}
		if (monitor.isCanceled()) {
			return Collections.emptySet();
		}
		return jaxRsClasses;
	}

	@Override
	public List<JaxRsMethodInfo> getJaxRsMethodInfo(ITypeRoot typeRoot, JaxRsContext jaxrsContext, IJDTUtils utils,
			IProgressMonitor monitor) {
		List<JaxRsMethodInfo> methodInfos = new ArrayList<>();
		try {
			collectJaxRsMethodInfo(typeRoot.getChildren(), null, methodInfos, jaxrsContext, utils, monitor);
		} catch (CoreException e) {
			LOGGER.log(Level.SEVERE, "while collecting JAX-RS method info using the default method", e);
		}
		return methodInfos;
	}

	private static void collectJaxRsMethodInfo(IJavaElement[] elements, String rootPath,
			Collection<JaxRsMethodInfo> jaxRsMethodsInfo, JaxRsContext jaxrsContext, IJDTUtils utils,
			IProgressMonitor monitor) throws JavaModelException, CoreException {

		for (IJavaElement element : elements) {
			if (monitor.isCanceled()) {
				return;
			}
			if (element.getElementType() == IJavaElement.TYPE) {
				IType type = (IType) element;
				// Get value of JAX-RS @Path annotation from the class
				String pathValue = getJaxRsPathValue(type);
				if (pathValue != null) {
					// Class is annotated with @Path
					// Loop for each method annotated with @Path to generate
					// URL code lens per
					// method.
					collectJaxRsMethodInfo(type.getChildren(), pathValue, jaxRsMethodsInfo, jaxrsContext, utils,
							monitor);
				}
				continue;
			} else if (element.getElementType() == IJavaElement.METHOD) {
				if (utils.isHiddenGeneratedElement(element)) {
					continue;
				}
				// ignore element if method range overlaps the type range,
				// happens for generated
				// bytecode, i.e. with lombok
				IJavaElement parentType = element.getAncestor(IJavaElement.TYPE);
				if (parentType != null && overlaps(((ISourceReference) parentType).getNameRange(),
						((ISourceReference) element).getNameRange())) {
					continue;
				}
			} else {// neither a type nor a method, we bail
				continue;
			}

			// Here java element is a method
			if (rootPath != null) {
				IMethod method = (IMethod) element;
				// A JAX-RS method is a public method annotated with @GET @POST,
				// @DELETE, @PUT
				// JAX-RS
				// annotation
				if (isJaxRsRequestMethod(method) && Flags.isPublic(method.getFlags())) {
					String baseURL = jaxrsContext.getLocalBaseURL();
					JaxRsMethodInfo info = createJaxRsMethodInfo(baseURL, rootPath, method, utils);
					if (info != null) {
						jaxRsMethodsInfo.add(info);
					}
				}
			}
		}
	}

	private static final SearchPattern annotationSearchPattern(String annotationFQN) {
		return SearchPattern.createPattern(annotationFQN, IJavaSearchConstants.ANNOTATION_TYPE,
				IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH);
	}

	private static ITypeRoot getTypeRoot(IJavaElement element) {
		ICompilationUnit cu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (cu != null) {
			return cu;
		}
		return (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
	}

	/**
	 * Returns the JAX-RS method information for the given Java method using the
	 * default JAX-RS semantics.
	 *
	 * @param baseURL  the base URL.
	 * @param rootPath the JAX-RS path value.
	 * @param method   the method to build the JAX-RS method information out of
	 * @param utils    the jdt utils
	 * @return the JAX-RS method information for the given Java method using the
	 *         default JAX-RS semantics
	 * @throws JavaModelException if something does wrong while calculating the HTTP
	 *                            method or resolving the URL
	 */
	private static JaxRsMethodInfo createJaxRsMethodInfo(String baseUrl, String rootPath, IMethod method,
			IJDTUtils utils) throws JavaModelException {
		IResource resource = method.getResource();
		if (resource == null) {
			return null;
		}
		String documentUri = resource.getLocationURI().toString();

		HttpMethod httpMethod = null;
		for (String methodAnnotationFQN : JaxRsConstants.HTTP_METHOD_ANNOTATIONS) {
			if (hasAnnotation(method, methodAnnotationFQN)) {
				httpMethod = JaxRsUtils.getHttpMethodForAnnotation(methodAnnotationFQN);
				break;
			}
		}
		if (httpMethod == null) {
			return null;
		}

		String pathValue = getJaxRsPathValue(method);
		String url = JaxRsUtils.buildURL(baseUrl, rootPath, pathValue);

		return new JaxRsMethodInfo(url, httpMethod, method, documentUri);
	}

}
