package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator;

import com.google.common.base.Preconditions;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.rpc.ConsentResponse;
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

public class VolksbankAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {
    private static final Random random = new SecureRandom();
    private static final Encoder encoder = Base64.getUrlEncoder();
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final VolksbankAuthenticator authenticator;
    private final String state;
    private static final long WAIT_FOR_MINUTES = 9L;

    public VolksbankAuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper,
            VolksbankAuthenticator authenticator) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.state = generateRandomState();
    }

    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        throw SessionError.SESSION_EXPIRED.exception();
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {

        this.supplementalInformationHelper.waitForSupplementalInformation(
                this.formatSupplementalKey(this.state), WAIT_FOR_MINUTES, TimeUnit.MINUTES);

        if (!authenticator.getConsentStatus().isValid()) {
            throw new IllegalStateException("Invalid consent!");
        }

        ConsentResponse detailedConsent = authenticator.getDetailedConsent(state);

        return openThirdPartyApp(new URL(detailedConsent.getLinks().getScaRedirect()));
    }

    private ThirdPartyAppResponse<String> collectDetailedConsent(String reference) {

        this.supplementalInformationHelper.waitForSupplementalInformation(
                this.formatSupplementalKey(this.state), WAIT_FOR_MINUTES, TimeUnit.MINUTES);

        if (!authenticator.getConsentStatus().isValid()) {
            throw new IllegalStateException("Invalid consent!");
        }

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    private ThirdPartyAppResponse<String> openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = this.getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
        ThirdPartyAppResponse<String> response =
                ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
        return this.collectDetailedConsent(response.getReference());
    }

    public ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
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

    private static String generateRandomState() {
        byte[] randomData = new byte[32];
        random.nextBytes(randomData);
        return encoder.encodeToString(randomData);
    }

    private String formatSupplementalKey(String key) {
        return String.format("tpcb_%s", key);
    }
}
