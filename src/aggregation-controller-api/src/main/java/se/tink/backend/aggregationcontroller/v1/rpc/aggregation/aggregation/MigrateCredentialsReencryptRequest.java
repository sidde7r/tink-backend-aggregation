package se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation;

import se.tink.backend.aggregationcontroller.v1.rpc.entities.Credentials;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.User;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;

public class MigrateCredentialsReencryptRequest extends CredentialsRequest {
    public MigrateCredentialsReencryptRequest() {
    }

    public MigrateCredentialsReencryptRequest(User user,
            Provider provider,
            Credentials credentials) {
        super(user, provider, credentials);
    }

    @Override
    public boolean isManual() {
        return false;
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.UPDATE;
    }
}
