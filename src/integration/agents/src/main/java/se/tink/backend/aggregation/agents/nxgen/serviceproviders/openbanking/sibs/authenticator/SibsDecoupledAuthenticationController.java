package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;

public class SibsDecoupledAuthenticationController
        implements Authenticator, AutoAuthenticator, MultiFactorAuthenticator {
    private static final Logger logger =
            LoggerFactory.getLogger(SibsDecoupledAuthenticationController.class);
    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SibsAuthenticator authenticator;
    private final String state;
    private final Retryer<ConsentStatus> consentStatusRetryer =
            SibsUtils.getConsentStatusRetryer(SLEEP_TIME, RETRY_ATTEMPTS);

    public SibsDecoupledAuthenticationController(
            SibsAuthenticator authenticator,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.authenticator = authenticator;
        this.state = SibsUtils.getRequestId();
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        authenticator.autoAuthenticate();
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException {
        initializeDecoupledConsent(credentials);
        supplementalInformationHelper.openThirdPartyApp(getAppPayload());
        try {
            ConsentStatus consentStatus = consentStatusRetryer.call(authenticator::getConsentStatus);
            authenticator.setSessionExpiryDateIfAccepted(consentStatus);
        } catch (ExecutionException | RetryException e) {
            logger.warn("Authorization failed, consents status is not accepted.", e);
            throw new ThirdPartyAppException(ThirdPartyAppError.TIMED_OUT);
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }

    private void initializeDecoupledConsent(Credentials credentials) throws ThirdPartyAppException {
        try {
            authenticator.initializeDecoupledConsent(
                    state,
                    SibsConstants.HeaderValues.CLIENTE_PARTICULAR,
                    credentials.getField(SibsConstants.Storage.PSU_ID));
        } catch (HttpClientException e) {
            logger.warn("Authorization failed, cannot create consents.", e);
            throw new ThirdPartyAppException(ThirdPartyAppError.TIMED_OUT);
        }
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload() {
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();
        payload.setDownloadMessage(SibsConstants.AppPayload.DOWNLOAD_MESSAGE.get());
        payload.setDownloadTitle(SibsConstants.AppPayload.DOWNLOAD_TITLE.get());

        ThirdPartyAppAuthenticationPayload.Android androidPayload =
                new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setIntent(SibsConstants.AppPayload.CAIXADIRECTA_DEEPLINK);
        androidPayload.setPackageName(SibsConstants.AppPayload.ANDROID_PACKAGE_NAME);
        payload.setAndroid(androidPayload);

        URL iosRedirectUrl = new URL(SibsConstants.AppPayload.CAIXADIRECTA_DEEPLINK);
        ThirdPartyAppAuthenticationPayload.Ios iOsPayload =
                new ThirdPartyAppAuthenticationPayload.Ios();
        iOsPayload.setAppScheme(iosRedirectUrl.getScheme());
        iOsPayload.setDeepLinkUrl(iosRedirectUrl.get());
        iOsPayload.setAppStoreUrl(SibsConstants.AppPayload.APP_STORE_URL);
        payload.setIos(iOsPayload);
        return payload;
    }
}
