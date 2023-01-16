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
package org.eclipse.lsp4mp.commons.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;

import io.smallrye.common.expression.Expression;
import io.smallrye.common.expression.Expression.Flag;

/**
 * Expands property expressions to a final resolved value, while avoiding a few
 * pitfalls.
 *
 * Checks for cycles before attempting expansion, and mitigates OOM due to
 * Billion Laughs by counting the number of variable references.
 *
 * @author datho7561
 */
public class PropertyValueExpander {

	private static final Logger LOGGER = Logger.getLogger(PropertyValueExpander.class.getName());
	private static final long REFERENCE_UPPER_BOUND = 1_000_000;

	private final PropertyGraph propertyGraph;
	private final IConfigSourcePropertiesProvider properties;
	private final Map<String, ResolvedPropertyValueInformation> resolved;

	public PropertyValueExpander(IConfigSourcePropertiesProvider properties) {
		this.properties = properties;
		this.propertyGraph = new PropertyGraph(properties);
		this.resolved = new HashMap<>();
	}

	/**
	 * Returns the expanded value for the give key, or the unexpanded value if the
	 * value can't be expanded.
	 *
	 * @param key the key to get the value of
	 * @return the expanded value for the give key, or the unexpanded value if the
	 *         value can't be expanded.
	 */
	public String getValue(String key) {
		if (propertyGraph.isAcyclic()) {
			ResolvedPropertyValueInformation info = getResolvedValue(key);
			return info == null ? null : info.getValue();
		}
		return properties.getValue(key);
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
		return propertyGraph.getIndependentProperties(property);
	}

	private ResolvedPropertyValueInformation getResolvedValue(String key) {

		if (!properties.hasKey(key)) {
			return null;
		}

		if (resolved.get(key) != null) {
			return resolved.get(key);
		}

		final Counter referenceCounter = new Counter();
		String unresolvedValue = properties.getValue(key);

		Expression expr = Expression.compile(unresolvedValue, Flag.LENIENT_SYNTAX);
		String resolvedValue = expr.evaluate((context, builder) -> {
			referenceCounter.add(1);
			ResolvedPropertyValueInformation referencedKeyValueInformation = getResolvedValue(context.getKey());

			if (referencedKeyValueInformation == null || referencedKeyValueInformation.getValue() == null) {
				if (context.hasDefault()) {
					context.expandDefault();
				} else {
					builder.append("${" + context.getKey() + "}");
				}
			} else if (referencedKeyValueInformation.getExpansions() >= REFERENCE_UPPER_BOUND) {
				referenceCounter.add(REFERENCE_UPPER_BOUND);
				LOGGER.warning(MessageFormat.format(
						"Property expression expansion for key `{0}` has exceeded 1 million references, and as such, will not be attempted",
						key));
			} else {
				referenceCounter.add(referencedKeyValueInformation.getExpansions());
				builder.append(referencedKeyValueInformation.getValue());
			}
		});

		ResolvedPropertyValueInformation resolvedInfo = null;
		if (referenceCounter.getValue() >= REFERENCE_UPPER_BOUND) {
			resolvedInfo = new ResolvedPropertyValueInformation(unresolvedValue, referenceCounter.getValue());
			resolved.put(key, resolvedInfo);
		} else if (resolvedValue != null && resolvedValue.length() > 0) {
			resolvedInfo = new ResolvedPropertyValueInformation(resolvedValue, referenceCounter.getValue());
			resolved.put(key, resolvedInfo);
		}

		return resolvedInfo;

	}

	private static class PropertyGraph {

		private Graph<String> graph;
		private Optional<Boolean> acyclic;

		PropertyGraph(IConfigSourcePropertiesProvider properties) {
			MutableGraph<String> graph = GraphBuilder.directed().allowsSelfLoops(true).build();
			acyclic = Optional.empty();
			// add vertices
			for (String key : properties.keys()) {
				graph.addNode((String) key);
			}
			// add edges
			for (String key : properties.keys()) {
				String unresolvedValue = properties.getValue(key);
				if (StringUtils.hasText(unresolvedValue) && unresolvedValue.contains("${")) {
					Expression expr = Expression.compile(unresolvedValue, Flag.LENIENT_SYNTAX);
					expr.evaluate((resolver, builder) -> {
						if (graph.nodes().contains(resolver.getKey())) {
							graph.putEdge((String) key, resolver.getKey());
						}
						resolver.expandDefault();
					});
				}
			}

			this.graph = graph;
		}

		boolean isAcyclic() {
			if (!acyclic.isPresent()) {
				acyclic = Optional.of(!Graphs.hasCycle(graph));
			}
			return acyclic.get();
		}

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

	}

	/**
	 * Represents a resolved property value and the number of resolutions needed to
	 * reach
	 */
	private static class ResolvedPropertyValueInformation {
		private long expansions;
		private String value;

		ResolvedPropertyValueInformation(String value, long expansions) {
			this.value = value;
			this.expansions = expansions;
		}

		String getValue() {
			return this.value;
		}

		long getExpansions() {
			return this.expansions;
		}
	}

	private static class Counter {
		private long value = 0;

		Counter() {
		}

		void add(long amt) {
			value += amt;
		}

		long getValue() {
			return this.value;
		}
	}
}
