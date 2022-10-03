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
package org.eclipse.lsp4mp.jdt.internal.openapi.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4mp.commons.JavaCodeActionStub;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4mp.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;
import org.eclipse.lsp4mp.jdt.internal.openapi.MicroProfileOpenAPIConstants;

/**
 * Generate OpenAPI annotations by the "Source" kind code action.
 *
 * @author Benson Ning
 *
 */
public class MicroProfileGenerateOpenAPIOperation implements IJavaCodeActionParticipant {

    private static String CODE_ACTION_DESCRIPTION = "Generate OpenAPI Annotations";

    private static List<JavaCodeActionStub> CODE_ACTION_STUBS = Arrays.asList(new JavaCodeActionStub( //
            "", // TODO: what diagnostic does this apply for?
            MicroProfileGenerateOpenAPIOperation.class.toString(),
            CODE_ACTION_DESCRIPTION,
            null));

    @Override
    public boolean isAdaptedForCodeAction(JavaCodeActionContext context, IProgressMonitor monitor)
            throws CoreException {
        IJavaProject javaProject = context.getJavaProject();
        return JDTTypeUtils.findType(javaProject, MicroProfileOpenAPIConstants.OPERATION_ANNOTATION) != null;
    }

    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
            IProgressMonitor monitor) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();
        CompilationUnit cu = context.getASTRoot();
        List<?> types = cu.types();
        for (Object type : types) {
            if (type instanceof TypeDeclaration) {
                ChangeCorrectionProposal proposal = new OpenAPIAnnotationProposal(
                        "Generate OpenAPI Annotations", context.getCompilationUnit(), context.getASTRoot(),
                        (TypeDeclaration) type, MicroProfileOpenAPIConstants.OPERATION_ANNOTATION, 0);
                // Convert the proposal to LSP4J CodeAction
                CodeAction codeAction = context.convertToCodeAction(proposal);
                if (codeAction != null) {
                    codeActions.add(codeAction);
                }
            }
        }
        return codeActions;
    }

    @Override
    public List<JavaCodeActionStub> getCodeActionStubs() {
        return CODE_ACTION_STUBS;
    }

}
