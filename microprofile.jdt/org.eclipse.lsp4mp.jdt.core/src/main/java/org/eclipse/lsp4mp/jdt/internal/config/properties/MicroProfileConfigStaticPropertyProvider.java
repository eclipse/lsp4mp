/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.internal.config.properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.AbstractStaticPropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.SearchContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * MicroProfile Config provider.
 * 
 * @see https://download.eclipse.org/microprofile/microprofile-config-1.4/microprofile-config-spec.html#_microprofile_config
 */
public class MicroProfileConfigStaticPropertyProvider extends AbstractStaticPropertiesProvider {

    public MicroProfileConfigStaticPropertyProvider() {
        super(MicroProfileCorePlugin.PLUGIN_ID, "/static-properties/mp-config-metadata.json");
    }

    @Override
    protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
        // MicroProfile config properties are always available with MicroProfile
        return true;
    }
}
