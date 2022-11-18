package org.acme.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

public class RequiresConfig {

	/**
	 * {@link org.acme.config.GreetingResource}
	 */
	@ConfigProperty(name="my.number")
	public int number;

}