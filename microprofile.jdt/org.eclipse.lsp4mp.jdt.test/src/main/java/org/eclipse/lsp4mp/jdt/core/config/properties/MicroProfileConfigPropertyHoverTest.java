package org.eclipse.lsp4mp.jdt.core.config.properties;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.p;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

public class MicroProfileConfigPropertyHoverTest extends BasePropertiesManagerTest {

	@Test
	public void configPropertyHoverTest() throws Exception {

		IJavaProject project = loadMavenProject(MicroProfileMavenProjectName.config_hover);

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MicroProfileMavenProjectName.config_hover, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath, 6 /* properties from Java sources with ConfigProperty */ + //
				7 /* static properties from microprofile-context-propagation-api */ + //
				1 /* static property from microprofile config_ordinal */,

				p(null, "my.number", "int",
						"[org.acme.config.GreetingResource]("
								+ project.getUnderlyingResource().getLocationURI().toString()+"/src/main/java/org/acme/config/GreetingResource.java#13)",
						false, "org.acme.config.RequiresConfig", "number", null, 0, null));

	}
}
