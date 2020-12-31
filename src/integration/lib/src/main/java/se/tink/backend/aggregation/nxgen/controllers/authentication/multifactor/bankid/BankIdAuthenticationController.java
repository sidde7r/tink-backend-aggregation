package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.invoke.MethodHandles;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BankIdAuthenticationController<T> implements AutoAuthenticator, TypedAuthenticator {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int MAX_ATTEMPTS = 90;
    private static final int DEFAULT_TOKEN_LIFETIME = 90;
    private static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;

    private final BankIdAuthenticator<T> authenticator;
    private final SupplementalRequester supplementalRequester;
    private final boolean waitOnBankId;
    private final int tokenLifetime;
    private final TemporalUnit tokenLifetimeUnit;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;

    public BankIdAuthenticationController(
            SupplementalRequester supplementalRequester,
            BankIdAuthenticator<T> authenticator,
            PersistentStorage persistentStorage,
            Credentials credentials) {
        this(supplementalRequester, authenticator, false, persistentStorage, credentials);
    }

    public BankIdAuthenticationController(
            SupplementalRequester supplementalRequester,
            BankIdAuthenticator<T> authenticator,
            PersistentStorage persistentStorage,
            Credentials credentials,
            int tokenLifetime,
            TemporalUnit tokenLifetimeUnit) {
        this(
                supplementalRequester,
                authenticator,
                false,
                persistentStorage,
                credentials,
                tokenLifetime,
                tokenLifetimeUnit);
    }

    public BankIdAuthenticationController(
            SupplementalRequester supplementalRequester,
            BankIdAuthenticator<T> authenticator,
            boolean waitOnBankId,
            PersistentStorage persistentStorage,
            Credentials credentials) {
        this(
                supplementalRequester,
                authenticator,
                waitOnBankId,
                persistentStorage,
                credentials,
                DEFAULT_TOKEN_LIFETIME,
                DEFAULT_TOKEN_LIFETIME_UNIT);
    }

    public BankIdAuthenticationController(
            SupplementalRequester supplementalRequester,
            BankIdAuthenticator<T> authenticator,
            boolean waitOnBankId,
            PersistentStorage persistentStorage,
            Credentials credentials,
            int tokenLifetime,
            TemporalUnit tokenLifetimeUnit) {
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.supplementalRequester = Preconditions.checkNotNull(supplementalRequester);
        this.waitOnBankId = waitOnBankId;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.tokenLifetime = tokenLifetime;
        this.tokenLifetimeUnit = tokenLifetimeUnit;
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

        // Empty SSN is valid for autostart token agents, handling for ssn not empty needs to be
        // done in each agent
        T reference = authenticator.init(ssn);

        openBankId();

        poll(reference);
        authenticator.getAccessToken().ifPresent(token -> storeAccessToken(token, credentials));
    }

    private void openBankId() {
        supplementalRequester.openBankId(
                authenticator.getAutostartToken().orElse(null), waitOnBankId);
    }

    // throws exception unless the BankIdStatus was DONE
    private void poll(T reference) throws AuthenticationException, AuthorizationException {
        BankIdStatus status = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            status = authenticator.collect(reference);

            switch (status) {
                case DONE:
                    // BankID successful, proceed authentication
                    return;
                case WAITING:
                    logger.info("Waiting for BankID");
                    break;
                case CANCELLED:
                    throw BankIdError.CANCELLED.exception();
                case NO_CLIENT:
                    throw BankIdError.NO_CLIENT.exception();
                case TIMEOUT:
                    throw BankIdError.TIMEOUT.exception();
                case EXPIRED_AUTOSTART_TOKEN:
                    reference = authenticator.refreshAutostartToken();
                    openBankId();
                    break;
                case INTERRUPTED:
                    throw BankIdError.INTERRUPTED.exception();
                default:
                    logger.warn(String.format("Unknown BankIdStatus (%s)", status));
                    throw BankIdError.UNKNOWN.exception();
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        logger.info(String.format("BankID timed out internally, last status: %s", status));
        throw BankIdError.TIMEOUT.exception();
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        OAuth2Token oAuth2Token =
                persistentStorage
                        .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (oAuth2Token.hasAccessExpired()) {
            if (!oAuth2Token.canRefresh()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            // Refresh token is not always present, if it's absent we fall back to the manual
            // authentication.
            String refreshToken =
                    oAuth2Token
                            .getRefreshToken()
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);
            OAuth2Token refreshedOAuth2Token =
                    authenticator
                            .refreshAccessToken(refreshToken)
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);

            if (!refreshedOAuth2Token.isValid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            if (refreshedOAuth2Token.hasRefreshExpire()) {
                credentials.setSessionExpiryDate(
                        OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                                refreshedOAuth2Token, tokenLifetime, tokenLifetimeUnit));
            }

            oAuth2Token = refreshedOAuth2Token.updateTokenWithOldToken(oAuth2Token);

            // Store the new access token on the persistent storage again.
            persistentStorage.rotateStorageValue(
                    OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
        }
    }

    private void storeAccessToken(OAuth2Token oAuth2Token, Credentials credentials) {
        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        oAuth2Token, tokenLifetime, tokenLifetimeUnit));
    }
}
