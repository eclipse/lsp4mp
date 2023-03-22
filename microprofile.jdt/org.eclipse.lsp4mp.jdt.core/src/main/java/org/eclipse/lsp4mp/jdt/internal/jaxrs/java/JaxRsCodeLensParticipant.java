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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeLensParams;
import org.eclipse.lsp4mp.jdt.core.java.codelens.IJavaCodeLensParticipant;
import org.eclipse.lsp4mp.jdt.core.java.codelens.JavaCodeLensContext;
import org.eclipse.lsp4mp.jdt.core.jaxrs.HttpMethod;
import org.eclipse.lsp4mp.jdt.core.jaxrs.IJaxRsInfoProvider;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsContext;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsMethodInfo;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.core.java.JavaFeaturesRegistry;

/**
 *
 * JAX-RS CodeLens participant
 *
 * @author Angelo ZERR
 *
 */
public class JaxRsCodeLensParticipant implements IJavaCodeLensParticipant {

	private static final Logger LOGGER = Logger.getLogger(JaxRsCodeLensParticipant.class.getName());

	private static final String LOCALHOST = "localhost";

	private static final int PING_TIMEOUT = 2000;

	@Override
	public boolean isAdaptedForCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		MicroProfileJavaCodeLensParams params = context.getParams();
		if (!params.isUrlCodeLensEnabled()) {
			return false;
		}

		ITypeRoot typeRoot = context.getTypeRoot();
		// if some jaxrs info provider can provide jaxrs method info for this class, provide lens
		return getProviderForType(typeRoot, monitor) != null;
	}

	@Override
	public void beginCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		JaxRsContext.getJaxRsContext(context).getApplicationPath(monitor);
	}

	@Override
	public List<CodeLens> collectCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		ITypeRoot typeRoot = context.getTypeRoot();
		JaxRsContext jaxrsContext = JaxRsContext.getJaxRsContext(context);
		IJDTUtils utils = context.getUtils();

		if (context.getParams().isCheckServerAvailable()
				&& !isServerAvailable(LOCALHOST, jaxrsContext.getServerPort(), PING_TIMEOUT)) {
			return Collections.emptyList();
		}

		IJaxRsInfoProvider provider = getProviderForType(typeRoot, monitor);
		if (provider == null) {
			return Collections.emptyList();
		}
		List<JaxRsMethodInfo> infos = provider.getJaxRsMethodInfo(typeRoot, jaxrsContext, utils, monitor);

		MicroProfileJavaCodeLensParams params = context.getParams();
		return infos.stream() //
				.map(methodInfo -> {
					try {
						return createCodeLens(methodInfo, params.getOpenURICommand(), utils);
					} catch (Exception e) {
						LOGGER.log(Level.WARNING, "failed to create codelens for jax-rs method", e);
						return null;
					}
				}) //
				.filter(lens -> lens != null) //
				.collect(Collectors.toList());
	}

	private static boolean isServerAvailable(String host, int port, int timeout) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), timeout);
			return true;
		} catch (IOException e) {
			return false;
		}
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

	/**
	 * Returns a code lens for the given JAX-RS method information.
	 *
	 * @param methodInfo       the JAX-RS method information to build the code lens
	 *                         out of
	 * @param openUriCommandId the id of the client command to invoke to open a URL
	 *                         in the browser
	 * @param utils            the jdt utils
	 * @return a code lens for the given JAX-RS method information
	 * @throws JavaModelException if something goes wrong calculating the range
	 */
	private static CodeLens createCodeLens(JaxRsMethodInfo methodInfo, String openUriCommandId, IJDTUtils utils)
			throws JavaModelException {
		CodeLens lens = new CodeLens();

		IMethod method = methodInfo.getJavaMethod();
		IAnnotation[] annotations = method.getAnnotations();
		if (annotations != null && annotations.length > 0) {
			ISourceRange r = annotations[annotations.length - 1].getSourceRange();

			Range range = utils.toRange(method.getOpenable(), r.getOffset(), r.getLength());
			// Increment line number for code lens to appear on the line right after the
			// last annotation
			Position codeLensPosition = new Position(range.getEnd().getLine() + 1, range.getEnd().getCharacter());
			range.setStart(codeLensPosition);
			range.setEnd(codeLensPosition);

			lens.setRange(range);
		} else {
			ISourceRange r = method.getNameRange();
			Range range = utils.toRange(method.getOpenable(), r.getOffset(), r.getLength());
			lens.setRange(range);
		}

		lens.setCommand(new Command(methodInfo.getUrl(), //
				isHttpMethodClickable(methodInfo.getHttpMethod()) && openUriCommandId != null ? openUriCommandId : "", //
				Collections.singletonList(methodInfo.getUrl())));
		return lens;
	}

	private static boolean isHttpMethodClickable(HttpMethod httpMethod) {
		return HttpMethod.GET.equals(httpMethod);
	}

}
