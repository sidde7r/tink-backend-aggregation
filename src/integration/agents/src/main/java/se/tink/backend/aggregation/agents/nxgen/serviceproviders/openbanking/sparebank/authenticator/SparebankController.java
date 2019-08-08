package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
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

public class SparebankController implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {
    private static final Random random = new SecureRandom();
    private static final Encoder encoder = Base64.getUrlEncoder();
    private static final long WAIT_FOR_MINUTES = 9L;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SparebankAuthenticator authenticator;
    private final String state;
    private String errorMessage;

    public SparebankController(
            final SupplementalInformationHelper supplementalInformationHelper,
            final SparebankAuthenticator authenticator) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.state = generateRandomState();
    }

    @Override
    public void autoAuthenticate(Credentials credentials)
            throws SessionException, BankServiceException {
        throw SessionError.SESSION_EXPIRED.exception();
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {

        Map<String, String> supplementalInformation =
                this.supplementalInformationHelper
                        .waitForSupplementalInformation(
                                this.formatSupplementalKey(state),
                                WAIT_FOR_MINUTES,
                                TimeUnit.MINUTES)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No supplemental info found in api response"));

        Optional<String> psuId =
                Optional.ofNullable(supplementalInformation.getOrDefault("psu-id", null));

        Optional<String> tppSessionId =
                Optional.ofNullable(supplementalInformation.getOrDefault("tpp-session-id", null));

        if (psuId.isPresent() && tppSessionId.isPresent()) {
            authenticator.setUpPsuAndSession(psuId.get(), tppSessionId.get());
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
        } else {
            errorMessage = supplementalInformation.getOrDefault("message", null);
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.CANCELLED);
        }
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        final ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();
        final Android androidPayload = new Android();
        payload.setAndroid(androidPayload);

        final URL authorizeUrl = this.authenticator.buildAuthorizeUrl(state);
        androidPayload.setIntent(authorizeUrl.get());

        final Ios iOsPayload = new Ios();
        iOsPayload.setAppScheme(authorizeUrl.getScheme());
        iOsPayload.setDeepLinkUrl(authorizeUrl.get());
        payload.setIos(iOsPayload);

        return payload;
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.ofNullable(errorMessage).map(LocalizableKey::of);
    }

    private String formatSupplementalKey(final String key) {
        return String.format("tpcb_%s", key);
    }

    private static String generateRandomState() {
        final byte[] randomData = new byte[32];
        random.nextBytes(randomData);
        return encoder.encodeToString(randomData);
    }
}
