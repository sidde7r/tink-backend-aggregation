package se.tink.backend.aggregation.nxgen.agents.demo;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.rpc.Credentials;

public class NextGenerationDemoAuthenticator implements BankIdAuthenticator<String>, PasswordAuthenticator {
    private static final AggregationLogger log = new AggregationLogger(NextGenerationDemoAuthenticator.class);
    private final Credentials credentials;

    private int attempt = 0;

    public NextGenerationDemoAuthenticator(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public String init(String ssn) throws BankIdException {
        System.out.println(String.format("Init BankID with social security number: \"%s\"", ssn));
        return RandomStringUtils.randomAscii(10);
    }

    @Override
    public BankIdStatus collect(String reference) {
        System.out.println(String.format("Collect BankID with order reference: \"%s\"", reference));

        if (!Objects.equals(credentials.getProviderName(), "demo-bankid")) {
            return BankIdStatus.DONE;
        }

        BankIdStatus status = BankIdStatus.NO_CLIENT;

        if (attempt > 3) {
            status = BankIdStatus.DONE;
        } else if (attempt > 1) {
            status = BankIdStatus.WAITING;
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
