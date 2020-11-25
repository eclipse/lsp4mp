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
package org.eclipse.lsp4mp.jdt.internal.core.java.definition;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4mp.commons.MicroProfileDefinition;
import org.eclipse.lsp4mp.jdt.core.java.definition.IJavaDefinitionParticipant;
import org.eclipse.lsp4mp.jdt.core.java.definition.JavaDefinitionContext;
import org.eclipse.lsp4mp.jdt.internal.core.java.AbstractJavaFeatureDefinition;

/**
 * Wrapper class around java participants {@link IJavaDefinitionParticipant}.
 */
public class JavaDefinitionDefinition extends AbstractJavaFeatureDefinition<IJavaDefinitionParticipant>
		implements IJavaDefinitionParticipant {

	private static final Logger LOGGER = Logger.getLogger(JavaDefinitionDefinition.class.getName());

	public JavaDefinitionDefinition(IConfigurationElement element) {
		super(element);
	}

	// -------------- Definition

	@Override
	public boolean isAdaptedForDefinition(JavaDefinitionContext context, IProgressMonitor monitor) {
		try {
			return getParticipant().isAdaptedForDefinition(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling isAdaptedForDefinition", e);
			return false;
		}
	}

	@Override
	public void beginDefinition(JavaDefinitionContext context, IProgressMonitor monitor) {
		try {
			getParticipant().beginDefinition(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling beginDefinition", e);
		}
	}

	@Override
	public List<MicroProfileDefinition> collectDefinitions(JavaDefinitionContext context, IProgressMonitor monitor) {
		try {
			return getParticipant().collectDefinitions(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while collecting definition", e);
			return null;
		}
	}

	@Override
	public void endDefinition(JavaDefinitionContext context, IProgressMonitor monitor) {
		try {
			getParticipant().endDefinition(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling endDefinition", e);
		}
	}

}
