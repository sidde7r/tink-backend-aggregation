package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Urls;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class DeutscheBankRedirectHandler {
    private final SupplementalInformationHelper supplementalInformationHelper;

    /** @param supplementalInformationHelper */
    public DeutscheBankRedirectHandler(
            SupplementalInformationHelper supplementalInformationHelper) {
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    private ThirdPartyAppAuthenticationPayload getRedirectPayload() {
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        ThirdPartyAppAuthenticationPayload.Android androidPayload =
                new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setIntent(Urls.MYBANK_BELGIUM_ANDROID);
        payload.setAndroid(androidPayload);

        ThirdPartyAppAuthenticationPayload.Ios iOsPayload =
                new ThirdPartyAppAuthenticationPayload.Ios();
        iOsPayload.setAppScheme((new URL(Urls.MYBANK_BELGIUM_IOS)).getScheme());
        iOsPayload.setDeepLinkUrl(Urls.MYBANK_BELGIUM_IOS);
        payload.setIos(iOsPayload);

        ThirdPartyAppAuthenticationPayload.Desktop desktop =
                new ThirdPartyAppAuthenticationPayload.Desktop();
        desktop.setUrl(Urls.MYBANK_BELGIUM);
        payload.setDesktop(desktop);

        return payload;
    }

    /** Handle a Deutsche Bank redirect by opening the URL in the client. */
    public void handleRedirect() {
        final ThirdPartyAppAuthenticationPayload payload = getRedirectPayload();
        supplementalInformationHelper.openThirdPartyApp(payload);
    }
}
