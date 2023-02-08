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
package org.eclipse.lsp4mp.jdt.core.jaxrs.java;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.PropertiesManagerForJava;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTMicroProfileUtils;
import org.junit.Test;

/**
 * Tests for <code>JaxRsWorkspaceSymbolParticipantTest</code>.
 */
public class JaxRsWorkspaceSymbolParticipantTest extends BasePropertiesManagerTest {

	private static IProgressMonitor NULL_MONITOR = new NullProgressMonitor();

	@Test
	public void testConfigQuickstart() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_quickstart);
		IJDTUtils utils = JDT_UTILS;
		String projectUri = JDTMicroProfileUtils.getProjectURI(javaProject);

		List<SymbolInformation> actual = PropertiesManagerForJava.getInstance().workspaceSymbols(projectUri, utils, NULL_MONITOR);

		assertWorkspaceSymbols(Arrays.asList( //
				si("@/greeting/hello4: GET", 40, 18, 40, 24), si("@/greeting/constructor: GET", 34, 18, 34, 23),
				si("@/greeting/hello: GET", 33, 18, 33, 24), si("@/greeting: GET", 26, 18, 26, 23),
				si("@/greeting/method: GET", 38, 18, 38, 23), si("@/greeting/hello5: PATCH", 46, 18, 46, 24)), actual);
	}

	@Test
	public void testOpenLiberty() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.open_liberty);
		IJDTUtils utils = JDT_UTILS;
		String projectUri = JDTMicroProfileUtils.getProjectURI(javaProject);

		List<SymbolInformation> actual = PropertiesManagerForJava.getInstance().workspaceSymbols(projectUri, utils, NULL_MONITOR);

		assertWorkspaceSymbols(Arrays.asList( //
				si("@/api/api/resource: GET", 13, 15, 13, 20)), actual);
	}

	private static void assertWorkspaceSymbols(List<SymbolInformation> expected, List<SymbolInformation> actual) {
		assertEquals(expected.size(), actual.size());
		Collections.sort(expected, (si1, si2) -> si1.getName().compareTo(si2.getName()));
		Collections.sort(actual, (si1, si2) -> si1.getName().compareTo(si2.getName()));
		for (int i = 0; i < expected.size(); i++) {
			assertSymbolInformation(expected.get(i), actual.get(i));
		}
	}

	/**
	 * Asserts that the expected and actual symbol informations' name and range are
	 * the same.
	 *
	 * Doesn't check any of the other properties.
	 *
	 * @param expected the expected symbol information
	 * @param actual   the actual symbol information
	 */
	private static void assertSymbolInformation(SymbolInformation expected, SymbolInformation actual) {
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getLocation().getRange(), actual.getLocation().getRange());
	}

	private static SymbolInformation si(String name, int startLine, int startChar, int endLine, int endChar) {
		SymbolInformation symbolInformation = new SymbolInformation();
		symbolInformation.setName(name);
		Range range = new Range(new Position(startLine, startChar), new Position(endLine, endChar));
		Location location = new Location("", range);
		symbolInformation.setLocation(location);
		return symbolInformation;
	}
}
