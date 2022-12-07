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
package org.eclipse.lsp4mp.snippets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4mp.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4mp.ls.commons.snippets.ISnippetContext;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * A snippet context for Java files which matches java scope and dependency.
 *
 * @author Angelo ZERR
 *
 */
public class SnippetContextForJava implements ISnippetContext<ProjectLabelInfoEntry> {

	public static final TypeAdapter<SnippetContextForJava> TYPE_ADAPTER = new SnippetContextForJavaAdapter();

	/**
	 * A list of all types whose presence indicates the snippet should be displayed.
	 */
	private List<String> types;

	/**
	 * A list of all types whose presence indicates the snippet shouldn't be
	 * displayed.
	 *
	 * Takes precedence over {@link #types} i.e. if a type is present in
	 * {@link #exclusionTypes}, then the snippet won't be shown, and {@link #types} won't be considered.
	 */
	private List<String> exclusionTypes;

	public SnippetContextForJava(List<String> types, List<String> exclusionTypes) {
		this.types = types;
		this.exclusionTypes = exclusionTypes;
	}

	public List<String> getTypes() {
		return types;
	}

	public List<String> getExcludedTypes() {
		return this.exclusionTypes;
	}

	@Override
	public boolean isMatch(ProjectLabelInfoEntry context) {
		if (context == null) {
			return true;
		}
		if (exclusionTypes != null && !exclusionTypes.isEmpty()) {
			for (String type : exclusionTypes) {
				if (context.hasLabel(type)) {
					return false;
				}
			}
		}
		if (types == null || types.isEmpty()) {
			return true;
		}
		for (String type : types) {
			if (context.hasLabel(type)) {
				return true;
			}
		}
		return false;
	}

	private static class SnippetContextForJavaAdapter extends TypeAdapter<SnippetContextForJava> {

		@Override
		public SnippetContextForJava read(final JsonReader in) throws IOException {
			JsonToken nextToken = in.peek();
			if (nextToken == JsonToken.NULL) {
				return null;
			}

			List<String> types = new ArrayList<>();
			List<String> exclusionTypes = new ArrayList<>();
			in.beginObject();
			while (in.hasNext()) {
				String name = in.nextName();
				switch (name) {
				case "type":
					if (in.peek() == JsonToken.BEGIN_ARRAY) {
						in.beginArray();
						while (in.peek() != JsonToken.END_ARRAY) {
							types.add(in.nextString());
						}
						in.endArray();
					} else {
						types.add(in.nextString());
					}
					break;
				case "exclusionType":
					if (in.peek() == JsonToken.BEGIN_ARRAY) {
						in.beginArray();
						while (in.peek() != JsonToken.END_ARRAY) {
							exclusionTypes.add(in.nextString());
						}
						in.endArray();
					} else {
						exclusionTypes.add(in.nextString());
					}
					break;
				default:
					in.skipValue();
				}
			}
			in.endObject();
			return new SnippetContextForJava(types, exclusionTypes);
		}

		@Override
		public void write(JsonWriter out, SnippetContextForJava value) throws IOException {
			// Do nothing
		}
	}

}