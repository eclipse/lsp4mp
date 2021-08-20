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
package org.eclipse.lsp4mp.jdt.core.java.codeaction;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4mp.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4mp.jdt.core.java.corrections.proposal.InsertAnnotationAttributeProposal;

/**
 * QuickFix for inserting attribute of a given annotation.
 *
 * @author Angelo ZERR
 *
 */
public class InsertAnnotationAttributeQuickFix implements IJavaCodeActionParticipant {

	private static final String CODE_ACTION_LABEL = "Insert ''{0}'' attribute";

	private final String attributeName;

	/**
	 * Constructor for inserting attribute annotation quick fix.
	 *
	 * @param attribute name list of annotation to insert.
	 */
	public InsertAnnotationAttributeQuickFix(String attributeName) {
		this.attributeName = attributeName;
	}

	@Override
	public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
			IProgressMonitor monitor) throws CoreException {
		ASTNode selectedNode = context.getCoveringNode();
		Annotation annotation = (Annotation) selectedNode.getParent().getParent();
		List<CodeAction> codeActions = new ArrayList<>();
		insertAnnotationAttribute(annotation, this.attributeName, diagnostic, codeActions, context);
		return codeActions;
	}

	protected static void insertAnnotationAttribute(Annotation annotation, String attributeName, Diagnostic diagnostic,
			List<CodeAction> codeActions, JavaCodeActionContext context) throws CoreException {
		// Insert the annotation and the proper import by using JDT Core Manipulation
		// API
		String name = getLabel(attributeName);
		ChangeCorrectionProposal proposal = new InsertAnnotationAttributeProposal(name, context.getCompilationUnit(),
				annotation, 0, attributeName);
		// Convert the proposal to LSP4J CodeAction
		CodeAction codeAction = context.convertToCodeAction(proposal, diagnostic);
		if (codeAction != null) {
			codeActions.add(codeAction);
		}
	}

	private static String getLabel(String memberName) {
		return MessageFormat.format(CODE_ACTION_LABEL, memberName);
	}

}
