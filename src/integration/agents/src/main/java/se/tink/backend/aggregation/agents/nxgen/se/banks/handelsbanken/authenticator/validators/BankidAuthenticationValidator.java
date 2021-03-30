package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.validators;

import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators.HandelsbankenValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.libraries.i18n.LocalizableKey;

public class BankidAuthenticationValidator extends HandelsbankenValidator<BaseResponse> {
    private final Credentials credentials;

    public BankidAuthenticationValidator(Credentials credentials, AuthorizeResponse response) {
        super(response);
        this.credentials = credentials;
    }

    public void validate() throws AuthorizationException, LoginException {
        String code = getCode();
        if (!Strings.isNullOrEmpty(code)) {
            switch (code) {
                case HandelsbankenSEConstants.BankIdAuthentication.INSUFFICIENT_PERMISSIONS:
                    throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(
                            new LocalizableKey(
                                    "You lack the sufficient permissions for this service."));
                default:
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }
    }
}
