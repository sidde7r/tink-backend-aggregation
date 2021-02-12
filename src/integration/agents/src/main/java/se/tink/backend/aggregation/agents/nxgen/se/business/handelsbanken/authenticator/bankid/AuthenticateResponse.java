package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants.BankIdAuthentication;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants.BankIdErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants.BankIdUserMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class AuthenticateResponse extends BaseResponse {
    private static final org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public BankIdStatus toBankIdStatus() throws AuthenticationException {
        String authenticateResult = getResult();
        if (BankIdAuthentication.DONE.equalsIgnoreCase(authenticateResult)) {
            return BankIdStatus.DONE;
        }
        if (BankIdAuthentication.MUST_ACTIVATE.equalsIgnoreCase(authenticateResult)) {
            throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(
                    BankIdUserMessages.ACTIVATION_NEEDED);
        }

        String statusCode = getCode();

        if (!Strings.isNullOrEmpty(statusCode)) {
            switch (statusCode) {
                case BankIdAuthentication.UNKNOWN_BANKID:
                    throw BankIdError.BLOCKED.exception();
                case BankIdAuthentication.CANCELLED:
                    return BankIdStatus.CANCELLED;
                case BankIdAuthentication.TIMEOUT:
                    return BankIdStatus.TIMEOUT;
                case BankIdAuthentication.FAILED_UNKNOWN:
                    return errorToBankIdStatus(getMessage());
            }
        }

        return BankIdStatus.WAITING;
    }

    private BankIdStatus errorToBankIdStatus(String message) {
        if (Strings.isNullOrEmpty(message)) {
            return BankIdStatus.FAILED_UNKNOWN;
        }

        if (BankIdErrorMessages.FAILED_TRY_AGAIN.equalsIgnoreCase(message)) {
            return BankIdStatus.TIMEOUT;
        } else if (BankIdErrorMessages.CANCELLED.equalsIgnoreCase(message)) {
            return BankIdStatus.CANCELLED;
        }

        logger.info("Status FAILED_UNKNOWN, message: " + message);
        return BankIdStatus.FAILED_UNKNOWN;
    }

    public URL toAuthorize() {
        return findLink(HandelsbankenConstants.URLS.Links.AUTHORIZE);
    }
}
