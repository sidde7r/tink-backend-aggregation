package se.tink.backend.aggregation.agents.nxgen.demo.banks.bankid;

import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class DemoBankIdAuthenticator implements BankIdAuthenticator<String>, PasswordAuthenticator {
    private static final AggregationLogger logger =
            new AggregationLogger(NextGenerationDemoAuthenticator.class);
    private static final Random RANDOM = new Random();

    private final Credentials credentials;
    private final boolean successfulAuthentication;
    private int attempt = 0;
    String demoUserName = "18001212";

    public DemoBankIdAuthenticator(Credentials credentials, boolean successfulAuthentication) {
        this.credentials = credentials;
        this.successfulAuthentication = successfulAuthentication;
    }

    @Override
    public String init(String ssn) throws BankIdException {
        if (Strings.isNullOrEmpty(ssn) || !ssn.startsWith(demoUserName)) {
            throw new BankIdException(BankIdError.USER_VALIDATION_ERROR);
        }

        return RandomStringUtils.randomAscii(10);
    }

    @Override
    public BankIdStatus collect(String reference) {
        BankIdStatus status;
        if (attempt > 3) {
            status = BankIdStatus.DONE;
        } else if (successfulAuthentication) {
            status = BankIdStatus.WAITING;
        } else {
            status = BankIdStatus.FAILED_UNKNOWN;
        }

        attempt++;
        return status;
    }

    @Override
    public Optional<String> getAutostartToken() {
        if (credentials.getProviderName().equals("se-test-bankid-qr-successful")) {
            int leftLimit = 48; // numeral '0'
            int rightLimit = 101; // letter 'e'
            int targetStringLength = 32;
            // Autostart token example: 8dce0218-9cc2-0263-3904-aec17576ec3a
            String generatedString =
                    RANDOM.ints(leftLimit, rightLimit + 1)
                            .filter(i -> (i <= 57 || i >= 97))
                            .limit(targetStringLength)
                            .collect(
                                    StringBuilder::new,
                                    StringBuilder::appendCodePoint,
                                    StringBuilder::append)
                            .toString()
                            .replaceAll("(.{8})(.{4})(.{4})(.{4})", "$1-$2-$3-$4-");
            return Optional.of(generatedString);
        }
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.empty();
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        if (!Objects.equals(password, "demo")) {
            logger.error(
                    String.format(
                            "Could not authenticate demo credentials (fields: %s)",
                            credentials.getFieldsSerialized()));

            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
