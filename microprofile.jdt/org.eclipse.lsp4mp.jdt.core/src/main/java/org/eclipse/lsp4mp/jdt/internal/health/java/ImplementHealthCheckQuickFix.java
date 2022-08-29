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

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4mp.commons.CodeActionResolveData;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.JavaCodeActionResolveContext;
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

	private static final Logger LOGGER = Logger.getLogger(ImplementHealthCheckQuickFix.class.getName());

	@Override
	public String getParticipantId() {
		return ImplementHealthCheckQuickFix.class.getName();
	}

	@Override
	public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
			IProgressMonitor monitor) throws CoreException {
		IBinding binding = getBinding(context.getCoveringNode());
		ExtendedCodeAction codeAction = new ExtendedCodeAction("Let '" + binding.getName() + "' implement '@"
				+ MicroProfileHealthConstants.HEALTH_CHECK_INTERFACE_NAME + "'");
		codeAction.setRelevance(0);
		codeAction.setDiagnostics(Collections.singletonList(diagnostic));
		codeAction.setKind(CodeActionKind.QuickFix);
		codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(),
				context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(),
				context.getParams().isCommandConfigurationUpdateSupported()));

		return Collections.singletonList(codeAction);
	}

	@Override
	public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {

		CodeAction toResolve = context.getUnresolved();

		ASTNode node = context.getCoveringNode();
		ITypeBinding parentType = Bindings.getBindingOfParentType(node);
		if (parentType != null) {
			// Create workspace edit to implement
			// 'org.eclipse.microprofile.health.HealthCheck'
			// interface
			ChangeCorrectionProposal proposal = new ImplementInterfaceProposal(context.getCompilationUnit(), parentType,
					context.getASTRoot(), MicroProfileHealthConstants.HEALTH_CHECK_INTERFACE, 0);
			WorkspaceEdit workspaceEdit;
			try {
				workspaceEdit = context.convertToWorkspaceEdit(proposal);
				toResolve.setEdit(workspaceEdit);
			} catch (CoreException e) {
				LOGGER.log(Level.SEVERE, "Unable to create workspace edit to make the class implement @HealthCheck", e);
			}
		}

		return context.getUnresolved();
	}

	private static IBinding getBinding(ASTNode node) {
		if (node.getParent() instanceof VariableDeclarationFragment) {
			return ((VariableDeclarationFragment) node.getParent()).resolveBinding();
		}
		return Bindings.getBindingOfParentType(node);
	}

}
