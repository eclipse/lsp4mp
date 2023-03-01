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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4mp.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4mp.commons.codeaction.MicroProfileCodeActionId;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.JavaCodeActionResolveContext;
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

	private final static String MESSAGE = "Generate OpenAPI Annotations for ''{0}''";

	private final static String TYPE_NAME_KEY = "type";

	@Override
	public String getParticipantId() {
		return MicroProfileGenerateOpenAPIOperation.class.getName();
	}

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

				TypeDeclaration typeDeclaration = (TypeDeclaration) type;
				String typeName = typeDeclaration.getName().getFullyQualifiedName();

				Map<String, Object> extendedData = new HashMap<>();
				extendedData.put(TYPE_NAME_KEY, typeName);
				CodeActionResolveData data = new CodeActionResolveData(context.getUri(), getParticipantId(),
						context.getParams().getRange(),
						extendedData, context.getParams().isResourceOperationSupported(),
						context.getParams().isCommandConfigurationUpdateSupported(),
						MicroProfileCodeActionId.GenerateOpenApiAnnotations);

				ExtendedCodeAction codeAction = new ExtendedCodeAction(
						MessageFormat.format(MESSAGE, getSimpleName(typeName)));
				codeAction.setData(data);
				codeAction.setRelevance(0);
				codeAction.setKind(CodeActionKind.Source);
				codeActions.add(codeAction);
			}
		}
		return codeActions;
	}

	@Override
	public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {

		CodeAction toResolve = context.getUnresolved();
		CodeActionResolveData data = (CodeActionResolveData) toResolve.getData();
		String typeName = (String) data.getExtendedDataEntry(TYPE_NAME_KEY);

		if (StringUtils.isEmpty(typeName)) {
			return toResolve;
		}

		CompilationUnit cu = context.getASTRoot();
		@SuppressWarnings("unchecked")
		Optional<TypeDeclaration> typeDeclarationOpt = cu.types().stream() //
				.filter(type -> type instanceof TypeDeclaration
						&& typeName.equals(((TypeDeclaration) type).getName().getFullyQualifiedName())) //
				.map(type -> (TypeDeclaration) type) //
				.findFirst();

		if (typeDeclarationOpt.isEmpty()) {
			return toResolve;
		}

		TypeDeclaration typeDeclaration = typeDeclarationOpt.get();

		ChangeCorrectionProposal proposal = new OpenAPIAnnotationProposal(
				MessageFormat.format(MESSAGE, getSimpleName(typeName)), context.getCompilationUnit(),
				context.getASTRoot(),
				typeDeclaration, MicroProfileOpenAPIConstants.OPERATION_ANNOTATION, 0);

		try {
			toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
		} catch (CoreException e) {
		}

		return toResolve;
	}

	private static final String getSimpleName(String fullyQualifiedName) {
		int lastDot = fullyQualifiedName.lastIndexOf('.');
		if (lastDot == -1) {
			// It probably wasn't actually fully qualified :|
			return fullyQualifiedName;
		}
		return fullyQualifiedName.substring(lastDot);
	}

}
