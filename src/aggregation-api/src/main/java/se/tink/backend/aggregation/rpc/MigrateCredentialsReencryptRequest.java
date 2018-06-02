package se.tink.backend.aggregation.rpc;

public class MigrateCredentialsReencryptRequest extends CredentialsRequest {
    @Override
    public boolean isManual() {
        return false;
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.UPDATE;
    }
}
