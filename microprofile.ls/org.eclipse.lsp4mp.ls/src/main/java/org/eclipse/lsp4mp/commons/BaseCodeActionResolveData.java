/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.commons;

import java.util.Objects;

/**
 * Represents the minimum amount of supplementary data needed to resolve the
 * edits for a code action.
 * 
 * @author datho7561
 */
public class BaseCodeActionResolveData {

    /**
     * The unique id of the {@link IJavaCodeActionParticipant} that can resolve this
     * code action
     */
    private String participantId;

    /**
     * The uri of the document that the diagnostic that this code action addresses
     * comes from
     */
    private String documentUri;

    /**
     * Needed for GSON
     */
    public BaseCodeActionResolveData() {
        this(null, null);
    }

    public BaseCodeActionResolveData(String participantId, String documentUri) {
        this.participantId = participantId;
        this.documentUri = documentUri;
    }

    public String getParticipantId() {
        return this.participantId;
    }

    public String getDocumentUri() {
        return this.documentUri;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BaseCodeActionResolveData)) {
            return false;
        }

        BaseCodeActionResolveData that = (BaseCodeActionResolveData) other;

        return Objects.equals(this.documentUri, that.documentUri)
                && Objects.equals(this.participantId, that.participantId);
    }

}
