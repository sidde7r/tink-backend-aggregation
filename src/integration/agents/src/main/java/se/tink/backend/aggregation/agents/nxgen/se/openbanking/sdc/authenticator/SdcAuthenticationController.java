package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.authenticator;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.i18n.LocalizableKey;

public class SdcAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final OAuth2Authenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;
    private static final long WAIT_FOR_MINUTES = 9L;

    public SdcAuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper,
            OAuth2Authenticator authenticator,
            StrongAuthenticationState strongAuthenticationState) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        throw SessionError.SESSION_EXPIRED.exception();
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
        Preconditions.checkNotNull(code);

        OAuth2Token accessToken = authenticator.exchangeAuthorizationCode(code);

        authenticator.useAccessToken(accessToken);
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
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
