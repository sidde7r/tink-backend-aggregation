package se.tink.libraries.documentcontainer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.jersey.core.util.Base64;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

public class DocumentContainer {

    private String identifier;
    private String mimeType;
    private String base64Representation;

    public DocumentContainer() {}

    public DocumentContainer(String mimeType, InputStream documentInputStream) {
        this.mimeType = mimeType;
        try {
            base64Representation =
                    new String(Base64.encode(IOUtils.toByteArray(documentInputStream)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBase64Representation() {
        return base64Representation;
    }

    public void setBase64Representation(String base64Representation) {
        this.base64Representation = base64Representation;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @JsonIgnore
    public byte[] getBinaryDocument() {
        return Base64.decode(base64Representation);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
