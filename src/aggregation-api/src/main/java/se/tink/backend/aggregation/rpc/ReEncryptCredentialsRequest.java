package se.tink.backend.aggregation.rpc;

public class ReEncryptCredentialsRequest extends CredentialsRequest {
    @Override
    public boolean isManual() {
        return true;
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.REENCRYPT;
    }
}
