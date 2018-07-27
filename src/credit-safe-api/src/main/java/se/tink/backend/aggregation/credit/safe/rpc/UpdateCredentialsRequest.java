package se.tink.backend.aggregation.credit.safe.rpc;

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
