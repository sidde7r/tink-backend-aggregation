package se.tink.backend.aggregationcontroller.v1.rpc.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentContainer {
    private String identifier;
    private String mimeType;
    private String base64Representation;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getBase64Representation() {
        return base64Representation;
    }

    public void setBase64Representation(String base64Representation) {
        this.base64Representation = base64Representation;
    }
}
