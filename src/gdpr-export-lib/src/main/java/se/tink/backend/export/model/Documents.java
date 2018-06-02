package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportDocument;

public class Documents {

    private final List<ExportDocument> documents;

    public Documents(List<ExportDocument> documents) {
        this.documents = documents;
    }

    public List<ExportDocument> getDocuments() {
        return documents;
    }
}
