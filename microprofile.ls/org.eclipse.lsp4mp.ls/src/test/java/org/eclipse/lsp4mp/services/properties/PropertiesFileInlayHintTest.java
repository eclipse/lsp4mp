/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.services.properties;

import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.ih;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.p;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.testInlayHintFor;

import org.junit.Test;

/**
 * Test inlay hint for the 'microprofile-config.properties' file.
 *
 */
public class PropertiesFileInlayHintTest {

	@Test
	public void expression() throws Exception {
		String value = "server.url=https://${host}:${port:8080}/${endpoint}\n" + // [https://microprofile.io:8080/project/eclipse/microprofile-config]
				"host=microprofile.io\n" + //
				"app=project\n" + //
				"service=eclipse/microprofile-config\n" + //
				"endpoint=${app}/${service}"; // [ project/eclipse/microprofile-config]
		testInlayHintFor(value, //
				ih(p(0, 51), " https://microprofile.io:8080/project/eclipse/microprofile-config"), //
				ih(p(4, 26), " project/eclipse/microprofile-config"));
	};

	@Test
	public void invalidExpression() throws Exception {
		String value = "server.url=https://${host}:${port}/${endpoint}\n" + // error -> here port is not defined
				"host=microprofile.io\n" + //
				"app=project\n" + //
				"service=eclipse/microprofile-config\n" + //
				"endpoint=${app}/${service}";
		testInlayHintFor(value, //
				ih(p(4, 26), " project/eclipse/microprofile-config")); // [project/eclipse/microprofile-config]
	};
}