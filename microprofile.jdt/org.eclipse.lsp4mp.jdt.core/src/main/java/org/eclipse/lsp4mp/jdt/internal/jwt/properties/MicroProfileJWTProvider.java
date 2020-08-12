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

package org.eclipse.lsp4mp.jdt.internal.jwt.properties;

import org.eclipse.lsp4mp.jdt.internal.jwt.MicroProfileJWTConstants;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.AbstractStaticPropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.SearchContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Properties provider that provides static MicroProfile JTW properties
 * 
 * @author Kathryn Kodama
 * 
 * @see https://github.com/eclipse/microprofile-jwt-auth/blob/master/spec/src/main/asciidoc/configuration.asciidoc
 */
public class MicroProfileJWTProvider extends AbstractStaticPropertiesProvider {

    public MicroProfileJWTProvider() {
        super(MicroProfileCorePlugin.PLUGIN_ID, "/static-properties/mp-jwt-metadata.json");
    }

    @Override
    protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
        // Check if MicroProfile JWT exists in classpath
        IJavaProject javaProject = context.getJavaProject();
        return (JDTTypeUtils.findType(javaProject, MicroProfileJWTConstants.JWT_CLAIM) != null);
    }

}