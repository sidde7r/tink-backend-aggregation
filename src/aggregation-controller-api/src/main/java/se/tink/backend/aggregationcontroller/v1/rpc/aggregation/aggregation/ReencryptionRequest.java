package se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation;

import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;

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

