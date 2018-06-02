package se.tink.backend.export.model.submodels;

public class ExportDocument {

    private final String mimeType;

    public ExportDocument(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}
