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
package org.eclipse.lsp4mp.jdt.internal.restclient.java;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4mp.commons.JavaCodeActionStub;
import org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.InsertAnnotationMissingQuickFix;
import org.eclipse.lsp4mp.jdt.internal.restclient.MicroProfileRestClientConstants;
import org.eclipse.lsp4mp.jdt.internal.restclient.MicroProfileRestClientErrorCode;

/**
 * QuickFix for fixing
 * {@link MicroProfileRestClientErrorCode#InjectAndRestClientAnnotationMissing}
 * error by providing several code actions:
 *
 * <ul>
 * <li>Insert @Inject and @RestClient annotation and the proper import.</li>
 * </ul>
 *
 * @author Angelo ZERR
 *
 */
public class InjectAndRestClientAnnotationMissingQuickFix extends InsertAnnotationMissingQuickFix {

    private static List<JavaCodeActionStub> CODE_ACTION_STUBS = Arrays.asList(//
            new JavaCodeActionStub( //
                    MicroProfileRestClientErrorCode.InjectAndRestClientAnnotationMissing.getCode(), //
                    InjectAndRestClientAnnotationMissingQuickFix.class.getName(), //
                    "Insert @" + MicroProfileConfigConstants.INJECT_ANNOTATION + " and @"
                            + MicroProfileRestClientConstants.REST_CLIENT_ANNOTATION,
                    null));

    public InjectAndRestClientAnnotationMissingQuickFix() {
        super(true, MicroProfileConfigConstants.INJECT_ANNOTATION,
                MicroProfileRestClientConstants.REST_CLIENT_ANNOTATION);
    }

    @Override
    public List<JavaCodeActionStub> getCodeActionStubs() {
        return CODE_ACTION_STUBS;
    }

}
