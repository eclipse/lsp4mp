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
package org.eclipse.lsp4mp.jdt.core.jaxrs;

import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils.getJaxRsPathValue;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.overlaps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
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
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * A copy of {@link jdt.internal.jaxrs.java.DefaultJaxRsInfoProvider} that only
 * works for org.acme.hibernate.orm.CustomJaxRsResolving and sets all public
 * methods (regardless of annotations) as JAX-RS GET methods.
 */
public class TestJaxRsInfoProvider implements IJaxRsInfoProvider {

	private static final String CUSTOM_JAXRS_TEST_CLASS_FQN = "org.acme.hibernate.orm.CustomJaxRsResolving";

	@Override
	public boolean canProvideJaxRsMethodInfoForClass(ITypeRoot typeRoot, IProgressMonitor monitor) {
		// Only collect methods in org.acme.hibernate.orm.CustomJaxRsResolving
		IType fruitResourceType = JDTTypeUtils.findType(typeRoot.getJavaProject(), CUSTOM_JAXRS_TEST_CLASS_FQN);
		return fruitResourceType != null && typeRoot.equals(fruitResourceType.getTypeRoot());
	}

	@Override
	public Set<ITypeRoot> getAllJaxRsClasses(IJavaProject javaProject, IProgressMonitor monitor) {
		// Only collect methods in org.acme.hibernate.orm.CustomJaxRsResolving
		IType fruitResourceType = JDTTypeUtils.findType(javaProject, CUSTOM_JAXRS_TEST_CLASS_FQN);
		if (fruitResourceType == null) {
			return Collections.emptySet();
		}
		return Collections.singleton(fruitResourceType.getTypeRoot());
	}

	@Override
	public List<JaxRsMethodInfo> getJaxRsMethodInfo(ITypeRoot typeRoot, JaxRsContext jaxrsContext, IJDTUtils utils,
			IProgressMonitor monitor) {
		List<JaxRsMethodInfo> methodInfos = new ArrayList<>();
		try {
			collectJaxRsMethodInfo(typeRoot.getChildren(), null, methodInfos, jaxrsContext, utils, monitor);
		} catch (CoreException e) {
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
				// Only collect methods in org.acme.hibernate.orm.CustomJaxRsResolving
				if (pathValue != null && CUSTOM_JAXRS_TEST_CLASS_FQN.equals(type.getFullyQualifiedName())) {
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
				// In this case, add all methods, regardless of if it has a JAX-RS annotation
				if (Flags.isPublic(method.getFlags())) {
					String baseURL = jaxrsContext.getLocalBaseURL();
					JaxRsMethodInfo info = createJaxRsMethodInfo(baseURL, rootPath, method, utils);
					if (info != null) {
						jaxRsMethodsInfo.add(info);
					}
				}
			}
		}
	}

	private static JaxRsMethodInfo createJaxRsMethodInfo(String baseUrl, String rootPath, IMethod method,
			IJDTUtils utils) throws JavaModelException {
		IResource resource = method.getResource();
		if (resource == null) {
			return null;
		}
		String documentUri = resource.getLocationURI().toString();
		String pathValue = getJaxRsPathValue(method);
		String url = JaxRsUtils.buildURL(baseUrl, rootPath, pathValue);

		// assume all JAX-RS methods are GET methods
		return new JaxRsMethodInfo(url, HttpMethod.GET, method, documentUri);
	}

}
