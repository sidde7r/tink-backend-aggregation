package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.unregulated.authenticator;

import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class DemoBankIdAuthenticator implements BankIdAuthenticator<String> {
    public enum FailCauses {
        TIMEOUT,
        IN_PROGRESS,
        CANCELLED,
        UNKNOWN
    }

    private static final int EXPIRES_IN_SECONDS = 20 * 60; // 20min
    private static final String DEMO_USER_NAME = "18001212";
    private static final int TOTAL_ATTEMPTS = 5;

    private final boolean successfulAuthentication;
    private final FailCauses failureCause;

    private int attempt = 0;

    public DemoBankIdAuthenticator(boolean successfulAuthentication, FailCauses failCauses) {
        this.successfulAuthentication = successfulAuthentication;
        this.failureCause = failCauses;
    }

    @Override
    public String init(String ssn) throws BankIdException {
        if (Strings.isNullOrEmpty(ssn) || !ssn.startsWith(DEMO_USER_NAME)) {
            throw new BankIdException(BankIdError.USER_VALIDATION_ERROR);
        }

        return RandomStringUtils.randomAscii(10);
    }

    @Override
    public BankIdStatus collect(String reference) {
        if (attempt <= TOTAL_ATTEMPTS) {
            attempt++;
            return BankIdStatus.WAITING;
        }

        if (successfulAuthentication || Objects.isNull(failureCause)) {
            return BankIdStatus.DONE;
        }

        switch (failureCause) {
            case IN_PROGRESS:
                return BankIdStatus.CANCELLED;
            case TIMEOUT:
                return BankIdStatus.TIMEOUT;
            case CANCELLED:
                return BankIdStatus.CANCELLED;
            case UNKNOWN:
                // intentional fall through
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        // Returning an access token here will make the wrapping, generic,
        // BankIdAuthenticationController set the expiration time of the refresh token as the
        // sessionExpiry of the credentials. This can then be used to keep the session alive in the
        // SessionHandler.
        return Optional.of(OAuth2Token.createBearer("FAKE", null, EXPIRES_IN_SECONDS));
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.empty();
    }
}
