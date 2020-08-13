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

import static org.eclipse.lsp4mp.jdt.internal.core.JavaUtils.getJarPath;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfoParams;
import org.junit.Assert;
import org.junit.Test;

/**
 * JDT Quarkus manager test.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesManagerTest extends BasePropertiesManagerTest {

	private static final String QUARKUS_CORE_JAR = getJarPath("quarkus-core-0.28.1.jar");

	private static final String QUARKUS_CORE_DEPLOYMENT_JAR = getJarPath("quarkus-core-deployment-0.28.1.jar");

	@Test
	public void notBelongToEclipseProject() throws JavaModelException, CoreException {
		MicroProfileProjectInfoParams params = new MicroProfileProjectInfoParams();
		params.setUri("bad-uri");
		MicroProfileProjectInfo info = PropertiesManager.getInstance().getMicroProfileProjectInfo(params,
				JDT_UTILS, new NullProgressMonitor());
		Assert.assertNotNull("MicroProfileProjectInfo for 'bad-uri' should not be null", info);
		Assert.assertTrue("MicroProfileProjectInfo for 'bad-uri' should not belong to an Eclipse project ",
				info.getProjectURI().isEmpty());
	}
}
