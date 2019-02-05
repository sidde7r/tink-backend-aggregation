package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.agents.rpc.Credentials;

public class SignatureValidator extends HandelsbankenValidator<BaseResponse> {

    private static final AggregationLogger LOGGER = new AggregationLogger(SignatureValidator.class);

    private final String validSignatureResult;
    private final Credentials credentials;

    public SignatureValidator(ValidateSignatureResponse validateSignature, String validSignatureResult,
            Credentials credentials) {
        super(validateSignature);
        this.validSignatureResult = validSignatureResult;
        this.credentials = credentials;
    }

    public void validate() throws SessionException {
        String result = getResult();
        if (!validSignatureResult.equals(result)) {
            LOGGER.warn(String.format("#login-refactoring - SHB - Login failed (validateSignatureResponse/result) with "
                                    + "message %s, code %s, errors %s, result %s",
                            getMessage(),
                            getCode(),
                            getErrors(),
                            result));
            throw HandelsbankenConstants.AutoAuthentication.UserError.INCORRECT_CREDENTIALS.exception();
        }

    }
}
