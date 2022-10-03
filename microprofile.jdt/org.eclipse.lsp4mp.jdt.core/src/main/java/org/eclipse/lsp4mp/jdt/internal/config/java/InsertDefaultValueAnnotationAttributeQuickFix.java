/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.internal.config.java;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4mp.commons.JavaCodeActionStub;
import org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.InsertAnnotationAttributeQuickFix;

/**
 * QuickFix for fixing
 * {@link MicroProfileConfigErrorCode#NO_VALUE_ASSIGNED_TO_PROPERTY} error by
 * providing several code actions:
 *
 * <ul>
 * <li>Insert defaultValue attribute annotation in the @ConfigProperty.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class InsertDefaultValueAnnotationAttributeQuickFix extends InsertAnnotationAttributeQuickFix {

    private static final List<JavaCodeActionStub> CODE_ACTION_STUBS = Arrays.asList(new JavaCodeActionStub( //
            MicroProfileConfigErrorCode.NO_VALUE_ASSIGNED_TO_PROPERTY.getCode(), //
            InsertDefaultValueAnnotationAttributeQuickFix.class.getName(), //
            "Insert the default value for the annotation",
            null));

    public InsertDefaultValueAnnotationAttributeQuickFix() {
        super(MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE);
    }

    @Override
    public List<JavaCodeActionStub> getCodeActionStubs() {
        return CODE_ACTION_STUBS;
    }

}
