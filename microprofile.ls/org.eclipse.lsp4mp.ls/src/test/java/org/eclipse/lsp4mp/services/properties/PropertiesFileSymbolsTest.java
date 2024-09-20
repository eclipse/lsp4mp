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

import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.ds;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.r;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.s;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.testDocumentSymbolsFor;
import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.testSymbolInformationsFor;

import java.util.Arrays;

import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.junit.Test;

/**
 * Test with symbols in 'microprofile-config.properties' file.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesFileSymbolsTest {

	@Test
	public void symbolsInformation() throws BadLocationException {
		String value = "# quarkus.datasource.url=jdbc:postgresql:quarkus_test\n" + //
				"quarkus.datasource.driver=org.postgresql.Driver\n" + //
				"quarkus.datasource.username=quarkus_test\n" + //
				"quarkus.datasource.password=quarkus_test\n" + //
				"\n" + //
				"       \n" + //
				"quarkus.datasource.max-size\n" + //
				"quarkus.datasource.min-size=\n" + //
				"\n" + //
				"";
		testSymbolInformationsFor(value, //
				s("quarkus.datasource.driver", SymbolKind.Property, "microprofile-config.properties", r(1, 0, 47)), //
				s("quarkus.datasource.username", SymbolKind.Property, "microprofile-config.properties", r(2, 0, 40)), //
				s("quarkus.datasource.password", SymbolKind.Property, "microprofile-config.properties", r(3, 0, 40)), //
				s("quarkus.datasource.max-size", SymbolKind.Property, "microprofile-config.properties", r(6, 0, 27)), //
				s("quarkus.datasource.min-size", SymbolKind.Property, "microprofile-config.properties", r(7, 0, 28)) //
		);
	};

	@Test
	public void documentSymbols() throws BadLocationException {
		String value = "# quarkus.datasource.url=jdbc:postgresql:quarkus_test\n" + //
				"quarkus.datasource.driver=org.postgresql.Driver\n" + //
				"quarkus.hibernate-orm.database.generation=drop-and-create\n" + //
				"quarkus.hibernate-orm.log.sql=true\n" + //
				"\n" + //
				"       \n" + //
				"quarkus.datasource.max-size\n" + //
				"quarkus.datasource.min-size=\n" + //
				"%dev.quarkus.datasource.max-size=2\n" + //
				"%dev.quarkus.datasource.min-size=1\n" + //
				"\n" + //
				"";
		testDocumentSymbolsFor(value, //
				ds("quarkus", SymbolKind.Package, r(1, 0, 47), null, //
						Arrays.asList( //
								ds("datasource", SymbolKind.Package, r(1, 0, 47), null, //
										Arrays.asList( //
												ds("driver", SymbolKind.Property, r(1, 0, 47), "org.postgresql.Driver"), //
												ds("max-size", SymbolKind.Property, r(6, 0, 27), null), //
												ds("min-size", SymbolKind.Property, r(7, 0, 28), "") //
										)), //
								ds("hibernate-orm", SymbolKind.Package, r(2, 0, 57), null, //
										Arrays.asList( //
												ds("database", SymbolKind.Package, r(2, 0, 57), null, //
														Arrays.asList(ds("generation", SymbolKind.Property, r(2, 0, 57),
																"drop-and-create"))), //
												ds("log", SymbolKind.Package, r(3, 0, 34), null, //
														Arrays.asList( //
																ds("sql", SymbolKind.Property, r(3, 0, 34), "true"))) //
										)))), //
				ds("%dev", SymbolKind.Package, r(8, 0, 34), null, //
						Arrays.asList( //
								ds("quarkus", SymbolKind.Package, r(8, 0, 34), null, //
										Arrays.asList(ds("datasource", SymbolKind.Package, r(8, 0, 34), null, //
												Arrays.asList( //
														ds("max-size", SymbolKind.Property, r(8, 0, 34), "2"), //
														ds("min-size", SymbolKind.Property, r(9, 0, 34), "1") //
												)))))) //
		);
	};

	@Test
	public void multiLineKeyValueSymbols() throws BadLocationException {
		String value = "quarkus.\\\n" + //
				"application.name = quarkus \\\n" + //
				"application \\\n" + //
				"name";
		testSymbolInformationsFor(value, //
				s("quarkus.application.name", SymbolKind.Property, "microprofile-config.properties", r(0, 0, 3, 4)));
	};

	@Test
	public void justPeriod() throws BadLocationException {
		String value = ".";
		testDocumentSymbolsFor(value, ds(".", SymbolKind.Property, r(0, 0, 1), null));
	}

	@Test
	public void twoPeriods() throws BadLocationException {
		String value = "a..b";
		testDocumentSymbolsFor(value, //
				ds("a", SymbolKind.Package, r(0, 0, 4), null, //
						Arrays.asList( //
								ds(".b", SymbolKind.Property, r(0, 0, 4), null))));
	}

	@Test
	public void threePeriods() throws BadLocationException {
		String value = "a...b";
		testDocumentSymbolsFor(value, //
				ds("a", SymbolKind.Package, r(0, 0, 5), null, //
						Arrays.asList( //
								ds(".", SymbolKind.Package, r(0, 0, 5), null, //
										Arrays.asList( //
												ds("b", SymbolKind.Property, r(0, 0, 5), null))))));
	}
	
	@Test
	public void fourPeriods() throws BadLocationException {
		String value = "a....b";
		testDocumentSymbolsFor(value, //
				ds("a", SymbolKind.Package, r(0, 0, 6), null, //
						Arrays.asList( //
								ds(".", SymbolKind.Package, r(0, 0, 6), null, //
										Arrays.asList( //
												ds(".b", SymbolKind.Property, r(0, 0, 6), null))))));
	}
	
	@Test
	public void propertiesWhichStartWithPeriod() throws BadLocationException {
		String value = ".quarkus\n" + //
				"  .quarkus2\n" + //
				"quarkus.\n" + //
				".mp\n" + //
				"  .mp2\n";
		testDocumentSymbolsFor(value, //
				ds(".quarkus", SymbolKind.Property, r(0, 0, 8), null), //
				ds(".quarkus2", SymbolKind.Property, r(1, 2, 11), null), //
				ds("quarkus", SymbolKind.Property, r(2, 0, 8), null), //
				ds(".mp", SymbolKind.Property, r(3, 0, 3), null), //
				ds(".mp2", SymbolKind.Property, r(4, 2, 6), null));
	}

	@Test
	public void justEquals() throws BadLocationException {
		String value = "=";
		testDocumentSymbolsFor(value);
	}

}
