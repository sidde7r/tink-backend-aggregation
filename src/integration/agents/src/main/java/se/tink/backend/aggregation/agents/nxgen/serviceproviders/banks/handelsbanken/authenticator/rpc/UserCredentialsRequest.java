package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.UserCredentials;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserCredentialsRequest {

    private UserCredentials userCredentials;

    private UserCredentialsRequest() {}

    public static UserCredentialsRequest create(String userId, String code) {
        return new UserCredentialsRequest()
                .setUserCredentials(UserCredentials.create(userId, code));
    }

    private UserCredentialsRequest setUserCredentials(UserCredentials userCredentials) {
        this.userCredentials = userCredentials;
        return this;
    }
}
