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
package org.eclipse.lsp4mp.jdt.core.project;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.jdt.internal.core.FakeJavaProject;

/**
 * {@link JDTMicroProfileProject} manager.
 *
 * @author Angelo ZERR
 *
 */
public class JDTMicroProfileProjectManager {

	private static final JDTMicroProfileProjectManager INSTANCE = new JDTMicroProfileProjectManager();

	public static JDTMicroProfileProjectManager getInstance() {
		return INSTANCE;
	}

	private final Map<IJavaProject, JDTMicroProfileProject> projects;

	private JDTMicroProfileProjectManager() {
		this.projects = new HashMap<>();
	}

	public JDTMicroProfileProject getJDTMicroProfileProject(IJavaProject project) throws JavaModelException {
		IJavaProject javaProject = FakeJavaProject.getRealJavaProject(project);
		JDTMicroProfileProject info = projects.get(javaProject);
		if (info == null) {
			info = new JDTMicroProfileProject(javaProject);
			projects.put(javaProject, info);
		}
		return info;
	}
}
