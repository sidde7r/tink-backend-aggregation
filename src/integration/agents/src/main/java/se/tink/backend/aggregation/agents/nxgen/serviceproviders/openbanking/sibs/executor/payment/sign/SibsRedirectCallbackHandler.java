package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.utils.OAuthUtils;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SibsRedirectCallbackHandler {
    private final SupplementalRequester supplementalRequester;
    private final long waitFor;
    private final TimeUnit unit;

    /**
     * @param supplementalRequester
     * @param waitFor total time to wait for a callback
     * @param unit total time to wait for a callback
     */
    public SibsRedirectCallbackHandler(
            SupplementalRequester supplementalRequester,
            long waitFor,
            TimeUnit unit) {
        this.supplementalRequester = supplementalRequester;
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

        ThirdPartyAppAuthenticationPayload.Desktop desktop =
                new ThirdPartyAppAuthenticationPayload.Desktop();
        desktop.setUrl(url.get());
        payload.setDesktop(desktop);

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
        AuthenticationResponse.openThirdPartyApp(payload);
        final String supplementalInformationKey = OAuthUtils.formatSupplementalKey(scaState);
        SupplementalWaitRequest waitRequest =
            new SupplementalWaitRequest(
                supplementalInformationKey,
                waitFor,
                unit);
        AuthenticationResponse.requestWaitingForSupplementalInformation(waitRequest);
        return supplementalRequester
            .waitForSupplementalInformation(supplementalInformationKey, waitFor, unit)
            .map(SibsRedirectCallbackHandler::stringToMap);
    }

    private static Map<String, String> stringToMap(final String string) {
        return SerializationUtils.deserializeFromString(
            string, new TypeReference<HashMap<String, String>>() {});
    }
}
