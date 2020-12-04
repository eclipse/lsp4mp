/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.services.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.Node.NodeType;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.model.PropertyKey;
import org.eclipse.lsp4mp.utils.PositionUtils;
import org.eclipse.lsp4mp.utils.StringUtils;

/**
 * The properties file symbols provider
 *
 * @author Angelo ZERR
 *
 */
class PropertiesFileSymbolsProvider {

	/**
	 * Returns symbol information list for the given properties model.
	 *
	 * @param document      the properties model document
	 * @param cancelChecker the cancel checker
	 * @return symbol information list for the given properties model.
	 */
	public List<SymbolInformation> findSymbolInformations(PropertiesModel document, CancelChecker cancelChecker) {
		List<SymbolInformation> symbols = new ArrayList<>();
		for (Node node : document.getChildren()) {
			if (cancelChecker != null) {
				cancelChecker.checkCanceled();
			}
			if (node.getNodeType() == NodeType.PROPERTY) {
				// It's a property (not a comments)
				Property property = (Property) node;
				String name = getSymbolName(property);
				if (!StringUtils.isEmpty(name)) {
					// The property is not an empty line
					Range range = getSymbolRange(property);
					Location location = new Location(document.getDocumentURI(), range);
					SymbolInformation symbol = new SymbolInformation(name, getSymbolKind(property), location);
					symbols.add(symbol);
				}
			}
		}
		return symbols;
	}

	/**
	 * Returns document symbol list for the given properties model.
	 *
	 * @param document      the properties model document
	 * @param cancelChecker the cancel checker
	 * @return document symbol list for the given properties model.
	 */
	public List<DocumentSymbol> findDocumentSymbols(PropertiesModel document, CancelChecker cancelChecker) {
		List<DocumentSymbol> symbols = new ArrayList<>();
		for (Node node : document.getChildren()) {
			if (cancelChecker != null) {
				cancelChecker.checkCanceled();
			}
			if (node.getNodeType() == NodeType.PROPERTY) {
				// It's a property (not a comments)
				Property property = (Property) node;
				String name = getSymbolName(property);
				if (!StringUtils.isEmpty(name)) {
					// The property is not an empty line
					String[] paths = name.split("[.]");
					DocumentSymbol symbol = null;
					for (String path : paths) {
						symbol = getSymbol(path, property, symbol != null ? symbol.getChildren() : symbols);
					}
					if (symbol != null) {
						symbol.setKind(SymbolKind.Property);
						String value = property.getPropertyValue();
						if (value != null) {
							symbol.setDetail(value);
						}
					}
				}
			}
		}
		return symbols;
	}

	private static DocumentSymbol getSymbol(String path, Property property, List<DocumentSymbol> children) {
		for (DocumentSymbol child : children) {
			if (path.equals(child.getName())) {
				return child;
			}
		}
		Range range = getSymbolRange(property);
		DocumentSymbol symbol = new DocumentSymbol(path, SymbolKind.Package, range, range);
		symbol.setChildren(new ArrayList<>());
		children.add(symbol);
		return symbol;
	}

	private static String getSymbolName(Property property) {
		PropertyKey key = property.getKey();
		if (key == null) {
			return null;
		}
		return key.getPropertyNameWithProfile();
	}

	private static Range getSymbolRange(Property property) {
		return PositionUtils.createRange(property);
	}

	private static SymbolKind getSymbolKind(Property property) {
		return SymbolKind.Property;
	}

}
