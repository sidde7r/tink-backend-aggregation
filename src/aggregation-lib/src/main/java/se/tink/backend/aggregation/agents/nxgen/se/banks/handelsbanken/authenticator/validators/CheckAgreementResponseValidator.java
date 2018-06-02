package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.validators;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.device.CheckAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators.HandelsbankenValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public class CheckAgreementResponseValidator extends HandelsbankenValidator<BaseResponse> {
    public CheckAgreementResponseValidator(CheckAgreementResponse checkAgreement) {
        super(checkAgreement);
    }

    public void validate() throws AuthenticationException {
        String result = getResult();
        if ("NOT EXIST".equalsIgnoreCase(result)) {
            HandelsbankenConstants.DeviceAuthentication.OtherUserError.CODE_ACTIVATION_NEEDED.throwException();
        } else if (!"EXIST".equalsIgnoreCase(result)) {
            throw new IllegalStateException(
                    String.format(
                            "#" + HandelsbankenSEConstants.Authentication.SE_LOGIN_REFACTORING
                                    + " - Login failed (checkAgreementResponse) with message %s, code %s, error message %s, result %s",
                            getMessage(),
                            getCode(),
                            getFirstErrorMessage(),
                            getResult()));
        }

    }
}
