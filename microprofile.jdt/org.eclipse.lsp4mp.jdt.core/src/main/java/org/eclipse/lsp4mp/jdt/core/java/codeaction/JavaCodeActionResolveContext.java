package org.eclipse.lsp4mp.jdt.core.java.codeaction;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeActionParams;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;

/**
 * Like {@see JavaCodeActionContext}, but also includes the unresolved CodeAction 
 * 
 * @author datho7561
 */
public class JavaCodeActionResolveContext extends JavaCodeActionContext {

    private CodeAction unresolved;

    public JavaCodeActionResolveContext(ITypeRoot typeRoot, int selectionOffset, int selectionLength, IJDTUtils utils,
            MicroProfileJavaCodeActionParams params, CodeAction unresolved) {
        super(typeRoot, selectionOffset, selectionLength, utils, params);
        this.unresolved = unresolved;
    }

    /**
     * Returns the unresolved code action.
     * 
     * @return the unresolved code action
     */
    public CodeAction getUnresolved() {
        return this.unresolved;
    }

}