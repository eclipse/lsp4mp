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
package org.eclipse.lsp4mp.jdt.internal.health.java;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4mp.commons.JavaCodeActionStub;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.InsertAnnotationMissingQuickFix;
import org.eclipse.lsp4mp.jdt.internal.health.MicroProfileHealthConstants;

/**
 * QuickFix for fixing
 * {@link MicroProfileHealthErrorCode#HealthAnnotationMissing} error by
 * providing several code actions:
 *
 * <ul>
 * <li>Insert @Liveness annotation and the proper import.</li>
 * <li>Insert @Readiness annotation and the proper import.</li>
 * <li>Insert @Health annotation and the proper import.</li>
 * </ul>
 *
 * @author Angelo ZERR
 *
 */
public class HealthAnnotationMissingQuickFix extends InsertAnnotationMissingQuickFix {

    private static final List<JavaCodeActionStub> CODE_ACTION_STUBS = Arrays.asList( //
            new JavaCodeActionStub( //
                    MicroProfileHealthErrorCode.HealthAnnotationMissing.getCode(), //
                    HealthAnnotationMissingQuickFix.class.getName(), //
                    "Insert missing @" + MicroProfileHealthConstants.LIVENESS_ANNOTATION,
                    null),
            new JavaCodeActionStub( //
                    MicroProfileHealthErrorCode.HealthAnnotationMissing.getCode(), //
                    HealthAnnotationMissingQuickFix.class.getName(), //
                    "Insert missing @" + MicroProfileHealthConstants.READINESS_ANNOTATION,
                    null),
            new JavaCodeActionStub( //
                    MicroProfileHealthErrorCode.HealthAnnotationMissing.getCode(), //
                    HealthAnnotationMissingQuickFix.class.getName(), //
                    "Insert missing @" + MicroProfileHealthConstants.HEALTH_ANNOTATION,
                    null));

    public HealthAnnotationMissingQuickFix() {
        super(MicroProfileHealthConstants.LIVENESS_ANNOTATION, MicroProfileHealthConstants.READINESS_ANNOTATION,
                MicroProfileHealthConstants.HEALTH_ANNOTATION);
    }

    @Override
    public List<JavaCodeActionStub> getCodeActionStubs() {
        return CODE_ACTION_STUBS;
    }

    @Override
    public String getParticipantId() {
        return HealthAnnotationMissingQuickFix.class.getName();
    }
}
