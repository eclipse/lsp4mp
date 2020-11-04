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
package org.eclipse.lsp4mp.commons.metadata;

/**
 * A provider for a value.
 * 
 * @see https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html#value-providers
 * 
 */
public class ValueProvider {

	public static enum ValueProviderDefaultName {

		HANDLE_AS("handle-as");

		private final String name;

		private ValueProviderDefaultName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private String name;

	private ValueProviderParameter parameters;

	public String getName() {
		return name;
	}

	public ValueProviderParameter getParameters() {
		return parameters;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParameters(ValueProviderParameter parameters) {
		this.parameters = parameters;
	}

}
