package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Android;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Ios;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.i18n.LocalizableKey;

public class SibsRedirectAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final Logger logger =
            LoggerFactory.getLogger(SibsRedirectAuthenticationController.class);
    private static final long WAIT_FOR_MINUTES = 9L;
    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 10;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SibsAuthenticator authenticator;
    private final String state;
    private final Retryer<ConsentStatus> consentStatusRetryer =
            SibsUtils.getConsentStatusRetryer(SLEEP_TIME, RETRY_ATTEMPTS);

    public SibsRedirectAuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper,
            SibsAuthenticator authenticator) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.state = SibsUtils.getRequestId();
    }

    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        authenticator.autoAuthenticate();
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) throws AuthenticationException {
        initializeRedirectConsent();

        ThirdPartyAppStatus status;
        try {
            ConsentStatus consentStatus =
                    consentStatusRetryer.call(authenticator::getConsentStatus);
            status = consentStatus.getThirdPartyAppStatus();
        } catch (ExecutionException | RetryException | IllegalStateException e) {
            logger.warn("Authorization failed, consents status is not accepted.", e);
            status = ThirdPartyAppStatus.TIMED_OUT;
        }

        return ThirdPartyAppResponseImpl.create(status);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        URL authorizeUrl = this.authenticator.buildAuthorizeUrl(this.state);
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();
        Android androidPayload = new Android();
        androidPayload.setIntent(authorizeUrl.get());
        payload.setAndroid(androidPayload);
        Ios iOsPayload = new Ios();
        iOsPayload.setAppScheme(authorizeUrl.getScheme());
        iOsPayload.setDeepLinkUrl(authorizeUrl.get());
        payload.setIos(iOsPayload);
        return payload;
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private void initializeRedirectConsent() throws ThirdPartyAppException {
        supplementalInformationHelper
                .waitForSupplementalInformation(
                        formatSupplementalKey(state), WAIT_FOR_MINUTES, TimeUnit.MINUTES)
                .orElseThrow(() -> new ThirdPartyAppException(ThirdPartyAppError.TIMED_OUT));
    }

    private String formatSupplementalKey(String key) {
        return String.format("tpcb_%s", key);
    }
}
