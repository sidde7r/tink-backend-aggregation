package se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation;

import se.tink.backend.common.aggregationcontroller.v1.enums.CredentialsRequestType;

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
