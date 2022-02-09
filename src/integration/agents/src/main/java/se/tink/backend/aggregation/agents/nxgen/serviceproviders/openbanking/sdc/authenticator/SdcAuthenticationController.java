package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@Slf4j
@RequiredArgsConstructor
public class SdcAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final long WAIT_FOR_MINUTES = 9L;
    private static final int DEFAULT_TOKEN_DAYS_TO_EXPIRE = 90;

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SdcAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;
    private final Credentials credentials;

    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        if (credentials.isSessionExpired()) {
            authenticator.removeTokenFromPersistentStorage();
            throw SessionError.SESSION_EXPIRED.exception();
        }
        authenticator.refreshAccessToken();
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
        authenticator.putTokenInPersistentStorage(accessToken);
        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        accessToken, DEFAULT_TOKEN_DAYS_TO_EXPIRE, ChronoUnit.DAYS));

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
