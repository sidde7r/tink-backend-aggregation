package se.tink.backend.rpc;

import io.protostuff.Tag;
import se.tink.backend.core.Credentials;

public class FraudActivationResponse {

    @Tag(1)
    private Credentials credentials;

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
}
