package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.TimeValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.AuthenticationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SwedbankAuthenticationController
        implements AutoAuthenticator, BankIdAuthenticator<AuthenticationResponse> {
    private static final int DEFAULT_TOKEN_LIFETIME = 90;
    private static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;

    private final PersistentStorage persistentStorage;
    private final SupplementalRequester supplementalRequester;
    private final SwedbankAuthenticator authenticator;
    private final Credentials credentials;
    private final int tokenLifetime;
    private final TemporalUnit tokenLifetimeUnit;
    private String ssn;
    private Optional<String> autoStartToken;

    public SwedbankAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalRequester supplementalRequester,
            SwedbankAuthenticator authenticator,
            Credentials credentials) {
        this(
                persistentStorage,
                supplementalRequester,
                authenticator,
                credentials,
                DEFAULT_TOKEN_LIFETIME,
                DEFAULT_TOKEN_LIFETIME_UNIT);
    }

    public SwedbankAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalRequester supplementalRequester,
            SwedbankAuthenticator authenticator,
            Credentials credentials,
            int tokenLifetime,
            TemporalUnit tokenLifetimeUnit) {
        this.persistentStorage = persistentStorage;
        this.supplementalRequester = supplementalRequester;
        this.authenticator = authenticator;
        this.credentials = credentials;
        this.tokenLifetime = tokenLifetime;
        this.tokenLifetimeUnit = tokenLifetimeUnit;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        OAuth2Token accessToken =
                getAccessToken().orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (accessToken.hasAccessExpired()) {
            if (!accessToken.canRefresh()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            persistentStorage.remove(PersistentStorageKeys.OAUTH_2_TOKEN);

            // Refresh token is not always present, if it's absent we fall back to the manual
            // authentication.
            String refreshToken =
                    accessToken
                            .getRefreshToken()
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);

            accessToken = authenticator.refreshAccessToken(refreshToken);
            if (!accessToken.isValid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            // Store the new access token
            authenticator.useAccessToken(accessToken);
        }
    }

    @Override
    public AuthenticationResponse init(String ssn) {
        this.ssn = ssn;
        try {
            AuthenticationResponse authenticationResponse = authenticator.init(ssn);
            this.autoStartToken =
                    Optional.ofNullable(authenticationResponse.getChallengeData())
                            .map(ChallengeDataEntity::getAutoStartToken);
            return authenticationResponse;
        } catch (HttpResponseException e) {
            if (getBankIdStatusBasedOnError(e) == BankIdStatus.INTERRUPTED) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            }
            throw e;
        }
    }

    @Override
    public BankIdStatus collect(AuthenticationResponse reference) throws AuthenticationException {
        final String collectAuthUri = reference.getCollectAuthUri();
        final AuthenticationStatusResponse authenticationStatusResponse;

        try {
            authenticationStatusResponse = authenticator.collect(ssn, collectAuthUri);
        } catch (HttpResponseException e) {
            return getBankIdStatusBasedOnError(e);
        }

        if (authenticationStatusResponse.loginCanceled()) {
            return BankIdStatus.CANCELLED;
        }

        switch (authenticationStatusResponse.getScaStatus().toLowerCase()) {
            case AuthStatus.RECEIVED:
            case AuthStatus.STARTED:
                return BankIdStatus.WAITING;
            case AuthStatus.FINALIZED:
                return finalizeBankid(authenticationStatusResponse);
            case AuthStatus.FAILED:
                return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    private BankIdStatus getBankIdStatusBasedOnError(HttpResponseException e) {
        GenericResponse errorResponse = e.getResponse().getBody(GenericResponse.class);

        if (errorResponse.isLoginInterrupted()) {
            return BankIdStatus.INTERRUPTED;
        }

        if (errorResponse.isKycError()) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                    EndUserMessage.MUST_UPDATE_AGREEMENT.getKey());
        }

        if (errorResponse.isMissingBankAgreement()) {
            throw LoginError.NOT_CUSTOMER.exception();
        }

        throw e;
    }

    private BankIdStatus finalizeBankid(AuthenticationStatusResponse authenticationStatusResponse) {
        final String code = authenticationStatusResponse.getAuthorizationCode();
        final OAuth2Token accessToken = authenticator.exchangeAuthorizationCode(code);

        if (!accessToken.isValid()) {
            throw new IllegalStateException("Invalid access token.");
        }

        if (!accessToken.isBearer()) {
            throw new IllegalStateException(
                    String.format("Unknown token type '%s'.", accessToken.getTokenType()));
        }

        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        accessToken, tokenLifetime, tokenLifetimeUnit));

        // Tell the authenticator which access token it can use.
        authenticator.useAccessToken(accessToken);

        handleSwedbankFlow();

        return BankIdStatus.DONE;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return autoStartToken;
    }

    @Override
    public AuthenticationResponse refreshAutostartToken()
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        Uninterruptibles.sleepUninterruptibly(
                TimeValues.SLEEP_TIME_MILLISECONDS, TimeUnit.MILLISECONDS);
        return init(ssn);
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class);
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) throws SessionException {
        return Optional.ofNullable(authenticator.refreshAccessToken(refreshToken));
    }

    private void handleSwedbankFlow() {
        ConsentResponse initConsent = authenticator.getConsentForAllAccounts();
        authenticator.useConsent(initConsent);

        authenticator.getConsentForIbanList().ifPresent(this::handleAccountDetailsConsent);
    }

    private void handleAccountDetailsConsent(ConsentResponse detailsConsentResponse) {
        authenticator.useConsent(detailsConsentResponse);

        if (detailsConsentResponse.getConsentStatus().equalsIgnoreCase(ConsentStatus.VALID)) {
            return;
        }
        AuthenticationResponse response =
                authenticator.initiateAuthorization(
                        detailsConsentResponse.getLinks().getHrefEntity().getHref());
        supplementalRequester.openBankId(response.getChallengeData().getAutoStartToken(), true);

        while (authenticator
                .getScaStatus(response.getCollectAuthUri())
                .equalsIgnoreCase(AuthStatus.STARTED)) {
            Uninterruptibles.sleepUninterruptibly(
                    SwedbankConstants.TimeValues.SLEEP_TIME_MILLISECONDS, TimeUnit.MILLISECONDS);
        }
    }
}
