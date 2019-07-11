package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BankIdAuthenticationController<T>
        implements AutoAuthenticator, MultiFactorAuthenticator {
    private static final int MAX_ATTEMPTS = 90;

    private static final AggregationLogger log =
            new AggregationLogger(BankIdAuthenticationController.class);
    private final BankIdAuthenticator<T> authenticator;
    private final SupplementalRequester supplementalRequester;
    private final boolean waitOnBankId;

    private final PersistentStorage persistentStorage;

    public BankIdAuthenticationController(
            SupplementalRequester supplementalRequester,
            BankIdAuthenticator<T> authenticator,
            PersistentStorage persistentStorage) {
        this(supplementalRequester, authenticator, false, persistentStorage);
    }

    public BankIdAuthenticationController(
            SupplementalRequester supplementalRequester,
            BankIdAuthenticator<T> authenticator,
            boolean waitOnBankId,
            PersistentStorage persistentStorage) {
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.supplementalRequester = Preconditions.checkNotNull(supplementalRequester);
        this.waitOnBankId = waitOnBankId;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.MOBILE_BANKID;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));

        String ssn = "";

        if (credentials.hasField(Field.Key.USERNAME)) {
            ssn = credentials.getField(Field.Key.USERNAME);

            if (Strings.isNullOrEmpty(ssn)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }

        T reference = authenticator.init(ssn);

        supplementalRequester.openBankId(
                authenticator.getAutostartToken().orElse(null), waitOnBankId);

        poll(reference);
    }

    private void poll(T reference) throws AuthenticationException, AuthorizationException {
        BankIdStatus status = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            status = authenticator.collect(reference);

            switch (status) {
                case DONE:
                    authenticator
                            .getAcessToken()
                            .ifPresent(
                                    token ->
                                            persistentStorage.put(
                                                    PersistentStorageKeys.ACCESS_TOKEN, token));
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

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        OAuth2Token accessToken =
                persistentStorage
                        .get(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (accessToken.hasAccessExpired()) {
            if (!accessToken.canRefresh()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            persistentStorage.remove(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN);

            // Refresh token is not always present, if it's absent we fall back to the manual
            // authentication.
            String refreshToken =
                    accessToken
                            .getRefreshToken()
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);
            accessToken =
                    authenticator
                            .refreshAccessToken(refreshToken)
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);
            if (!accessToken.isValid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            // Store the new access token on the persistent storage again.
            persistentStorage.put(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, accessToken);
        }
    }
}
