package se.tink.backend.aggregation.agents.nxgen.demo.banks.bankid;

import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.agents.rpc.Credentials;

public class DemoBankIdAuthenticator implements BankIdAuthenticator<String>, PasswordAuthenticator {
    private static final AggregationLogger log = new AggregationLogger(NextGenerationDemoAuthenticator.class);
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
        return Optional.empty();
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        if (!Objects.equals(password, "demo")) {
            log.error(String.format("Could not authenticate demo credentials (fields: %s)",
                    credentials.getFieldsSerialized()));

            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}