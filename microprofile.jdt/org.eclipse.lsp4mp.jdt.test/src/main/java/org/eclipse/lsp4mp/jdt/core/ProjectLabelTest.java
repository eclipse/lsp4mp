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
package org.eclipse.lsp4mp.jdt.core;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest.GradleProjectName;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest.MicroProfileMavenProjectName;
import org.eclipse.lsp4mp.jdt.core.utils.JDTMicroProfileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Project label tests
 *
 */
public class ProjectLabelTest {

	@Test
	public void getProjectLabelInfoOnlyMaven() throws Exception {
		IJavaProject maven = BasePropertiesManagerTest.loadMavenProject(MicroProfileMavenProjectName.empty_maven_project);
		List<ProjectLabelInfoEntry> projectLabelEntries = ProjectLabelManager.getInstance().getProjectLabelInfo();
		assertProjectLabelInfoContainsProject(projectLabelEntries, maven);
		assertLabels(projectLabelEntries, maven, "maven");
	}

	@Test
	public void getProjectLabelInfoOnlyGradle() throws Exception {
		IJavaProject gradle = BasePropertiesManagerTest.loadGradleProject(GradleProjectName.empty_gradle_project);
		List<ProjectLabelInfoEntry> projectLabelEntries = ProjectLabelManager.getInstance().getProjectLabelInfo();
		assertProjectLabelInfoContainsProject(projectLabelEntries, gradle);
		assertLabels(projectLabelEntries, gradle, "gradle");
	}

	@Test
	public void getProjectLabelQuarkusMaven() throws Exception {
		IJavaProject quarkusMaven = BasePropertiesManagerTest.loadMavenProject(MicroProfileMavenProjectName.using_vertx);
		List<ProjectLabelInfoEntry> projectLabelEntries = ProjectLabelManager.getInstance().getProjectLabelInfo();
		assertProjectLabelInfoContainsProject(projectLabelEntries, quarkusMaven);
		assertLabels(projectLabelEntries, quarkusMaven, "microprofile", "maven");
	}

	@Test
	public void getProjectLabelQuarkusGradle() throws Exception {
		IJavaProject quarkusGradle = BasePropertiesManagerTest
				.loadGradleProject(GradleProjectName.quarkus_gradle_project);
		List<ProjectLabelInfoEntry> projectLabelEntries = ProjectLabelManager.getInstance().getProjectLabelInfo();
		assertProjectLabelInfoContainsProject(projectLabelEntries, quarkusGradle);
		assertLabels(projectLabelEntries, quarkusGradle, "microprofile", "gradle");
	}

	@Test
	public void getProjectLabelMultipleProjects() throws Exception {
		IJavaProject [] projects = BasePropertiesManagerTest.loadJavaProjects(new String [] {
				"maven/" + MicroProfileMavenProjectName.using_vertx,
				"gradle/" + GradleProjectName.quarkus_gradle_project,
				"maven/" + MicroProfileMavenProjectName.empty_maven_project,
				"gradle/" + GradleProjectName.empty_gradle_project
				});
		List<ProjectLabelInfoEntry> projectLabelEntries = ProjectLabelManager.getInstance().getProjectLabelInfo();

		assertProjectLabelInfoContainsProject(projectLabelEntries, projects[0], projects[1], projects[2], projects[3]);
		assertLabels(projectLabelEntries, projects[0], "microprofile", "maven");
		assertLabels(projectLabelEntries, projects[1], "microprofile", "gradle");
		assertLabels(projectLabelEntries, projects[2], "maven");
		assertLabels(projectLabelEntries, projects[3], "gradle");
	}

	@Test
	public void projectNameMaven() throws Exception {
		IJavaProject[] projects = BasePropertiesManagerTest.loadJavaProjects(new String [] {
				"maven/" + MicroProfileMavenProjectName.using_vertx,
				"maven/" + MicroProfileMavenProjectName.empty_maven_project,
				"maven/" + MicroProfileMavenProjectName.folder_name_different_maven
		});
		List<ProjectLabelInfoEntry> projectLabelEntries = ProjectLabelManager.getInstance().getProjectLabelInfo();
		assertName(projectLabelEntries, projects[0], "using-vertx");
		assertName(projectLabelEntries, projects[1], "empty-maven-project");
		assertName(projectLabelEntries, projects[2], "mostly.empty");
	}

