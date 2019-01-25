package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ChallengeResponse;
import se.tink.backend.agents.rpc.Credentials;

public class ChallengeResponseValidator extends DeviceSecurityContextIdValidator {

    public ChallengeResponseValidator(Credentials credentials, ChallengeResponse challenge) {
        super(credentials, challenge);
    }

    public void validate() throws SessionException {
        validate(() -> new IllegalStateException(
                String.format(
                        "#login-refactoring- SHB - Login failed (ChallengeResponse) with message %s, code %s,"
                                + " errors %s",
                        getMessage(),
                        getCode(),
                        getErrors())));
    }

}
