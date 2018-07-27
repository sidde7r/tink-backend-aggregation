package se.tink.backend.aggregation.credit.safe.rpc;

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
