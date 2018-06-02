package se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation;

import se.tink.backend.common.aggregationcontroller.v1.enums.CredentialsRequestType;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;

public class DeleteCredentialsRequest extends CredentialsRequest {
    public DeleteCredentialsRequest(User user, Provider provider, Credentials credentials) {
        super(user, provider, credentials);
    }

    public DeleteCredentialsRequest() {

    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.DELETE;
    }

    @Override
    public boolean isManual() {
        return true;
    }
}
