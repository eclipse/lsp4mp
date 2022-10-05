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
package org.eclipse.lsp4mp.jdt.internal.metrics.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4mp.commons.JavaCodeActionStub;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.InsertAnnotationMissingQuickFix;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4mp.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4mp.jdt.core.java.corrections.proposal.ReplaceAnnotationProposal;
import org.eclipse.lsp4mp.jdt.internal.metrics.MicroProfileMetricsConstants;

/**
 * QuickFix for fixing
 * {@link MicroProfileMetricsErrorCode#ApplicationScopedAnnotationMissing} error
 * by providing several code actions:
 * 
 * <ul>
 * <li>Remove @RequestScoped | @SessionScoped | @Dependent annotation</li>
 * <li>Insert @ApplicationScoped annotation and the proper import.</li>
 * </ul>
 * 
 * @author Kathryn Kodama
 *
 */
public class ApplicationScopedAnnotationMissingQuickFix extends InsertAnnotationMissingQuickFix {

	private static final String[] REMOVE_ANNOTATION_NAMES = new String[] {
			MicroProfileMetricsConstants.REQUEST_SCOPED_ANNOTATION_NAME,
			MicroProfileMetricsConstants.SESSION_SCOPED_ANNOTATION_NAME,
			MicroProfileMetricsConstants.DEPENDENT_ANNOTATION_NAME };
	
    private static final List<JavaCodeActionStub> CODE_ACTION_STUB = Arrays.asList(new JavaCodeActionStub( //
            MicroProfileMetricsErrorCode.ApplicationScopedAnnotationMissing.getCode(), //
            ApplicationScopedAnnotationMissingQuickFix.class.getName(), //
            "Replace current scope with @" + MicroProfileMetricsConstants.APPLICATION_SCOPED_ANNOTATION.substring(
                    MicroProfileMetricsConstants.APPLICATION_SCOPED_ANNOTATION.lastIndexOf('.') + 1,
                    MicroProfileMetricsConstants.APPLICATION_SCOPED_ANNOTATION.length()), //
            null));

	public ApplicationScopedAnnotationMissingQuickFix() {
		super(MicroProfileMetricsConstants.APPLICATION_SCOPED_ANNOTATION);
	}

	@Override
	protected void insertAnnotations(Diagnostic diagnostic, JavaCodeActionContext context, IBinding parentType,
			List<CodeAction> codeActions) throws CoreException {
		String[] annotations = getAnnotations();
		for (String annotation : annotations) {
			insertAndReplaceAnnotation(diagnostic, context, parentType, codeActions, annotation);
		}
	}
	
	@Override
	public CodeAction resolveCodeAction(JavaCodeActionResolveContext context, IProgressMonitor monitor) throws CoreException {
        String annotation = Arrays.asList(getAnnotations()).stream() //
                .filter(a -> context.getUnresolved().getTitle().contains(a.substring(a.lastIndexOf('.') + 1, a.length())))
                .findFirst().orElse(null);
        if (annotation == null) {
            return null;
        }
        
        List<CodeAction> codeAction = new ArrayList<>();
        ASTNode node = context.getCoveringNode();
        IBinding parentType = getBinding(node);
        insertAndReplaceAnnotation(context.getUnresolved().getDiagnostics().get(0), context, parentType, codeAction, annotation);
        return codeAction.size() >= 1 ? codeAction.get(0) : null;
    }

	private static void insertAndReplaceAnnotation(Diagnostic diagnostic, JavaCodeActionContext context,
			IBinding parentType, List<CodeAction> codeActions, String annotation) throws CoreException {
		// Insert the annotation and the proper import by using JDT Core Manipulation
		// API
		String name = getLabel(annotation);
		ChangeCorrectionProposal proposal = new ReplaceAnnotationProposal(name, context.getCompilationUnit(),
				context.getASTRoot(), parentType, 0, annotation, REMOVE_ANNOTATION_NAMES);
		// Convert the proposal to LSP4J CodeAction
		CodeAction codeAction = context.convertToCodeAction(proposal, diagnostic);
		if (codeAction != null) {
			codeActions.add(codeAction);
		}
	}

	private static String getLabel(String annotation) {
		StringBuilder name = new StringBuilder("Replace current scope with ");
		String annotationName = annotation.substring(annotation.lastIndexOf('.') + 1, annotation.length());
		name.append("@");
		name.append(annotationName);
		return name.toString();
	}

    @Override
    public List<JavaCodeActionStub> getCodeActionStubs() {
        return CODE_ACTION_STUB;
    }

    @Override
    public String getParticipantId() {
        return ApplicationScopedAnnotationMissingQuickFix.class.getName();
    }

}
