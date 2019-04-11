package se.tink.backend.aggregation.rpc;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;
import se.tink.libraries.user.rpc.User;

public class KeepAliveRequest extends CredentialsRequest {

    public KeepAliveRequest() {}

    public KeepAliveRequest(User user, Provider provider, Credentials credentials) {
        super(user, provider, credentials);
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.KEEP_ALIVE;
    }

    @Override
    public boolean isManual() {
        return false;
    }
}
