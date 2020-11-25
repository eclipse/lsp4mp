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
package org.eclipse.lsp4mp.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link URIUtils}.
 * 
 * @author Angelo ZERR
 *
 */
public class URIUtilsTest {

	@Test
	public void windowsEncodedFileURI() {
		String fileURI = "file:///C:/Users/a folder/application.properties";
		String encodedFileURI = URIUtils.encodeFileURI(fileURI);
		Assert.assertEquals("file:///C%3A/Users/a%20folder/application.properties", encodedFileURI);
	}

	@Test
	public void linuxEncodedFileURI() {
		String fileURI = "file://home/a folder/application.properties";
		String encodedFileURI = URIUtils.encodeFileURI(fileURI);
		Assert.assertEquals("file://home/a%20folder/application.properties", encodedFileURI);
	}
}
