package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.validators;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device.SecurityCardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators.HandelsbankenValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public class SecurityCardResponseValidator extends HandelsbankenValidator<BaseResponse> {

    public SecurityCardResponseValidator(SecurityCardResponse securityCard) {
        super(securityCard);
    }

    public void validate() throws AuthenticationException, AuthorizationException {
        String statusCode = getCode();

        if (!Strings.isNullOrEmpty(statusCode)) {
            HandelsbankenConstants.DeviceAuthentication.BankCheckedUserError.throwException(
                    this,
                    () ->
                            new IllegalStateException(
                                    String.format(
                                            HandelsbankenFIConstants.Authentication.LOG_TAG
                                                    + " - Login failed "
                                                    + "(securityCardResponse) with message "
                                                    + "%s, code %s, error message %s",
                                            getMessage(),
                                            statusCode,
                                            getFirstErrorMessage())));
        }
    }
}
