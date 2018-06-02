package se.tink.backend.common.payloads;

import java.util.Locale;
import joptsimple.internal.Strings;
import se.tink.libraries.i18n.Catalog;

public class MobileBankIdAuthenticationPayload {

    public static ThirdPartyAppAuthenticationPayload create(String autoStartToken, String credentialsId,
            Locale locale) {
        Catalog catalog = Catalog.getCatalog(locale);

        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        // Generic things about download and upgrade
        payload.setDownloadTitle(catalog.getString("Download Mobile BankID"));
        payload.setDownloadMessage(catalog.getString("You need to install the Mobile BankID app to authenticate"));
        payload.setUpgradeTitle(payload.getDownloadTitle());
        payload.setUpgradeMessage(payload.getDownloadMessage());

        // iOS details
        ThirdPartyAppAuthenticationPayload.Ios ios = new ThirdPartyAppAuthenticationPayload.Ios();
        ios.setAppScheme("bankid://");
        ios.setAppStoreUrl("itms://itunes.apple.com/se/app/bankid-sakerhetsapp/id433151512");

        if (Strings.isNullOrEmpty(autoStartToken)) {
            ios.setDeepLinkUrl(String.format("bankid:///?redirect=tink://bankid/credentials/%s", credentialsId));
        } else {
            ios.setDeepLinkUrl(String.format("bankid:///?autostartToken=%s&redirect=tink://bankid/credentials/%s",
                    autoStartToken, credentialsId));
        }

        payload.setIos(ios);

        // Android details
        ThirdPartyAppAuthenticationPayload.Android android = new ThirdPartyAppAuthenticationPayload.Android();
        android.setPackageName("com.bankid.bus");
        android.setRequiredVersion(0);

        if (Strings.isNullOrEmpty(autoStartToken)) {
            android.setIntent("bankid:///?redirect=null");
        } else {
            android.setIntent(String.format("bankid:///?autostarttoken=%s&redirect=null", autoStartToken));
        }

        payload.setAndroid(android);

        return payload;
    }
}
