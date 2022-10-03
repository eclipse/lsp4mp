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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4mp.commons.JavaCodeActionStub;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4mp.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4mp.jdt.core.java.corrections.proposal.ImplementInterfaceProposal;
import org.eclipse.lsp4mp.jdt.internal.health.MicroProfileHealthConstants;

/**
 * QuickFix for fixing {@link MicroProfileHealthErrorCode#ImplementHealthCheck}
 * error by providing the code actions which implements
 * 'org.eclipse.microprofile.health.HealthCheck'.
 *
 * @author Angelo ZERR
 *
 */
public class ImplementHealthCheckQuickFix implements IJavaCodeActionParticipant {

    private static final List<JavaCodeActionStub> CODE_ACTION_STUBS = Arrays.asList(new JavaCodeActionStub(
            MicroProfileHealthErrorCode.ImplementHealthCheck.toString(), //
            MicroProfileHealthErrorCode.ImplementHealthCheck.toString(), //
            "Let '{0}' implement 'org.eclipse.microprofile.health.HealthCheck'", //
            "The class `([^`]+)`.+"));

    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
            IProgressMonitor monitor) throws CoreException {
        ASTNode node = context.getCoveringNode();
        ITypeBinding parentType = Bindings.getBindingOfParentType(node);
        if (parentType != null) {
            List<CodeAction> codeActions = new ArrayList<>();
            // Create code action to implement 'org.eclipse.microprofile.health.HealthCheck'
            // interface
            ChangeCorrectionProposal proposal = new ImplementInterfaceProposal(context.getCompilationUnit(), parentType,
                    context.getASTRoot(), MicroProfileHealthConstants.HEALTH_CHECK_INTERFACE, 0);
            CodeAction codeAction = context.convertToCodeAction(proposal, diagnostic);
            codeActions.add(codeAction);
            return codeActions;
        }
        return null;
    }

    @Override
    public List<JavaCodeActionStub> getCodeActionStubs() {
        return CODE_ACTION_STUBS;
    }

}
