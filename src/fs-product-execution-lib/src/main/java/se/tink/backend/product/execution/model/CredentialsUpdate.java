package se.tink.backend.product.execution.model;

import se.tink.backend.core.Credentials;

public class CredentialsUpdate {
    private final Credentials credentials;
    private final String userDeviceId;

    public CredentialsUpdate(Credentials credentials, String userDeviceId) {
        this.credentials = credentials;
        this.userDeviceId = userDeviceId;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public String getUserDeviceId() {
        return userDeviceId;
    }

}
