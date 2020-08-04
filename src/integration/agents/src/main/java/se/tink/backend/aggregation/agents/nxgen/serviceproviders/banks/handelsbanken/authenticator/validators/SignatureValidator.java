package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public class SignatureValidator extends HandelsbankenValidator<BaseResponse> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String validSignatureResult;
    private final Credentials credentials;

    public SignatureValidator(
            ValidateSignatureResponse validateSignature,
            String validSignatureResult,
            Credentials credentials) {
        super(validateSignature);
        this.validSignatureResult = validSignatureResult;
        this.credentials = credentials;
    }

    public void validate() throws SessionException {
        String result = getResult();
        if (!validSignatureResult.equals(result)) {
            logger.warn(
                    String.format(
                            "#login-refactoring - SHB - Login failed (validateSignatureResponse/result) with "
                                    + "message %s, code %s, errors %s",
                            getMessage(), getCode(), getErrors()));
            throw HandelsbankenConstants.AutoAuthentication.UserError.INCORRECT_CREDENTIALS
                    .exception();
        }
    }
}
