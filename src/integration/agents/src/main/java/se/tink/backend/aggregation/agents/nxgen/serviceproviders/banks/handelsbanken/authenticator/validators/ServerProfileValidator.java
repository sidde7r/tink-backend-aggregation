package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ServerProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public class ServerProfileValidator extends HandelsbankenValidator<BaseResponse> {

    public ServerProfileValidator(ServerProfileResponse serverProfile) {
        super(serverProfile);
    }

    public void validate() throws SessionException {

        if (HandelsbankenConstants.AutoAuthentication.Validation.INACTIVE_USER_PROFILE.equals(
                getCode())) {
            if (getMessage().toLowerCase().contains("appen är inte längre aktiv")) {
                throw HandelsbankenConstants.AutoAuthentication.UserError.BLOCKED_DUE_TO_INACTIVITY
                        .exception();
            }
            throw new IllegalStateException(
                    String.format(
                            "#login-refactoring- SHB - Login failed (serverProfileResponse) with message %s, code %s, error message %s",
                            getMessage(), getCode(), getFirstErrorMessage()));
        }
    }
}
