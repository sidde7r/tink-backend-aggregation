package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CommitProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public class CommitProfileResponseValidator extends HandelsbankenValidator<BaseResponse> {
    public CommitProfileResponseValidator(CommitProfileResponse commitProfile) {
        super(commitProfile);
    }

    public void validate() throws AuthenticationException, AuthorizationException {
        String statusCode = getCode();
        if (!Strings.isNullOrEmpty(statusCode)) {
            HandelsbankenConstants.DeviceAuthentication.BankCheckedUserError.throwException(this, () ->
                    new IllegalStateException(String.format(
                            "#login-refactoring - SHB - Login failed (commitProfileResponse) with message %s, code %s, error message %s",
                            getMessage(),
                            statusCode,
                            getFirstErrorMessage()))
            );
        }
    }
}
