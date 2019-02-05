package se.tink.backend.aggregation.rpc;

import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;

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
