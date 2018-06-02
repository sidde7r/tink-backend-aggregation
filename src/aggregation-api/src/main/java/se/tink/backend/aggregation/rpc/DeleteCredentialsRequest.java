package se.tink.backend.aggregation.rpc;


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
