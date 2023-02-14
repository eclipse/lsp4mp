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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4mp.jdt.core.java.symbols.IJavaWorkspaceSymbolsParticipant;
import org.eclipse.lsp4mp.jdt.core.jaxrs.IJaxRsInfoProvider;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsContext;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsMethodInfo;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.core.java.JavaFeaturesRegistry;

/**
 * Collects workspace symbols for JAX-RS REST endpoints.
 */
public class JaxRsWorkspaceSymbolParticipant implements IJavaWorkspaceSymbolsParticipant {

	private static final Logger LOGGER = Logger.getLogger(JaxRsWorkspaceSymbolParticipant.class.getName());

	@Override
	public void collectSymbols(IJavaProject project, IJDTUtils utils, List<SymbolInformation> symbols, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return;
		}

		JaxRsContext jaxrsContext = new JaxRsContext(project);
		Set<ITypeRoot> jaxrsTypes = getAllJaxRsTypes(project, monitor);
		if (monitor.isCanceled()) {
			return;
		}
		List<JaxRsMethodInfo> methodsInfo = new ArrayList<>();
		for (ITypeRoot typeRoot : jaxrsTypes) {
			IJaxRsInfoProvider provider = getProviderForType(typeRoot, monitor);
			if (provider != null) {
				methodsInfo.addAll(provider.getJaxRsMethodInfo(typeRoot, jaxrsContext, utils, monitor));
			}
			if (monitor.isCanceled()) {
				return;
			}
		}

		methodsInfo.forEach(methodInfo -> {
			try {
				symbols.add(createSymbol(methodInfo, utils));
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "failed to create workspace symbol for jax-rs method", e);
			}
		});
	}

	/**
	 * Returns the provider that can provide JAX-RS method info for the given class,
	 * or null if no provider can provide info.
	 *
	 * @param typeRoot the class to collect JAX-RS method info for
	 * @param monitor the progress monitor
	 * @return the provider that can provide JAX-RS method info for the given class,
	 *         or null if no provider can provide info
	 */
	private static IJaxRsInfoProvider getProviderForType(ITypeRoot typeRoot, IProgressMonitor monitor) {
		for (IJaxRsInfoProvider provider : JavaFeaturesRegistry.getInstance().getJaxRsInfoProviders()) {
			if (provider.canProvideJaxRsMethodInfoForClass(typeRoot, monitor)) {
				return provider;
			}
		}
		LOGGER.severe("Attempted to collect JAX-RS info for " + typeRoot.getElementName()
				+ ", but no participant was suitable, despite the fact that an earlier check found a suitable participant");
		return null;
	}

	private static Set<ITypeRoot> getAllJaxRsTypes(IJavaProject javaProject, IProgressMonitor monitor) {
		Set<ITypeRoot> jaxrsTypes = new HashSet<>();
		for (IJaxRsInfoProvider provider : JavaFeaturesRegistry.getInstance().getJaxRsInfoProviders()) {
			jaxrsTypes.addAll(provider.getAllJaxRsClasses(javaProject, monitor));
			if (monitor.isCanceled()) {
				return null;
			}
		}
		return jaxrsTypes;
	}

	private static SymbolInformation createSymbol(JaxRsMethodInfo methodInfo, IJDTUtils utils) throws JavaModelException, MalformedURLException {
		ISourceRange sourceRange = methodInfo.getJavaMethod().getNameRange();
		Range r = utils.toRange(methodInfo.getJavaMethod().getOpenable(), sourceRange.getOffset(), sourceRange.getLength());
		Location location = new Location(methodInfo.getDocumentUri(), r);

		StringBuilder nameBuilder = new StringBuilder("@");
		URL url = new URL(methodInfo.getUrl());
		String path = url.getPath();
		nameBuilder.append(path);
		nameBuilder.append(": ");
		nameBuilder.append(methodInfo.getHttpMethod());

		SymbolInformation symbol = new SymbolInformation();
		symbol.setName(nameBuilder.toString());
		symbol.setKind(SymbolKind.Method);
		symbol.setLocation(location);
		return symbol;
	}

}
