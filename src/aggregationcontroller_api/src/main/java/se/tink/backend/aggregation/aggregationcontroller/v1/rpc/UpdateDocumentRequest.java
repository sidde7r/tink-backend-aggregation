package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import se.tink.libraries.documentcontainer.DocumentContainer;

public class UpdateDocumentRequest {
    private String userId;
    private DocumentContainer documentContainer;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public DocumentContainer getDocumentContainer() {
        return documentContainer;
    }

    public void setDocumentContainer(DocumentContainer documentContainer) {
        this.documentContainer = documentContainer;
    }
}
