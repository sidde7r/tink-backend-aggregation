package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.agents.rpc.Credentials;

public class ValidateSignatureValidator extends DeviceSecurityContextIdValidator {

    public ValidateSignatureValidator(Credentials credentials, ValidateSignatureResponse validateSignature) {
        super(credentials, validateSignature);
    }

    public void validate() throws SessionException{
        validate(() -> new IllegalStateException(
                String.format(
                        "#login-refactoring- SHB - Login failed (validateSignatureResponse) with message %s, code %s, error message %s",
                        getMessage(),
                        getCode(),
                        getFirstErrorMessage())));

    }

}
