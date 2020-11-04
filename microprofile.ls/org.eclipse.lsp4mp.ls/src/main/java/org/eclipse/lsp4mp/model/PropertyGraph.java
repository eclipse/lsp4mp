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

package org.eclipse.lsp4mp.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lsp4mp.model.Node.NodeType;

import com.google.common.base.Optional;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;

/**
 * Represents the graph of dependencies between properties defined in a
 * MicroProfile properties file.
 */
public class PropertyGraph {

	private Graph<String> graph;
	private Optional<Boolean> acyclic;

	/**
	 * Build a PropertyGraph for the given properties model
	 *
	 * @param model the properties model to build the graph for
	 */
	public PropertyGraph(PropertiesModel model) {
		MutableGraph<String> graph = GraphBuilder.directed().allowsSelfLoops(true).build();
		acyclic = Optional.absent();
		// Add nodes
		for (Node modelChild : model.getChildren()) {
			if (modelChild.getNodeType() == NodeType.PROPERTY) {
				Property property = (Property) modelChild;
				String propertyName = property.getPropertyName();
				if (!(propertyName.isEmpty() || graph.nodes().contains(propertyName))) {
					graph.addNode(propertyName);
				}
			}
		}
		// Add edges
		for (Node modelChild : model.getChildren()) {
			if (modelChild.getNodeType() == NodeType.PROPERTY) {
				Property property = (Property) modelChild;
				if (property.getValue() != null) {
					for (Node valueNode : property.getValue().getChildren()) {
						if (valueNode.getNodeType() == NodeType.PROPERTY_VALUE_EXPRESSION) {
							PropertyValueExpression propExpr = (PropertyValueExpression) valueNode;
							String propName = property.getPropertyName();
							String refPropName = propExpr.getReferencedPropertyName();
							if (graph.nodes().containsAll(Arrays.asList(propName, refPropName))) {
								graph.putEdge(propName, refPropName);
							}
						}
					}
				}
			}
		}
		this.graph = graph;
	}

	/**
	 * Returns true if the given property is in the PropertyGraph, and false
	 * otherwise
	 *
	 * @param property the property that is being checked
	 * @return true if the given property is in the PropertyGraph, and false
	 *         otherwise
	 */
	public boolean hasNode(String property) {
		return this.graph.nodes().contains(property);
	}

	/**
	 * Gets a list of properties that do not depend on <code>property</code>.
	 *
	 * These are the properties that, if a dependency of <code>property</code> on
	 * them were introduced, wouldn't introduce a circular dependency.
	 *
	 * @param property The property the find the independent properties of.
	 * @return A list of all the properties whose value do not depend on this
	 *         property.
	 */
	public List<String> getIndependentProperties(String property) {
		Graph<String> reversed = getReversed();
		Set<String> reachable = new HashSet<>();
		List<String> unreachable = new ArrayList<>(graph.nodes().size());
		for (String reached : Traverser.forGraph(reversed).breadthFirst(property)) {
			reachable.add(reached);
		}
		for (String node : graph.nodes()) {
			if (!reachable.contains(node)) {
				unreachable.add(node);
			}
		}
		return unreachable;
	}

	/**
	 * Get a copy of this property graph with all the edges reversed.
	 *
	 * @return a copy of this property graph with all the edges reversed.
	 */
	private Graph<String> getReversed() {
		MutableGraph<String> mutableReversed = GraphBuilder.directed().allowsSelfLoops(true).build();
		for (String node : graph.nodes()) {
			mutableReversed.addNode(node);
		}
		for (EndpointPair<String> edge : graph.edges()) {
			mutableReversed.putEdge(edge.nodeV(), edge.nodeU());
		}
		return mutableReversed;
	}

	/**
	 * Returns true if the graph is acyclic, and false if it has a cycle.
	 *
	 * Uses lazy loading to speed up subsequent calls
	 *
	 * @return true if the graph is acyclic, and false if it has a cycle.
	 */
	public boolean isAcyclic() {
		if (!acyclic.isPresent()) {
			acyclic = Optional.of(!Graphs.hasCycle(graph));
		}
		return acyclic.get();
	}

}