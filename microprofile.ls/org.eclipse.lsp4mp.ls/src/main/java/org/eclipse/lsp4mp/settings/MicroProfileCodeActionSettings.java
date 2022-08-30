package org.eclipse.lsp4mp.settings;

import org.eclipse.lsp4j.CodeActionCapabilities;

public class MicroProfileCodeActionSettings {

	private CodeActionCapabilities codeActionCapabilities;
	
	public void setCapabilities(CodeActionCapabilities codeActionCapabilities) {
		this.codeActionCapabilities = codeActionCapabilities;
	}
	
	public CodeActionCapabilities getCodeActionCapabilities() {
		return this.codeActionCapabilities;
	}
	
	public boolean isCodeActionResolveSupported() {
		return codeActionCapabilities != null
				&& codeActionCapabilities.getDataSupport() != null
				&& codeActionCapabilities.getDataSupport().booleanValue()
				&& codeActionCapabilities.getResolveSupport() != null
				&& codeActionCapabilities.getResolveSupport().getProperties() != null
				&& codeActionCapabilities.getResolveSupport().getProperties().contains("edit");
	}
	
}
