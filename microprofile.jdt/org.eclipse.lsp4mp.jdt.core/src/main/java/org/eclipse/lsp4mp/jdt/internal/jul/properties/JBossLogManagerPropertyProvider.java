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
package org.eclipse.lsp4mp.jdt.internal.jul.properties;

import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.AbstractStaticPropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.IPropertiesCollector.MergingStrategy;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.SearchContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * JBoss Logging Manager provider for {@link Level}.
 * 
 * @author Angelo ZERR
 * 
 * @see https://github.com/jboss-logging/jboss-logmanager/blob/master/core/src/main/java/org/jboss/logmanager/Level.java
 *
 */
public class JBossLogManagerPropertyProvider extends AbstractStaticPropertiesProvider {

	private static final String JBOSS_LOGMANAGER_LEVEL_CLASS = "org.jboss.logmanager.Level";

	public JBossLogManagerPropertyProvider() {
		super(MicroProfileCorePlugin.PLUGIN_ID, "/static-properties/jboss-logmanager-metadata.json",
				MergingStrategy.REPLACE /* JUL 'INFO' must be overrided with JBossLogManager 'INFO' */);
	}

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		// Check if JBoss LogManager exists in classpath
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, JBOSS_LOGMANAGER_LEVEL_CLASS) != null;
	}
}
