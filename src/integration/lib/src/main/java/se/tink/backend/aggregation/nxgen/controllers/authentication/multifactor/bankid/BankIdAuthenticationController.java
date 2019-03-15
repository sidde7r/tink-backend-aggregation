package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.log.AggregationLogger;

public class BankIdAuthenticationController<T> implements MultiFactorAuthenticator {
    private static final int MAX_ATTEMPTS = 90;

    private static final AggregationLogger log = new AggregationLogger(BankIdAuthenticationController.class);
    private final BankIdAuthenticator<T> authenticator;
    private final SupplementalRequester supplementalRequester;
    private final boolean waitOnBankId;

    public BankIdAuthenticationController(SupplementalRequester supplementalRequester, BankIdAuthenticator<T> authenticator) {
        this(supplementalRequester, authenticator, false);
    }

    public BankIdAuthenticationController(SupplementalRequester supplementalRequester, BankIdAuthenticator<T> authenticator,
            boolean waitOnBankId) {
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.supplementalRequester = Preconditions.checkNotNull(supplementalRequester);
        this.waitOnBankId = waitOnBankId;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.MOBILE_BANKID;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        NotImplementedException.throwIf(!Objects.equals(credentials.getType(), getType()),
                String.format("Authentication method not implemented for CredentialsType: %s", credentials.getType()));
        String ssn = credentials.getField(Field.Key.USERNAME);

        if (Strings.isNullOrEmpty(ssn)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        T reference = authenticator.init(ssn);

        supplementalRequester.openBankId(authenticator
                .getAutostartToken()
                .orElse(null),
                waitOnBankId);

        poll(reference);
    }

    private void poll(T reference) throws AuthenticationException, AuthorizationException {
        BankIdStatus status = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            status = authenticator.collect(reference);

            switch (status) {
                case DONE:
                    return;
                case WAITING:
                    log.info("Waiting for BankID");
                    break;
                case CANCELLED:
                    throw BankIdError.CANCELLED.exception();
                case NO_CLIENT:
                    throw BankIdError.NO_CLIENT.exception();
                case TIMEOUT:
                    throw BankIdError.TIMEOUT.exception();
                case INTERRUPTED:
                    throw BankIdError.INTERRUPTED.exception();
                default:
                    log.warn(String.format("Unknown BankIdStatus (%s)", status));
                    throw BankIdError.UNKNOWN.exception();
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        log.info(String.format("BankID timed out internally, last status: %s", status));
        throw BankIdError.TIMEOUT.exception();
    }
}
