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

package org.eclipse.lsp4mp.jdt.internal.graphql.properties;

import org.eclipse.lsp4mp.jdt.internal.graphql.MicroProfileGraphQLConstants;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.AbstractStaticPropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.SearchContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Properties provider that provides static MicroProfile GraphQL properties
 * 
 * @author Kathryn Kodama
 * 
 * @see https://github.com/eclipse/microprofile-graphql/blob/905456693d9bfb1f78efe0d7e614db6fde3324b2/server/spec/src/main/asciidoc/errorhandling.asciidoc
 *
 */
public class MicroProfileGraphQLProvider extends AbstractStaticPropertiesProvider {

    public MicroProfileGraphQLProvider() {
        super(MicroProfileCorePlugin.PLUGIN_ID, "/static-properties/mp-graphql-metadata.json");
    }

    @Override
    protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
        // Check if MicroProfile GraphQL exists in classpath
        IJavaProject javaProject = context.getJavaProject();
        return (JDTTypeUtils.findType(javaProject, MicroProfileGraphQLConstants.GRAPHQL_NAME_TAG) != null);
    }

}