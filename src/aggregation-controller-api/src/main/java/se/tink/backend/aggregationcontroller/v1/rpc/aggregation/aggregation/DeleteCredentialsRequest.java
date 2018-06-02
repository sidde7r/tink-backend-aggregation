package se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation;

import se.tink.backend.aggregationcontroller.v1.rpc.entities.Credentials;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.User;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;

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
