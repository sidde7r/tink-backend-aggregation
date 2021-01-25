package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;

@JsonIgnoreProperties(ignoreUnknown = true)
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
