/*******************************************************************************
* Copyright (c) 2020 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.core.jwt.properties;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.p;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

/**
 * Test the availability of the MicroProfile JWT properties
 *
 * @author Kathryn Kodama
 * 
 */
public class MicroProfileJWTPropertiesTest extends BasePropertiesManagerTest {

	@Test
	public void microProfileContextPropagationPropertiesTest() throws Exception {
		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
			MicroProfileMavenProjectName.microprofile_jwt_quickstart, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,
			// confirm properties are being merged with force, should not overwrite properties coming from ConfigProperty provider

			p(null, "mp.jwt.verify.issuer", "java.lang.String", null,
				true, "io.smallrye.jwt.config.JWTAuthContextInfoProvider", "mpJwtIssuer", null, 0, "NONE"),

			// properties coming from static JSON

			p("microprofile-jwt-api", "mp.jwt.verify.publickey.algorithm", "java.lang.String",
				"Configuration property to specify the Public Key Signature Algorithm property. The value can be set to either `RS256` or `ES256`, `RS256` is the default value.",
				true, null, null, null, 0, null),

			p("microprofile-jwt-api", "mp.jwt.decrypt.key.location", "java.lang.String",
				"Configuration property to specify the relative path or full URL of the decryption key.",
				true, null, null, null, 0, null),

			p("microprofile-jwt-api", "mp.jwt.token.header", "java.lang.String",
				"Configuration property to specify the `HTTP` header name expected to contain the JWT token.",
				true, null, null, null, 0, null),

			p("microprofile-jwt-api", "mp.jwt.token.cookie", "java.lang.String",
				"Configuration property to specify the Cookie name (default is `Bearer`) expected to contain the JWT token. This configuration will be ignored unless `mp.jwt.token.header` is set to `Cookie`.",
				true, null, null, null, 0, null),

			p("microprofile-jwt-api", "mp.jwt.verify.audiences", "java.lang.String",
				"Configuration property to specify the list of allowable value(s) for the `aud` claim, separated by commas. If specified, MP-JWT claim must be present and match one of the values.",
				true, null, null, null, 0, null)
		);
	}
	
}