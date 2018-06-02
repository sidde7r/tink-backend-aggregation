package se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation;

import se.tink.backend.aggregationcontroller.v1.rpc.entities.Credentials;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.User;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;

public class KeepAliveRequest extends CredentialsRequest {

    public KeepAliveRequest() {
    }

    public KeepAliveRequest(User user, Provider provider, Credentials credentials) {
        super(user, provider, credentials);
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.KEEP_ALIVE;
    }

    @Override
    public boolean isManual() {
        return false;
    }
}
