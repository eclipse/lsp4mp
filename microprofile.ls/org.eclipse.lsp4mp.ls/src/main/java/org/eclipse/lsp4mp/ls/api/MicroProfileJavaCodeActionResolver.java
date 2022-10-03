package org.eclipse.lsp4mp.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

public interface MicroProfileJavaCodeActionResolver {
    
    @JsonRequest("microprofile/java/codeActionResolve")
    default CompletableFuture<CodeAction> resolveCodeAction(CodeAction e) {
        return CompletableFuture.completedFuture(null);
    }

}
