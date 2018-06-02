package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportCredential;

public class Credentials {

    private final List<ExportCredential> credentials;

    public Credentials(
            List<ExportCredential> credentials) {
        this.credentials = credentials;
    }

    public List<ExportCredential> getCredentials() {
        return credentials;
    }
}
