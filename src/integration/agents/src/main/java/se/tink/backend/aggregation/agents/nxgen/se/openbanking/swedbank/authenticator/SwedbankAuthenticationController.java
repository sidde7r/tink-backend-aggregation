package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.SwedbankTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants.ErrorType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final Logger logger =
            LoggerFactory.getLogger(OAuth2AuthenticationController.class);
    private static final int DEFAULT_TOKEN_LIFETIME = 90;
    private static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;

    private final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SwedbankAuthenticator authenticator;
    private final Credentials credentials;
    private final int tokenLifetime;
    private final TemporalUnit tokenLifetimeUnit;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SwedbankTransactionFetcher swedbankTransactionFetcher;
    private final SwedbankConfiguration swedbankConfiguration;

    // This wait time is for the whole user authentication. Different banks have different
    // cumbersome authentication flows.
    private static final long WAIT_FOR_MINUTES = 9;

    public SwedbankAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            SwedbankAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            SwedbankTransactionFetcher swedbankTransactionFetcher,
            SwedbankConfiguration swedbankConfiguration) {
        this(
                persistentStorage,
                supplementalInformationHelper,
                authenticator,
                credentials,
                strongAuthenticationState,
                DEFAULT_TOKEN_LIFETIME,
                DEFAULT_TOKEN_LIFETIME_UNIT,
                swedbankTransactionFetcher,
                swedbankConfiguration);
    }

    public SwedbankAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            SwedbankAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            int tokenLifetime,
            TemporalUnit tokenLifetimeUnit,
            SwedbankTransactionFetcher swedbankTransactionFetcher,
            SwedbankConfiguration swedbankConfiguration) {
        this.persistentStorage = persistentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.credentials = credentials;
        this.tokenLifetime = tokenLifetime;
        this.tokenLifetimeUnit = tokenLifetimeUnit;
        this.strongAuthenticationState = strongAuthenticationState;
        this.swedbankTransactionFetcher = swedbankTransactionFetcher;
        this.swedbankConfiguration = swedbankConfiguration;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
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

            accessToken = authenticator.refreshAccessToken(refreshToken);
            if (!accessToken.isValid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            // Store the new access token on the persistent storage again.
            persistentStorage.put(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, accessToken);

            // Fall through.
        }

        // Tell the authenticator which access token it can use.
        authenticator.useAccessToken(accessToken);
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        return ThirdPartyAppAuthenticationPayload.of(
                authenticator.buildAuthorizeUrl(strongAuthenticationState.getState()));
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) throws AuthenticationException {

        Map<String, String> callbackData =
                supplementalInformationHelper
                        .waitForSupplementalInformation(
                                strongAuthenticationState.getSupplementalKey(),
                                WAIT_FOR_MINUTES,
                                TimeUnit.MINUTES)
                        .orElseThrow(
                                LoginError.INCORRECT_CREDENTIALS
                                        ::exception); // todo: change this exception

        handleErrors(callbackData);

        String code = callbackData.getOrDefault(OAuth2Constants.CallbackParams.CODE, null);
        if (Strings.isNullOrEmpty(code)) {
            throw new IllegalStateException("callbackData did not contain 'code'");
        }

        OAuth2Token accessToken = authenticator.exchangeAuthorizationCode(code);

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

        persistentStorage.put(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, accessToken);

        // Tell the authenticator which access token it can use.
        authenticator.useAccessToken(accessToken);

        handleSwedbankFlow();

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    private void handleSwedbankFlow() {
        ConsentResponse initConsent = authenticator.getConsentForAllAccounts();
        authenticator.useConsent(initConsent);

        List<String> ibanList = authenticator.getAccountList();

        ConsentResponse consentResponseIbanList = authenticator.getConsentForIbanList();
        authenticator.useConsent(consentResponseIbanList);

        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(
                        new URL(consentResponseIbanList.getLinks().getHrefEntity().getHref())));

        while (!authenticator.getConsentStatus(consentResponseIbanList.getConsentId())) {
            Uninterruptibles.sleepUninterruptibly(
                    SwedbankConstants.TimeValues.SLEEP_TIME_MILLISECONDS, TimeUnit.MILLISECONDS);
        }

        for (String iban : ibanList) {
            swedbankTransactionFetcher.startScaAuthorization(
                    iban,
                    Timestamp.valueOf(
                            LocalDateTime.now()
                                    .minusMonths(swedbankConfiguration.getMonthsToFetch())),
                    Timestamp.valueOf(LocalDateTime.now()));
        }
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private Optional<String> getCallbackElement(Map<String, String> callbackData, String key) {
        String value = callbackData.getOrDefault(key, null);

        return Strings.isNullOrEmpty(value) ? Optional.empty() : Optional.of(value);
    }

    private void handleErrors(Map<String, String> callbackData) throws AuthenticationException {
        Optional<String> error =
                getCallbackElement(callbackData, OAuth2Constants.CallbackParams.ERROR);
        Optional<String> errorDescription =
                getCallbackElement(callbackData, OAuth2Constants.CallbackParams.ERROR_DESCRIPTION);

        if (!error.isPresent()) {
            logger.info("OAuth2 callback success.");
            return;
        }

        ErrorType errorType = ErrorType.getErrorType(error.get());
        if (OAuth2Constants.ErrorType.ACCESS_DENIED.equals(errorType)
                || ErrorType.LOGIN_REQUIRED.equals(errorType)) {
            logger.info(
                    "OAuth2 {} callback: {}",
                    errorType.getValue(),
                    SerializationUtils.serializeToString(callbackData));
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        throw new IllegalStateException(
                String.format("Unknown error: %s:%s.", errorType, errorDescription.orElse("")));
    }
}
