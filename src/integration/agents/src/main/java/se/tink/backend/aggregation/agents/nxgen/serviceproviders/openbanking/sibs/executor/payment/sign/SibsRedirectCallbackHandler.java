package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.utils.OAuthUtils;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SibsRedirectCallbackHandler {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final long waitFor;
    private final TimeUnit unit;

    /**
     * @param supplementalInformationHelper
     * @param waitFor total time to wait for a callback
     * @param unit total time to wait for a callback
     */
    public SibsRedirectCallbackHandler(
            SupplementalInformationHelper supplementalInformationHelper,
            long waitFor,
            TimeUnit unit) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.waitFor = waitFor;
        this.unit = unit;
    }

    private ThirdPartyAppAuthenticationPayload getRedirectPayload(URL url) {
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        ThirdPartyAppAuthenticationPayload.Android androidPayload =
                new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setIntent(url.get());
        payload.setAndroid(androidPayload);

        ThirdPartyAppAuthenticationPayload.Ios iOsPayload =
                new ThirdPartyAppAuthenticationPayload.Ios();
        iOsPayload.setAppScheme(url.getScheme());
        iOsPayload.setDeepLinkUrl(url.get());
        payload.setIos(iOsPayload);

        return payload;
    }

    /**
     * Handle a SCA redirect by opening the URL in the client, and waiting for a callback.
     *
     * <p>If the SCA flow supports different callbacks for success and failure, you can add
     * different parameters to them, and check for them in the result.
     *
     * @param redirectUrl URL to open on the client
     * @param scaState State token used on the SCA callback URL.
     * @return Parameters passed to the SCA callback (including the state parameter), or empty if it
     *     timed out.
     */
    public Optional<Map<String, String>> handleRedirect(URL redirectUrl, String scaState) {
        final ThirdPartyAppAuthenticationPayload payload = getRedirectPayload(redirectUrl);
        supplementalInformationHelper.openThirdPartyApp(payload);

        final String supplementalInformationKey = OAuthUtils.formatSupplementalKey(scaState);
        return supplementalInformationHelper.waitForSupplementalInformation(
                supplementalInformationKey, waitFor, unit);
    }
}
