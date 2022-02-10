package se.tink.backend.aggregation.agents.abnamro;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n_aggregation.Catalog;

public class MobileBankingAuthenticationPayload {
    public static final int MOBILE_BANKING_APP_VERSION_CODE = 10050000; // Mobile Banking 10.5

    public static ThirdPartyAppAuthenticationPayload create(Catalog catalog, String token) {
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        // Generic things about download and upgrade
        payload.setDownloadTitle(catalog.getString("Download Mobile Banking"));
        payload.setDownloadMessage(
                catalog.getString("You need to download the Mobile Banking in order to continue."));

        payload.setUpgradeTitle(catalog.getString("Upgrade Mobile Banking"));
        payload.setUpgradeMessage(
                catalog.getString("You need to upgrade the Mobile Banking in order to continue."));

        // iOS details
        ThirdPartyAppAuthenticationPayload.Ios iosPayload =
                new ThirdPartyAppAuthenticationPayload.Ios();
        iosPayload.setAppScheme("nl.abnamro.constellation.enrollment://");

        // Note 1: This query parameters are case sensitive at ABN AMRO side.
        // Note 2: The redirect urls cannot be empty
        iosPayload.setDeepLinkUrl(
                String.format(
                        "nl.abnamro.constellation.enrollment://grip?id=%s&successurl=grip://open&failurl=grip://open",
                        token));

        iosPayload.setAppStoreUrl("https://itunes.apple.com/nl/app/mobiel-bankieren/id439728011");

        payload.setIos(iosPayload);

        // Android details
        ThirdPartyAppAuthenticationPayload.Android androidPayload =
                new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setPackageName("com.abnamro.nl.mobile.payments");
        androidPayload.setRequiredVersion(MOBILE_BANKING_APP_VERSION_CODE);
        androidPayload.setIntent(
                String.format(
                        "nl.abnamro.constellation.enrollment://grip?id=%s&successurl=grip://open&failurl=grip://open",
                        token));

        payload.setAndroid(androidPayload);

        return payload;
    }
}
