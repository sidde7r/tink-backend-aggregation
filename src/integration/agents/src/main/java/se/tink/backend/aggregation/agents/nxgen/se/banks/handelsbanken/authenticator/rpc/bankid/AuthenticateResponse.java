package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.URL;

public class AuthenticateResponse extends BaseResponse {

    private static final AggregationLogger LOGGER = new AggregationLogger(AuthenticateResponse.class);

    public BankIdStatus toBankIdStatus() throws AuthenticationException {
        String authenticateResult = getResult();
        if (HandelsbankenSEConstants.BankIdAuthentication.DONE.equalsIgnoreCase(authenticateResult)) {
            return BankIdStatus.DONE;
        }
        if (HandelsbankenSEConstants.BankIdAuthentication.MUST_ACTIVATE.equalsIgnoreCase(authenticateResult)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    HandelsbankenSEConstants.BankIdUserMessages.ACTIVATION_NEEDED);
        }

        String statusCode = getCode();

        if (!Strings.isNullOrEmpty(statusCode)) {
            LOGGER.info(String.format(
                    "BankID authentication failed with response: %s",
                    MoreObjects.toStringHelper(this)
                            .add("result", authenticateResult)
                            .add("code", statusCode)
                            .add("message", getMessage())
                            .add("errors", getErrors())
            ));
            switch (statusCode) {
                case HandelsbankenSEConstants.BankIdAuthentication.UNKNOWN_BANKID:
                    throw BankIdError.BLOCKED.exception();
                case HandelsbankenSEConstants.BankIdAuthentication.CANCELLED:
                    return BankIdStatus.CANCELLED;
            case HandelsbankenSEConstants.BankIdAuthentication.TIMEOUT:
                return BankIdStatus.TIMEOUT;
            }

        }

        return BankIdStatus.WAITING;
    }

    public URL toAuthorize() {
        return findLink(HandelsbankenConstants.URLS.Links.AUTHORIZE);
    }
}
