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
package org.eclipse.lsp4mp.jdt.internal.jaxrs.java;

import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils.createURLCodeLens;
import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils.getJaxRsApplicationPathValue;
import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils.getJaxRsPathValue;
import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils.isJaxRsRequestMethod;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.overlaps;
import static org.eclipse.lsp4mp.jdt.internal.jaxrs.JaxRsConstants.JAVAX_WS_RS_APPLICATIONPATH_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.jaxrs.JaxRsConstants.JAVAX_WS_RS_PATH_ANNOTATION;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
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
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeLensParams;
import org.eclipse.lsp4mp.jdt.core.java.codelens.IJavaCodeLensParticipant;
import org.eclipse.lsp4mp.jdt.core.java.codelens.JavaCodeLensContext;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsContext;
import org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 *
 * JAX-RS CodeLens participant
 *
 * @author Angelo ZERR
 *
 */
public class JaxRsCodeLensParticipant implements IJavaCodeLensParticipant {

	private static final String LOCALHOST = "localhost";

	private static final int PING_TIMEOUT = 2000;

	@Override
	public boolean isAdaptedForCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		MicroProfileJavaCodeLensParams params = context.getParams();
		if (!params.isUrlCodeLensEnabled()) {
			return false;
		}
		// Collection of URL codeLens is done only if JAX-RS is on the classpath
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, JAVAX_WS_RS_PATH_ANNOTATION) != null;
	}

	@Override
	public void beginCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		IJavaProject javaProject = context.getJavaProject();
		IType applicationPathType = JDTTypeUtils.findType(javaProject, JAVAX_WS_RS_APPLICATIONPATH_ANNOTATION);
		String applicationPath = findApplicationPath(applicationPathType, context, monitor);
		JaxRsContext.getJaxRsContext(context).setApplicationPath(applicationPath);
	}

	@Override
	public List<CodeLens> collectCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		ITypeRoot typeRoot = context.getTypeRoot();
		IJavaElement[] elements = typeRoot.getChildren();
		JaxRsContext jaxrsContext = JaxRsContext.getJaxRsContext(context);
		IJDTUtils utils = context.getUtils();
		MicroProfileJavaCodeLensParams params = context.getParams();
		List<CodeLens> lenses = new ArrayList<>();
		collectURLCodeLenses(elements, null, lenses, params, jaxrsContext, utils, monitor);
		return lenses;
	}

	private static void collectURLCodeLenses(IJavaElement[] elements, String rootPath, Collection<CodeLens> lenses,
			MicroProfileJavaCodeLensParams params, JaxRsContext jaxrsContext, IJDTUtils utils, IProgressMonitor monitor)
			throws JavaModelException, CoreException {

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
					// Display code lens only if local server is available.
					if (!params.isCheckServerAvailable()
							|| isServerAvailable(LOCALHOST, jaxrsContext.getServerPort(), PING_TIMEOUT)) {
						// Loop for each method annotated with @Path to generate
						// URL code lens per
						// method.
						collectURLCodeLenses(type.getChildren(), pathValue, lenses, params, jaxrsContext, utils,
								monitor);
					}
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
					String openURICommandId = params.getOpenURICommand();
					CodeLens lens = createURLCodeLens(baseURL, rootPath, openURICommandId, (IMethod) element, utils);
					if (lens != null) {
						lenses.add(lens);
					}
				}
			}
		}
	}

	/**
	 * Use the java search engine to search the java project for the location and
	 * value of the @ApplicationPath annotation, or null if not found
	 *
	 * @param annotationType the type representing the @ApplicationPath annotation
	 * @param context        the java code lens context
	 * @param monitor        the progress monitor
	 * @return the value of the @ApplicationPath annotation, or null if not found
	 * @throws CoreException
	 */
	private static String findApplicationPath(IType annotationType, JavaCodeLensContext context,
			IProgressMonitor monitor) throws CoreException {
		AtomicReference<String> applicationPathRef = new AtomicReference<String>();
		SearchPattern pattern = SearchPattern.createPattern(JAVAX_WS_RS_APPLICATIONPATH_ANNOTATION,
				IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH);
		SearchEngine engine = new SearchEngine();
		engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				createSearchScope(annotationType.getJavaProject()), new SearchRequestor() {

					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						Object o = match.getElement();
						if (o instanceof IType) {
							collectApplicationPath((IType) o);
						}
					}

					private void collectApplicationPath(IType type) throws CoreException {
						if (AnnotationUtils.hasAnnotation(type, JAVAX_WS_RS_APPLICATIONPATH_ANNOTATION)) {
							applicationPathRef.set(getJaxRsApplicationPathValue(type));
						}
					}
				}, monitor);
		return applicationPathRef.get();
	}

	private static IJavaSearchScope createSearchScope(IJavaProject javaProject) throws CoreException {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] { javaProject }, IJavaSearchScope.SOURCES);
	}

	private static boolean isServerAvailable(String host, int port, int timeout) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), timeout);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
