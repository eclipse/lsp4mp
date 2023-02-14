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
package org.eclipse.lsp4mp.jdt.core.jaxrs;

import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsConstants.JAKARTA_WS_RS_APPLICATIONPATH_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsConstants.JAVAX_WS_RS_APPLICATIONPATH_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils.getJaxRsApplicationPathValue;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.lsp4mp.jdt.core.java.codelens.JavaCodeLensContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * JAX-RS context.
 *
 * @author Angelo ZERR
 *
 */
public class JaxRsContext {

	public static final int DEFAULT_PORT = 8080;

	private static final String CONTEXT_KEY = JaxRsContext.class.getName();

	private static final SearchPattern SEARCH_PATTERN = SearchPattern.createOrPattern(
			SearchPattern.createPattern(JAVAX_WS_RS_APPLICATIONPATH_ANNOTATION, IJavaSearchConstants.ANNOTATION_TYPE,
					IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH),
			SearchPattern.createPattern(JAKARTA_WS_RS_APPLICATIONPATH_ANNOTATION, IJavaSearchConstants.ANNOTATION_TYPE,
					IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH));

	private int serverPort;

	// The quarkus.http.root-path property in application.properties
	private String rootPath;

	// The value of the @ApplicationPath annotation
	private String applicationPath;

	private final IJavaProject javaProject;

	private boolean applicationPathLoaded = false;

	public JaxRsContext(IJavaProject javaProject) {
		setServerPort(DEFAULT_PORT);
		this.javaProject = javaProject;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * Get the quarkus.http.root-path property
	 *
	 * @return the rootPath
	 */
	public String getRootPath() {
		return rootPath;
	}

	/**
	 * Set the quarkus.http.root-path property
	 *
	 * @param rootPath the rootPath to set
	 */
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	/**
	 * Get the @ApplicationPath annotation value
	 *
	 * @param the progress monitor
	 * @return the @ApplicationPath annotation value
	 * @throws CoreException
	 */
	public String getApplicationPath(IProgressMonitor monitor) throws CoreException {
		if (applicationPathLoaded) {
			return applicationPath;
		}
		IType applicationPathType = JDTTypeUtils.findType(javaProject,
				JAVAX_WS_RS_APPLICATIONPATH_ANNOTATION);
		if (applicationPathType == null) {
			applicationPathType = JDTTypeUtils.findType(javaProject,
					JAKARTA_WS_RS_APPLICATIONPATH_ANNOTATION);
		}
		applicationPath = findApplicationPath(javaProject, monitor);
		applicationPathLoaded = true;
		return applicationPath;
	}

	/**
	 * Set the @ApplicationPath annotation value
	 *
	 * @param applicationPath as the @ApplicationPath annotation value
	 */
	public void setApplicationPath(String applicationPath) {
		this.applicationPath = applicationPath;
	}

	public static JaxRsContext getJaxRsContext(JavaCodeLensContext context) {
		JaxRsContext jaxRsContext = (JaxRsContext) context.get(CONTEXT_KEY);
		if (jaxRsContext == null) {
			jaxRsContext = new JaxRsContext(context.getJavaProject());
			context.put(CONTEXT_KEY, jaxRsContext);
		}
		return jaxRsContext;
	}

	/**
	 * Create the local base URL
	 *
	 * @return the String representation of the project base URL
	 */
	public String getLocalBaseURL() {
		StringBuilder localBaseURL = new StringBuilder("http://localhost:");
		localBaseURL.append(getServerPort());
		if (rootPath != null) {
			localBaseURL.append(getRootPath());
		}
		// application path is lazy loaded, but we need it now
		try {
			getApplicationPath(null);
		} catch (CoreException e) {
			// do nothing
		}
		if (applicationPath != null) {
			if (!applicationPath.startsWith("/")) {
				localBaseURL.append('/');
			}
			localBaseURL.append(applicationPath);
		}
		return localBaseURL.toString();
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
	private static String findApplicationPath(IJavaProject javaProject,
			IProgressMonitor monitor) throws CoreException {
		AtomicReference<String> applicationPathRef = new AtomicReference<String>();
		SearchEngine engine = new SearchEngine();
		engine.search(SEARCH_PATTERN, new SearchParticipant[] {
				SearchEngine.getDefaultSearchParticipant()
		}, createSearchScope(javaProject), new SearchRequestor() {

			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				Object o = match.getElement();
				if (o instanceof IType) {
					collectApplicationPath((IType) o);
				}
			}

			private void collectApplicationPath(IType type) throws CoreException {
				String applicationPathValue = getJaxRsApplicationPathValue(type);
				if (applicationPathValue != null) {
					applicationPathRef.set(applicationPathValue);
				}
			}
		}, monitor);
		return applicationPathRef.get();
	}

	private static IJavaSearchScope createSearchScope(IJavaProject javaProject) throws CoreException {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] {
				javaProject
		}, IJavaSearchScope.SOURCES);
	}

}
