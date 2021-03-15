package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.user.rpc.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MigrateCredentialsRequest extends CredentialsRequest {

    public MigrateCredentialsRequest() {}

    public MigrateCredentialsRequest(User user, Provider provider, Credentials credentials) {
        super(user, provider, credentials);
    }

    /**
     * @deprecated use UserAvailability's userPresent or userAvailableForInteraction depending on
     *     what you need
     */
    @Override
    @Deprecated
    public boolean isManual() {
        return false;
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.MIGRATE;
    }
}
