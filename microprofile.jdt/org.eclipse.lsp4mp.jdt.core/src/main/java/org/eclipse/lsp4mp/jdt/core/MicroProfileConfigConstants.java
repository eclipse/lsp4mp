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
package org.eclipse.lsp4mp.jdt.core;

/**
 * MicroProfile Config constants
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfileConfigConstants {

	private MicroProfileConfigConstants() {
	}

	public static final String MICRO_PROFILE_CONFIG_DIAGNOSTIC_SOURCE = "microprofile-config";

	public static final String INJECT_ANNOTATION = "javax.inject.Inject";

	// MicroProfile Core annotations

	public static final String CONFIG_PROPERTY_ANNOTATION = "org.eclipse.microprofile.config.inject.ConfigProperty";

	public static final String CONFIG_PROPERTY_ANNOTATION_NAME = "name";

	public static final String CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE = "defaultValue";

}
