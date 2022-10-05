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
package org.eclipse.lsp4mp.commons.utils;

import java.util.HashMap;

import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * JSONUtility
 */
public class JSONUtility {

    private JSONUtility() {
    }

    /**
     * Returns the given JSON object as the given class.
     * 
     * Uses a gson instance with some of the lsp4j adapters included but not all of
     * them. {@see toLsp4jModel} for a version that actually uses the correct
     * adapters.
     * 
     * @param <T>    the type of the resulting object
     * @param object the json object to convert
     * @param clazz  the class to convert the json object into
     * @return the given JSON object as the given class
     */
    @Deprecated
    public static <T> T toModel(Object object, Class<T> clazz) {
        return toModel(getDefaultGsonBuilder().create(), object, clazz);
    }

    private static <T> T toModel(Gson gson, Object object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        if (clazz == null) {
            throw new IllegalArgumentException("Class can not be null");
        }
        if (object instanceof JsonElement) {
            return gson.fromJson((JsonElement) object, clazz);
        }
        if (clazz.isInstance(object)) {
            return clazz.cast(object);
        }
        return gson.fromJson(gson.toJson(object), clazz);
    }

    /**
     * Converts given JSON objects to given Model objects using the same adapters as
     * lsp4j.
     *
     * @throws IllegalArgumentException if clazz is null
     */
    public static <T> T toLsp4jModel(Object object, Class<T> clazz) {
        return toModel(new MessageJsonHandler(new HashMap<>()).getGson(), object, clazz);
    }

    private static GsonBuilder getDefaultGsonBuilder() {
        return new GsonBuilder() //
                // required to deserialize XMLFormattingOptions which extends FormattingOptions
                // which uses Either
                .registerTypeAdapterFactory(new EitherTypeAdapter.Factory());
    }
}