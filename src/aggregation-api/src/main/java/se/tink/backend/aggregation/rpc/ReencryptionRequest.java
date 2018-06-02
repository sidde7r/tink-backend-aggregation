package se.tink.backend.aggregation.rpc;

public class ReencryptionRequest extends CredentialsRequest {
    @Override
    public boolean isManual() {
        return false;
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.REENCRYPT;
    }
}
