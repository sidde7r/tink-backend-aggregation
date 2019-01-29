package se.tink.backend.aggregation.rpc;

import se.tink.libraries.user.rpc.User;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;

public class CreateCredentialsRequest extends CredentialsRequest {

    public CreateCredentialsRequest() {
    }
    
    public CreateCredentialsRequest(User user, Provider provider, Credentials credentials) {
        super(user, provider, credentials);
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.CREATE;
    }
    
    @Override
    public boolean isManual() {
        return true;
    }
}
