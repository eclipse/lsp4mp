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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4mp.commons.codeaction.MicroProfileCodeActionId;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.InsertAnnotationMissingQuickFix;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;
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

	public HealthAnnotationMissingQuickFix() {
		super(MicroProfileHealthConstants.LIVENESS_ANNOTATION, MicroProfileHealthConstants.READINESS_ANNOTATION,
				MicroProfileHealthConstants.HEALTH_ANNOTATION);
	}

	@Override
	public String getParticipantId() {
		return HealthAnnotationMissingQuickFix.class.getName();
	}

	@Override
	protected void insertAnnotations(Diagnostic diagnostic, JavaCodeActionContext context, List<CodeAction> codeActions)
			throws CoreException {
		boolean generateOnlyOneCodeAction = isGenerateOnlyOneCodeAction();
		String[] annotations = getAnnotations();
		if (generateOnlyOneCodeAction) {
			for (String annotation : annotations) {
				if (JDTTypeUtils.findType(context.getJavaProject(), annotation) == null) {
					return;
				}
			}
			insertAnnotation(diagnostic, context, codeActions, getAnnotations());
		} else {
			for (String annotation : annotations) {
				if (JDTTypeUtils.findType(context.getJavaProject(), annotation) != null) {
					insertAnnotation(diagnostic, context, codeActions, annotation);
				}
			}
		}
	}

	@Override
	protected MicroProfileCodeActionId getCodeActionId() {
		return MicroProfileCodeActionId.InsertMissingHealthAnnotation;
	}

}
