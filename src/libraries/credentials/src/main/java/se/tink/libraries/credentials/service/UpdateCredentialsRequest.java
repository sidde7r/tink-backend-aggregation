package se.tink.libraries.credentials.service;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.user.rpc.User;

public class UpdateCredentialsRequest extends CredentialsRequest {

    public UpdateCredentialsRequest() {}

    public UpdateCredentialsRequest(User user, Provider provider, Credentials credentials) {
        super(user, provider, credentials);
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.UPDATE;
    }

    @Override
    public boolean isManual() {
        return true;
    }
}
