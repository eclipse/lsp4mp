/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.lsp4mp.commons.ClasspathKind;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.core.JobHelpers;
import org.eclipse.lsp4mp.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Base class for testing {@link PropertiesManager}.
 *
 * @author Angelo ZERR
 *
 */
public class BasePropertiesManagerTest {

	private static final Logger LOGGER = Logger.getLogger(BasePropertiesManagerTest.class.getSimpleName());
	private static Level oldLevel;

	protected static IJDTUtils JDT_UTILS = JDTUtilsLSImpl.getInstance();

	public static class MicroProfileMavenProjectName {

		public static String all_quarkus_extensions = "all-quarkus-extensions";
		public static String config_hover = "config-hover";
		public static String config_properties = "config-properties";
		public static String config_quickstart = "config-quickstart";
		public static String config_quickstart_test = "config-quickstart-test";
		public static String empty_maven_project = "empty-maven-project";
		public static String other_empty_maven_project = "empty-maven-project";
		public static String folder_name_different_maven = "folder-name-different-maven";
		public static String hibernate_orm_resteasy = "hibernate-orm-resteasy";
		public static String hibernate_orm_resteasy_yaml = "hibernate-orm-resteasy-yaml";
		public static String microprofile_configproperties = "microprofile-configproperties";
		public static String microprofile_fault_tolerance = "microprofile-fault-tolerance";
		public static String microprofile_health_quickstart = "microprofile-health-quickstart";
		public static String microprofile_lra = "microprofile-lra";
		public static String microprofile_context_propagation = "microprofile-context-propagation";
		public static String microprofile_metrics = "microprofile-metrics";
		public static String microprofile_opentracing = "microprofile-opentracing";
		public static String microprofile_openapi = "microprofile-openapi";
		public static String microprofile_reactive_messaging = "microprofile-reactive-messaging";
		public static String microprofile_graphql = "microprofile-graphql";
		public static String microprofile_jwt_quickstart = "microprofile-jwt-quickstart";
		public static String rest_client_quickstart = "rest-client-quickstart";
		public static String using_vertx = "using-vertx";

	}

	public static class GradleProjectName {

		public static String empty_gradle_project = "empty-gradle-project";
		public static String quarkus_gradle_project = "quarkus-gradle-project";
		public static String renamed_quarkus_gradle_project = "renamed-gradle";

	}

	@BeforeClass
	public static void setUp() {
		oldLevel = LOGGER.getLevel();
		LOGGER.setLevel(Level.INFO);
	}

	@AfterClass
	public static void tearDown() {
		LOGGER.setLevel(oldLevel);
	}

	protected static void setJDTUtils(IJDTUtils newUtils) {
		JDT_UTILS = newUtils;
	}

	protected static MicroProfileProjectInfo getMicroProfileProjectInfoFromMavenProject(String mavenProject)
			throws CoreException, Exception, JavaModelException {
		return getMicroProfileProjectInfoFromMavenProject(mavenProject,
				MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);
	}

	protected static MicroProfileProjectInfo getMicroProfileProjectInfoFromMavenProject(String mavenProject,
			List<MicroProfilePropertiesScope> scopes) throws CoreException, Exception, JavaModelException {
		IJavaProject javaProject = loadMavenProject(mavenProject);
		return PropertiesManager.getInstance().getMicroProfileProjectInfo(javaProject, scopes, ClasspathKind.SRC,
				JDT_UTILS, DocumentFormat.Markdown, new NullProgressMonitor());
	}

	public static IJavaProject loadMavenProject(String mavenProject) throws CoreException, Exception {
		// Load existing "hibernate-orm-resteasy" maven project
		return loadJavaProject(mavenProject, "maven");
	}

	public static IJavaProject loadGradleProject(String gradleProject) throws CoreException, Exception {
		return loadJavaProject(gradleProject, "gradle");
	}

	public static IJavaProject loadMavenProjectFromSubFolder(String mavenProject, String subFolder) throws Exception {
		return loadJavaProject(mavenProject, java.nio.file.Paths.get("maven", subFolder).toString());
	}

