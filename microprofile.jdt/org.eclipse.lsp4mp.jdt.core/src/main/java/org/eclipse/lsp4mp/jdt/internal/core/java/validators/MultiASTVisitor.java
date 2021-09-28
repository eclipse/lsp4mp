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
package org.eclipse.lsp4mp.jdt.internal.core.java.validators;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Multiple JDT AST visitor.
 * 
 * @author Angelo ZERR
 *
 */
public class MultiASTVisitor extends ASTVisitor {

	private static final Logger LOGGER = Logger.getLogger(MultiASTVisitor.class.getName());
	private final Collection<ASTVisitor> visitors;

	public MultiASTVisitor(Collection<ASTVisitor> visitors) {
		this.visitors = visitors;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		boolean result = false;
		for (ASTVisitor visitor : visitors) {
			try {
				result |= visitor.visit(node);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while visiting node with " + visitor.getClass().getName(), e);
			}
		}
		return result;
	}

	@Override
	public void endVisit(SingleMemberAnnotation node) {
		for (ASTVisitor visitor : visitors) {
			try {
				visitor.endVisit(node);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while end visiting node with " + visitor.getClass().getName(), e);
			}
		}
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		boolean result = false;
		for (ASTVisitor visitor : visitors) {
			try {
				result |= visitor.visit(node);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while visiting node with " + visitor.getClass().getName(), e);
			}
		}
		return result;
	}

	@Override
	public void endVisit(NormalAnnotation node) {
		for (ASTVisitor visitor : visitors) {
			try {
				visitor.endVisit(node);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while end visiting node with " + visitor.getClass().getName(), e);
			}
		}
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		boolean result = false;
		for (ASTVisitor visitor : visitors) {
			try {
				result |= visitor.visit(node);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while visiting node with " + visitor.getClass().getName(), e);
			}
		}
		return result;
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		for (ASTVisitor visitor : visitors) {
			try {
				visitor.endVisit(node);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while end visiting node with " + visitor.getClass().getName(), e);
			}
		}
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		boolean result = false;
		for (ASTVisitor visitor : visitors) {
			try {
				result |= visitor.visit(node);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while visiting node with " + visitor.getClass().getName(), e);
			}
		}
		return result;
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		for (ASTVisitor visitor : visitors) {
			try {
				visitor.endVisit(node);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while end visiting node with " + visitor.getClass().getName(), e);
			}
		}
	}
}
