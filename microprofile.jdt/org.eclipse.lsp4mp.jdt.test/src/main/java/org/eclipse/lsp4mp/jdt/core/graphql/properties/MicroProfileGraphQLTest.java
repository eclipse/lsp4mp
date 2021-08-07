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

package org.eclipse.lsp4mp.jdt.core.graphql.properties;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertPropertiesDuplicate;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.p;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

/**
 * Test the availability of the MicroProfile GraphQL properties
 * 
 * @author Kathryn Kodama
 * 
 */
public class MicroProfileGraphQLTest extends BasePropertiesManagerTest {

    @Test
    public void microprofileContextPropagationPropertiesTest() throws Exception {
        MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MicroProfileMavenProjectName.microprofile_graphql, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,

                p("microprofile-graphql-api", "mp.graphql.defaultErrorMessage", "java.lang.String",
                "Configured default message displayed when an unchecked exception is thrown from the user application. By default this value is set to \"Server Error\".", true,
                        null, null, null, 0, null),
                
                p("microprofile-graphql-api", "mp.graphql.hideErrorMessage", "java.lang.String",
                "Hide exception error messages when checked exceptions are thrown from the user application. Separate multiple exceptions with a comma. By default all unchecked exceptions are on the `hideErrorMessage` list.", true,
                        null, null, null, 0, null),
                
                p("microprofile-graphql-api", "mp.graphql.showErrorMessage", "java.lang.String",
                "Show exception error messages when unchecked exceptions are thrown from the user application. Separate multiple exceptions with a comma. By default all checked exceptions are on the `showErrorMessage` list.", true,
                        null, null, null, 0, null)
		);

        assertPropertiesDuplicate(infoFromClasspath);
    }

}