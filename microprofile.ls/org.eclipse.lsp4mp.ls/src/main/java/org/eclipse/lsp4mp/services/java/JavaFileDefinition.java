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
package org.eclipse.lsp4mp.services.java;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDefinitionParams;
import org.eclipse.lsp4mp.ls.api.MicroProfileJavaDefinitionProvider;
import org.eclipse.lsp4mp.ls.commons.TextDocument;
import org.eclipse.lsp4mp.ls.properties.IPropertiesModelProvider;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.utils.PositionUtils;
import org.eclipse.lsp4mp.utils.PropertiesFileUtils;

/**
 * Java file definition support.
 * 
 * @author Angelo ZERR
 *
 */
class JavaFileDefinition {

	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> findDefinition(
			TextDocument document, Position position, MicroProfileJavaDefinitionProvider definitionProvider,
			IPropertiesModelProvider propertiesModelProvider, boolean definitionLinkSupport) {
		MicroProfileJavaDefinitionParams javaParams = new MicroProfileJavaDefinitionParams(document.getUri(), position);
		return definitionProvider.getJavaDefinition(javaParams).thenApply(definitions -> {
			List<LocationLink> locations = definitions.stream() //
					.filter(definition -> definition.getLocation() != null) //
					.map(definition -> {
						LocationLink location = definition.getLocation();
						String propertyName = definition.getSelectPropertyName();
						if (propertyName != null) {
							Range targetRange = null;
							// The target range must be resolved
							String documentURI = location.getTargetUri();
							if (documentURI.endsWith(".properties")) {
								PropertiesModel model = propertiesModelProvider.getPropertiesModel(documentURI);
								if (model == null) {
									model = PropertiesFileUtils.loadProperties(documentURI);
								}
								if (model != null) {
									for (Node node : model.getChildren()) {
										if (node.getNodeType() == Node.NodeType.PROPERTY) {
											Property property = (Property) node;
											if (propertyName.equals(property.getPropertyNameWithProfile())) {
												targetRange = PositionUtils.createRange(property.getKey());
											}
										}
									}
								}
							}
							if (targetRange == null) {
								targetRange = new Range(new Position(0, 0), new Position(0, 0));
							}
							location.setTargetRange(targetRange);
							location.setTargetSelectionRange(targetRange);
						}
						return location;
					}).collect(Collectors.toList());
			if (definitionLinkSupport) {
				// I don't understand
				// return Either.forRight(locations);
			}
			return Either.forLeft(locations.stream() //
					.map((link) -> {
						return new Location(link.getTargetUri(), link.getTargetRange());
					}).collect(Collectors.toList()));
		});
	}
}
