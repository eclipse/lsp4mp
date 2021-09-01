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
package org.eclipse.lsp4mp.jdt.internal.core.java.validators.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

/**
 * Test for Range expression.
 * 
 * @author Angelo ZERR
 *
 */
public class RangeExpressionTest {

	@Test
	public void nullExpression() throws RangeExpressionException {
		assertRangeExpressionException(null, "No expression");
	}

	@Test
	public void emptyExpression() throws RangeExpressionException {
		assertRangeExpressionException("", "No expression");
	}
	
	@Test
	public void invalidExpression() throws RangeExpressionException {
		// assertRangeExpressionException("a", "No expression");
	}

	private static void assertRangeExpressionException(String expression, String expectedErrorMessage) {
		RangeExpressionException exception = assertThrows(RangeExpressionException.class,
				() -> RangeExpression.parse(expression));
		assertEquals(expectedErrorMessage, exception.getMessage());
	}

}
