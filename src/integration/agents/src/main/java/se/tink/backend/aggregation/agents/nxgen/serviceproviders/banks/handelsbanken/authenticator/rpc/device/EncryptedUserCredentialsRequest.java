package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.encryption.LibTFA;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.UserCredentialsRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EncryptedUserCredentialsRequest {
    private String encUserCredentials;

    private EncryptedUserCredentialsRequest() {}

    public static EncryptedUserCredentialsRequest create(
            InitNewProfileResponse initNewProfile,
            UserCredentialsRequest userCredentials,
            LibTFA tfa) {
        return new EncryptedUserCredentialsRequest()
                .setEncUserCredentials(
                        tfa.generateEncUserCredentials(initNewProfile, userCredentials));
    }

    private EncryptedUserCredentialsRequest setEncUserCredentials(String encUserCredentials) {
        this.encUserCredentials = encUserCredentials;
        return this;
    }
}
