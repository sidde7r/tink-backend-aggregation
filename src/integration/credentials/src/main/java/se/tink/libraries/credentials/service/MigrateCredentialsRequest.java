package se.tink.libraries.credentials.service;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.user.rpc.User;

public class MigrateCredentialsRequest extends CredentialsRequest {

    public MigrateCredentialsRequest() {}

    public MigrateCredentialsRequest(User user, Provider provider, Credentials credentials) {
        super(user, provider, credentials);
    }

    @Override
    public boolean isManual() {
        return false;
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.MIGRATE;
    }
}
