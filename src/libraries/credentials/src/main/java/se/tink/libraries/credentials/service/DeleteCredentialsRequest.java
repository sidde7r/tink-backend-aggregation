package se.tink.libraries.credentials.service;

import se.tink.libraries.user.rpc.User;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;

public class DeleteCredentialsRequest extends CredentialsRequest {
    public DeleteCredentialsRequest(User user, Provider provider, Credentials credentials) {
        super(user, provider, credentials);
    }
    
    public DeleteCredentialsRequest() {
        
    }
    
    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.DELETE;
    }

    @Override
    public boolean isManual() {
        return true;
    }
}
