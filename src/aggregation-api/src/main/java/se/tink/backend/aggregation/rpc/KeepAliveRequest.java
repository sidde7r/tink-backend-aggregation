package se.tink.backend.aggregation.rpc;

import se.tink.backend.agents.rpc.User;

public class KeepAliveRequest extends CredentialsRequest {
    
    public KeepAliveRequest() {
    }
            
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
