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
package org.eclipse.lsp4mp.jdt.internal.health;

/**
 * MicroProfile Health constants
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfileHealthConstants {

	private MicroProfileHealthConstants() {
	}

	/**
	 * Deprecated in microprofile-health 2.0, removed in microprofile-health 3.0
	 */
	@Deprecated
	public static final String HEALTH_ANNOTATION = "org.eclipse.microprofile.health.Health";
	public static final String READINESS_ANNOTATION = "org.eclipse.microprofile.health.Readiness";
	public static final String LIVENESS_ANNOTATION = "org.eclipse.microprofile.health.Liveness";

	public static final String HEALTH_CHECK_INTERFACE_NAME = "HealthCheck";

	public static final String HEALTH_CHECK_INTERFACE = "org.eclipse.microprofile.health."
			+ HEALTH_CHECK_INTERFACE_NAME;

	public static final String DIAGNOSTIC_SOURCE = "microprofile-health";
}
