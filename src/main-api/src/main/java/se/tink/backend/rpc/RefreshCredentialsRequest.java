package se.tink.backend.rpc;

import io.protostuff.Tag;
import java.util.List;
import se.tink.backend.core.Credentials;

public class RefreshCredentialsRequest {
    @Tag(1)
    private List<Credentials> credentials;

    public RefreshCredentialsRequest() {

    }

    public List<Credentials> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<Credentials> credentials) {
        this.credentials = credentials;
    }
}
