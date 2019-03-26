package se.tink.backend.nasa.boot.rpc;

import java.util.List;

public abstract class CredentialsRequest {
    private Credentials credentials;
    private Provider provider;
    private User user;
    private String userDeviceId;
    private List<Account> accounts;

    public CredentialsRequest() {}

    public CredentialsRequest(Credentials credentials, Provider provider, User user) {
        this.credentials = credentials;
        this.provider = provider;
        this.user = user;
    }

    public abstract boolean isManual();

    public abstract CredentialsRequestType getType();

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUserDeviceId(String userDeviceId) {
        this.userDeviceId = userDeviceId;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
}