	@Test
	public void projectNameSameFolderName() throws Exception {
		IJavaProject [] empties = BasePropertiesManagerTest.loadJavaProjects(new String [] {
				"maven/" + MicroProfileMavenProjectName.empty_maven_project,
				"maven/" + "folder/" + MicroProfileMavenProjectName.other_empty_maven_project});
		List<ProjectLabelInfoEntry> projectLabelEntries = ProjectLabelManager.getInstance().getProjectLabelInfo();
		assertName(projectLabelEntries, empties[0], "empty-maven-project");
		assertName(projectLabelEntries, empties[1], "other-empty-maven-project");
	}

	@Test
	public void projectNameGradle() throws Exception {
		IJavaProject [] projects = BasePropertiesManagerTest.loadJavaProjects(new String [] {
				"gradle/" + GradleProjectName.quarkus_gradle_project,
				"gradle/" + GradleProjectName.empty_gradle_project,
				"gradle/" + GradleProjectName.renamed_quarkus_gradle_project
		});
		List<ProjectLabelInfoEntry> projectLabelEntries = ProjectLabelManager.getInstance().getProjectLabelInfo();
		assertName(projectLabelEntries, projects[0], "quarkus-gradle-project");
		assertName(projectLabelEntries, projects[1], "empty-gradle-project");
		// See https://github.com/eclipse-jdtls/eclipse.jdt.ls/issues/1743
		assertName(projectLabelEntries, projects[2], "my-gradle-project-renamed-gradle");
	}

	private static void assertProjectLabelInfoContainsProject(List<ProjectLabelInfoEntry> projectLabelEntries,
			IJavaProject... javaProjects) throws CoreException {
		List<String> actualProjectPaths = projectLabelEntries.stream().map(e -> e.getUri())
				.collect(Collectors.toList());
		for (IJavaProject javaProject : javaProjects) {
			assertContains(actualProjectPaths, JDTMicroProfileUtils.getProjectURI(javaProject.getProject()));
		}
	}

	private static void assertLabels(List<ProjectLabelInfoEntry> projectLabelEntries, IJavaProject javaProject,
			String... expectedLabels) throws CoreException {
		String javaProjectPath = JDTMicroProfileUtils.getProjectURI(javaProject.getProject());
		List<String> actualLabels = getLabelsFromProjectPath(projectLabelEntries, javaProjectPath);
		Assert.assertEquals(
				"Test project labels size for '" + javaProjectPath + "' with labels ["
						+ actualLabels.stream().collect(Collectors.joining(",")) + "]",
				expectedLabels.length, actualLabels.size());
		for (String expectedLabel : expectedLabels) {
			assertContains(actualLabels, expectedLabel);
		}
	}

	private static void assertName(List<ProjectLabelInfoEntry> projectLabelEntries, IJavaProject javaProject,
			String expectedName) {
		String javaProjectPath = JDTMicroProfileUtils.getProjectURI(javaProject.getProject());
		String actualName = null;
		for (ProjectLabelInfoEntry entry : projectLabelEntries) {
			if (entry.getUri().equals(javaProjectPath)) {
				actualName = entry.getName();
				break;
			}
		}
		Assert.assertEquals("Test project name in label", expectedName, actualName);
	}

	private static List<String> getLabelsFromProjectPath(List<ProjectLabelInfoEntry> projectLabelEntries, String projectPath) {
		for (ProjectLabelInfoEntry entry : projectLabelEntries) {
			if (entry.getUri().equals(projectPath)) {
				return entry.getLabels();
			}
		}
		return Collections.emptyList();
	}

	private static void assertContains(List<String> list, String strToFind) {
		for (String str : list) {
			if (str.equals(strToFind)) {
				return;
			}
		}
		Assert.fail("Expected List to contain <\"" + strToFind + "\">.");
	}
}
