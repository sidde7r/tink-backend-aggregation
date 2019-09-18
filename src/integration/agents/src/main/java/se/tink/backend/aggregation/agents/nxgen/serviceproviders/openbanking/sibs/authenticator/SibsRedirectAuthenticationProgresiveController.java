package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStrongAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.i18n.LocalizableKey;

public class SibsRedirectAuthenticationProgresiveController
        implements AutoAuthenticator, ThirdPartyAppStrongAuthenticator<String> {
    private static final Logger logger =
            LoggerFactory.getLogger(SibsRedirectAuthenticationController.class);
    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 10;
    private final SibsAuthenticator authenticator;
    private final String strongAuthenticationState;
    private final String strongAuthenticationStateSupplementalKey;
    private final Retryer<ConsentStatus> consentStatusRetryer =
            SibsUtils.getConsentStatusRetryer(SLEEP_TIME, RETRY_ATTEMPTS);

    public SibsRedirectAuthenticationProgresiveController(
            SibsAuthenticator authenticator, StrongAuthenticationState strongAuthenticationState) {
        this.authenticator = authenticator;

        this.strongAuthenticationStateSupplementalKey =
                strongAuthenticationState.getSupplementalKey();
        this.strongAuthenticationState = strongAuthenticationState.getState();
    }

    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(Map<String, String> callbackData)
            throws AuthenticationException, AuthorizationException {

        ThirdPartyAppStatus status;
        try {
            ConsentStatus consentStatus =
                    consentStatusRetryer.call(authenticator::getConsentStatus);
            status = consentStatus.getThirdPartyAppStatus();

            authenticator.setSessionExpiryDateIfAccepted(consentStatus);
        } catch (ExecutionException | RetryException e) {
            logger.warn("Authorization failed, consents status is not accepted.", e);
            status = ThirdPartyAppStatus.TIMED_OUT;
        }

        return ThirdPartyAppResponseImpl.create(status);
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        authenticator.autoAuthenticate();
    }

    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        URL authorizeUrl = this.authenticator.buildAuthorizeUrl(this.strongAuthenticationState);
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    @Override
    public String getStrongAuthenticationStateSupplementalKey() {
        return strongAuthenticationStateSupplementalKey;
    }

    @Override
    public long getWaitForMinutes() {
        return SLEEP_TIME;
    }

    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
