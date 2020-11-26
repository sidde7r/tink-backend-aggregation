package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableKey;

@Slf4j
public class SdcAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final OAuth2Authenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;
    private static final long WAIT_FOR_MINUTES = 9L;
    private PersistentStorage persistentStorage;

    public SdcAuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper,
            OAuth2Authenticator authenticator,
            StrongAuthenticationState strongAuthenticationState,
            PersistentStorage persistentStorage) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.strongAuthenticationState = strongAuthenticationState;
        this.persistentStorage = persistentStorage;
    }

    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        OAuth2Token token =
                persistentStorage
                        .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        OAuth2Token accessToken =
                authenticator.refreshAccessToken(
                        token.getRefreshToken()
                                .orElseThrow(SessionError.SESSION_EXPIRED::exception));

        authenticator.useAccessToken(accessToken);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) throws AuthenticationException {

        Optional<Map<String, String>> stringStringMap =
                supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        Map<String, String> callbackData =
                stringStringMap.orElseThrow(LoginError.INCORRECT_CREDENTIALS::exception);

        String code = callbackData.getOrDefault("code", null);
        checkForErrorsInCallback(callbackData);
        Preconditions.checkNotNull(code);

        OAuth2Token accessToken = authenticator.exchangeAuthorizationCode(code);

        // SDC provides Oauth2 Token without information about token type
        OAuth2TokenEnricher.enrich(accessToken);

        authenticator.useAccessToken(accessToken);
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    private void checkForErrorsInCallback(Map<String, String> callbackData) {
        String errorType = callbackData.get("error");
        if (!Strings.isNullOrEmpty(errorType)) {
            String errorDescription = callbackData.get("error_description");
            throw LoginError.DEFAULT_MESSAGE.exception(
                    errorType + " received in callback data with description: " + errorDescription);
        }
    }

    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        URL authorizeUrl = authenticator.buildAuthorizeUrl(strongAuthenticationState.getState());
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
