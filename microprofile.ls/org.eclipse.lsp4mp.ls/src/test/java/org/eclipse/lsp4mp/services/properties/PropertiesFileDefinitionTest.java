/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.services.properties;

import static org.eclipse.lsp4mp.services.PropertiesFileAssert.ll;
import static org.eclipse.lsp4mp.services.PropertiesFileAssert.r;
import static org.eclipse.lsp4mp.services.PropertiesFileAssert.testDefinitionFor;

import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.junit.Test;

/**
 * Test with definition in 'application.properties' file.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesFileDefinitionTest {

	private static final String PROPERTY_DOCUMENT_NAME = "/microprofile.properties";

	@Test
	public void definitionOnComments() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "#|";
		testDefinitionFor(value);

		value = " #|";
		testDefinitionFor(value);
	}

	@Test
	public void definitionOnKey() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.application.na|me";
		testDefinitionFor(value, ll(
				"jdt://contents/quarkus-core-1.3.2.Final.jar/io.quarkus.runtime/ApplicationConfig.class?=all-quarkus-extensions/C:%5C/Users%5C/azerr%5C/.m2%5C/repository%5C/io%5C/quarkus%5C/quarkus-core%5C/1.3.2.Final%5C/quarkus-core-1.3.2.Final.jar%3Cio.quarkus.runtime(ApplicationConfig.class",
				r(0, 0, 24), r(16, 28, 32)));
	}

	@Test
	public void definitionOnEnumValue() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.log.syslog.async.overflow=BLO|CK";
		testDefinitionFor(value, ll(
				"jdt://contents/jboss-logmanager-embedded-1.0.4.jar/org.jboss.logmanager.handlers/AsyncHandler$OverflowAction.class?=all-quarkus-extensions/C:%5C/Users%5C/azerr%5C/.m2%5C/repository%5C/org%5C/jboss%5C/logmanager%5C/jboss-logmanager-embedded%5C/1.0.4%5C/jboss-logmanager-embedded-1.0.4.jar%3Corg.jboss.logmanager.handlers(AsyncHandler$OverflowAction.class",
				r(0, 34, 39), r(222, 8, 13)));
	}

	@Test
	public void definitionOnOptionalEnumValue() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.transaction-isolation-level=no|ne";
		testDefinitionFor(value, ll(
				"jdt://contents/agroal-api-1.7.jar/io.agroal.api.configuration/AgroalConnectionFactoryConfiguration$TransactionIsolation.class?=all-quarkus-extensions/C:%5C/Users%5C/azerr%5C/.m2%5C/repository%5C/io%5C/agroal%5C/agroal-api%5C/1.7%5C/agroal-api-1.7.jar%3Cio.agroal.api.configuration(AgroalConnectionFactoryConfiguration$TransactionIsolation.class",
				r(0, 47, 51), r(87, 19, 23)));
	}

	@Test
	public void definitionOnOptionalEnumValueKebabCase()
			throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.transaction-isolation-level=read-uncom|mitted";
		testDefinitionFor(value, ll(
				"jdt://contents/agroal-api-1.7.jar/io.agroal.api.configuration/AgroalConnectionFactoryConfiguration$TransactionIsolation.class?=all-quarkus-extensions/C:%5C/Users%5C/azerr%5C/.m2%5C/repository%5C/io%5C/agroal%5C/agroal-api%5C/1.7%5C/agroal-api-1.7.jar%3Cio.agroal.api.configuration(AgroalConnectionFactoryConfiguration$TransactionIsolation.class",
				r(0, 47, 63), r(87, 25, 41)));
	}

	@Test
	public void definitionOnMappedPropertyOptionalEnumValue()
			throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.key.transaction-isolation-level=no|ne";
		testDefinitionFor(value, ll(
				"jdt://contents/agroal-api-1.7.jar/io.agroal.api.configuration/AgroalConnectionFactoryConfiguration$TransactionIsolation.class?=all-quarkus-extensions/C:%5C/Users%5C/azerr%5C/.m2%5C/repository%5C/io%5C/agroal%5C/agroal-api%5C/1.7%5C/agroal-api-1.7.jar%3Cio.agroal.api.configuration(AgroalConnectionFactoryConfiguration$TransactionIsolation.class",
				r(0, 51, 55), r(87, 19, 23)));
	}

	@Test
	public void noDefinitionOnKey() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.driv|erXXXX";
		testDefinitionFor(value);
	}

	@Test
	public void noDefinitionOnValue() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.driver=XXX|X";
		testDefinitionFor(value);
	}

	@Test
	public void noDefinitionOnValueBoundaries() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.driver=|XXXX";
		testDefinitionFor(value);
		value = "quarkus.datasource.driver=XXXX|";
		testDefinitionFor(value);
	}

	@Test
	public void definitionOnPropertyValueExpression() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "test.property = value\n" + //
				"other.property = ${test.prop|erty}";
		testDefinitionFor(value, PROPERTY_DOCUMENT_NAME, ll(
			PROPERTY_DOCUMENT_NAME, r(1, 19, 32), r(0, 0, 13)
		));
	}

	@Test
	public void noDefinitionOnUndefinedProperty() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "test.property = ${other.prop|erty}";
		testDefinitionFor(value);
	}

	@Test
	public void noDefinitionOnJustDollarSign() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "test.property = $|";
		testDefinitionFor(value);
	}

	@Test
	public void definitionOnUnclosedPropertyValueExpression() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "test.property = value\n" + //
				"other.property = ${test.|property";
		testDefinitionFor(value, PROPERTY_DOCUMENT_NAME, ll(
			PROPERTY_DOCUMENT_NAME, r(1, 19, 32), r(0, 0, 13)
		));
	}

	@Test
	public void noDefinitionAfterPropertyValueExpression() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "test.property = value\n" + //
				"other.property = $test.|property";
		testDefinitionFor(value);
	}

	@Test
	public void goToNoProfilePropertyValueExpression() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "other.property = ${test.prop|erty}\n" + //
				"%dev.test.property=hi\n" + //
				"%prop.test.property=hello\n" + //
				"test.property=salutations";
		testDefinitionFor(value, PROPERTY_DOCUMENT_NAME, //
				ll(PROPERTY_DOCUMENT_NAME, r(0, 19, 32), r(1, 0, 18)), //
				ll(PROPERTY_DOCUMENT_NAME, r(0, 19, 32), r(2, 0, 19)), //
				ll(PROPERTY_DOCUMENT_NAME, r(0, 19, 32), r(3, 0, 13) //
		));
	}

	@Test
	public void goToDevProfilePropertyValueExpression() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "other.property = ${%dev.test.prop|erty}\n" + //
				"%dev.test.property=hi\n" + //
				"%prop.test.property=hello\n" + //
				"test.property=salutations";
		testDefinitionFor(value, PROPERTY_DOCUMENT_NAME, ll(
			PROPERTY_DOCUMENT_NAME, r(0, 19, 37), r(1, 0, 18)
		));
	}
}
