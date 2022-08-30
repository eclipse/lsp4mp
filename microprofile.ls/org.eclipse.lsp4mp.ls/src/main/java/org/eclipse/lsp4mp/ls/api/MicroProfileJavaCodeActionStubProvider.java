package org.eclipse.lsp4mp.ls.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4mp.commons.JavaCodeActionStub;

/**
 * MicroProfile Java Code Action stub provider.
 * 
 * Provides "stubs" or templates to build the code actions provided by the java
 * language server component.
 * 
 * @author datho7561
 */
public interface MicroProfileJavaCodeActionStubProvider {

	@JsonRequest("microprofile/java/codeActionStub")
	default CompletableFuture<List<JavaCodeActionStub>> getJavaCodeActionStubs() {
		return CompletableFuture.completedFuture(null);
	}

}
