package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.utils;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.URL;

public class BunqRedirectHandler {
    final SupplementalInformationHelper supplementalInformationHelper;

    /** @param supplementalInformationHelper */
    public BunqRedirectHandler(SupplementalInformationHelper supplementalInformationHelper) {
        this.supplementalInformationHelper = supplementalInformationHelper;
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
     * Handle a Bunq redirect by opening the URL in the client.
     *
     * @param redirectUrl URL to open on the client
     */
    public void handleRedirect(URL redirectUrl) {
        final ThirdPartyAppAuthenticationPayload payload = getRedirectPayload(redirectUrl);
        supplementalInformationHelper.openThirdPartyApp(payload);
    }
}