	private static IJavaProject loadJavaProject(String projectName, String parentDirName)
			throws CoreException, Exception {
		// Move project to working directory
		File projectFolder = copyProjectToWorkingDirectory(projectName, parentDirName);

		IPath path = new Path(new File(projectFolder, "/.project").getAbsolutePath());
		IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(path);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());

		if (!project.exists()) {
			project.create(description, null);
			project.open(null);

			// We need to call waitForBackgroundJobs with a Job which does nothing to have a
			// resolved classpath (IJavaProject#getResolvedClasspath) when search is done.
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					monitor.done();

				}
			};
			IProgressMonitor monitor = new NullProgressMonitor();
			JavaCore.run(runnable, null, monitor);
			waitForBackgroundJobs(monitor);
		}
		// Collect Quarkus properties from the "hibernate-orm-resteasy" project. It
		// should collect Quarkus properties from given JAR:

		// 1) quarkus-hibernate-orm.jar which is declared in the dependencies of the
		// pom.xml
		// <dependency>
		// <groupId>io.quarkus</groupId>
		// <artifactId>quarkus-hibernate-orm</artifactId>
		// </dependency>

		// 2) quarkus-hibernate-orm-deployment.jar which is declared in
		// META-INF/quarkus-extension.properties of quarkus-hibernate-orm.jar as
		// property:
		// deployment-artifact=io.quarkus\:quarkus-hibernate-orm-deployment\:0.21.1

		return JavaModelManager.getJavaModelManager().getJavaModel()
				.getJavaProject(description.getName());
	}

	private static File copyProjectToWorkingDirectory(String projectName, String parentDirName) throws IOException {
		File from = new File("projects/" + parentDirName + "/" + projectName);
		File to = new File(JavaUtils.getWorkingProjectDirectory(),
				java.nio.file.Paths.get(parentDirName, projectName).toString());

		if (to.exists()) {
			FileUtils.forceDelete(to);
		}

		if (from.isDirectory()) {
			FileUtils.copyDirectory(from, to);
		} else {
			FileUtils.copyFile(from, to);
		}

		return to;
	}

	private static void waitForBackgroundJobs(IProgressMonitor monitor) throws Exception {
		JobHelpers.waitForJobsToComplete(monitor);
	}

	private static void createFile(IFile file, String contents) throws CoreException {
		createParentFolders(file);
		file.refreshLocal(IResource.DEPTH_ZERO, null);
		InputStream fileContents = new ByteArrayInputStream(contents.getBytes());
		if (file.exists()) {			
			file.setContents(fileContents, IResource.NONE, null);
		} else {
			file.create(fileContents, true, null);
		}		
	}

	private static void createParentFolders(final IResource resource) throws CoreException {
		if (resource == null || resource.exists())
			return;
		if (!resource.getParent().exists())
			createParentFolders(resource.getParent());
		switch (resource.getType()) {
		case IResource.FOLDER:
			((IFolder) resource).create(IResource.NONE, true, new NullProgressMonitor());
			break;
		case IResource.PROJECT:
			((IProject) resource).create(new NullProgressMonitor());
			((IProject) resource).open(new NullProgressMonitor());
			break;
		}
	}

	private static void updateFile(IFile file, String content) throws CoreException {
		// For Mac OS, Linux OS, the call of Files.getLastModifiedTime is working for 1
		// second.
		// Here we wait for > 1s to be sure that call of Files.getLastModifiedTime will
		// work.
		try {
			Thread.sleep(1050);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		createFile(file, content);
	}

	protected static void saveFile(String configFileName, String content, IJavaProject javaProject)
			throws CoreException {
		IFile file = getFile(configFileName, javaProject);
		updateFile(file, content);
	}

	protected static void deleteFile(String configFileName, IJavaProject javaProject)
			throws IOException, CoreException {
		IFile file = getFile(configFileName, javaProject);
		file.delete(true, new NullProgressMonitor());
	}

	private static IFile getFile(String configFileName, IJavaProject javaProject) throws JavaModelException {
		IPath output = javaProject.getOutputLocation();
		IPath filePath = output.append(configFileName);
		return ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
	}
}
