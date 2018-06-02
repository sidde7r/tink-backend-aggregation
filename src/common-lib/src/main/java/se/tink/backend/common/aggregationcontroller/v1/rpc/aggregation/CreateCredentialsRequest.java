package se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation;

import se.tink.backend.common.aggregationcontroller.v1.enums.CredentialsRequestType;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;

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
