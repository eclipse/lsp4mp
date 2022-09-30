/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.internal.reactivemessaging.java;

import static org.eclipse.lsp4mp.jdt.internal.reactivemessaging.MicroProfileReactiveMessagingConstants.MICRO_PROFILE_REACTIVE_MESSAGING_DIAGNOSTIC_SOURCE;
import static org.eclipse.lsp4mp.jdt.internal.reactivemessaging.MicroProfileReactiveMessagingConstants.INCOMING_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.reactivemessaging.MicroProfileReactiveMessagingConstants.OUTGOING_ANNOTATION;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4mp.jdt.core.java.validators.JavaASTValidator;
import org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 *
 * MicroProfile Reactive Messaging Diagnostics:
 *
 * <ul>
 * <li>Diagnostic: display reactive messaging diagnostic message if the name of
 * the consumed channel is blank.</li>
 * </ul>
 *
 * <p>
 * This rule comes from
 * https://github.com/eclipse/microprofile-reactive-messaging/blob/62c9ed5dffe01125941bb185f1433d6307b83c86/api/src/main/java/org/eclipse/microprofile/reactive/messaging/Incoming.java#L95
 * </p>
 * 
 * @See https://github.com/eclipse/microprofile-reactive-messaging
 *
 */
public class MicroProfileReactiveMessagingASTValidator extends JavaASTValidator {

    private static final String BLANK_CHANNEL_NAME_MESSAGE = "The name of the consumed channel must not be blank.";

    private static final String ATTRIBUTE_VALUE = "value";

    private static final Logger LOGGER = Logger.getLogger(MicroProfileReactiveMessagingASTValidator.class.getName());

    @Override
    public boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context,
            IProgressMonitor monitor) throws CoreException {
        IJavaProject javaProject = context.getJavaProject();
        boolean adapted = JDTTypeUtils.findType(javaProject, INCOMING_ANNOTATION) != null
                || JDTTypeUtils.findType(javaProject, OUTGOING_ANNOTATION) != null;
        return adapted;
    }

    @Override
    public boolean visit(SingleMemberAnnotation node) {
        validateIncomingOutgoingAnnotation(node);
        return false;
    }

    @Override
    public boolean visit(NormalAnnotation node) {
        validateIncomingOutgoingAnnotation(node);
        return false;
    }

    private void validateIncomingOutgoingAnnotation(Annotation node) {
        try {
            Expression expression = AnnotationUtils.getAnnotationMemberValueExpression(node, ATTRIBUTE_VALUE);
            if (expression != null && expression.getNodeType() == StringLiteral.STRING_LITERAL
                    && ((StringLiteral) expression).getLiteralValue().isBlank()) {
                super.addDiagnostic(BLANK_CHANNEL_NAME_MESSAGE, MICRO_PROFILE_REACTIVE_MESSAGING_DIAGNOSTIC_SOURCE,
                        expression, MicroProfileReactiveMessagingErrorCode.BLANK_CHANNEL_NAME,
                        DiagnosticSeverity.Error);
            }
        } catch (JavaModelException e) {
            LOGGER.log(Level.WARNING, "Exception when trying to validate @Incoming/@Outgoing annotation", e);
        }
    }

}
