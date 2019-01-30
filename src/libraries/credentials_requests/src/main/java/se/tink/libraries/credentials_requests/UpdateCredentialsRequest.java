package se.tink.libraries.credentials_requests;

import se.tink.libraries.user.rpc.User;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;

public class UpdateCredentialsRequest extends CredentialsRequest {
    
    public UpdateCredentialsRequest() {
    }
    
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
