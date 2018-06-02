package se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation;

import se.tink.backend.common.aggregationcontroller.v1.enums.CredentialsRequestType;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;

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
