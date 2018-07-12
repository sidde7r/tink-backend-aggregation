package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class LoginDocument {

    private String document;
    private int documentType = IngConstants.Default.LOGIN_DOCUMENT_TYPE;

    public int getDocumentType() {
        return documentType;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public void setDocumentType(int documentType) {
        this.documentType = documentType;
    }

    public String getDocument() {
        return document;
    }
}
