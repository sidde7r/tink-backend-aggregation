package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Data
public class ThirdPartyAppAuthenticationPayload {
    private Android android;
    private Ios ios;
    private Desktop desktop;
    private String downloadTitle;
    private String downloadMessage;
    private String upgradeTitle;
    private String upgradeMessage;
    private String state;

    public static ThirdPartyAppAuthenticationPayload of(URL url) {
        Preconditions.checkNotNull(url, "URL must not be null.");
        final ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        final Android androidPayload = new Android();
        androidPayload.setIntent(url.get());

        final Ios iOsPayload = new Ios();
        iOsPayload.setAppScheme(url.getScheme());
        iOsPayload.setDeepLinkUrl(url.get());

        final Desktop desktop = new Desktop();
        desktop.setUrl(url.get());

        payload.setAndroid(androidPayload);
        payload.setIos(iOsPayload);
        payload.setDesktop(desktop);
        return payload;
    }

    @Data
    public static class Ios {

        /**
         * Url to AppStore where the app can be downloaded. Example:
         * https://itunes.apple.com/nl/app/abn-amro-mobiel-bankieren/id439728011?mt=8
         */
        private String appStoreUrl;

        /** Base scheme of the app. Example: abnamro.nl:// */
        @Setter(AccessLevel.NONE)
        private String scheme;

        /**
         * Url that the app should open. Can be of another scheme then app scheme. Example:
         * nl.abnamro.signing.grip://signing?itemId=10101010101
         */
        private String deepLinkUrl;

        public void setAppScheme(String scheme) {
            this.scheme = scheme;
        }
    }

    @Data
    public static class Android {

        /** Name of the package that should be opened. Example: "com.abnamro.nl.mobile.payments" */
        private String packageName;

        /**
         * The minimum version of the package that needs to be installed. Example: 9.03.01.01 =
         * 9030101
         */
        @Setter(AccessLevel.NONE)
        private int requiredMinimumVersion;

        /**
         * Url of the intent that should be opened. Example:
         * nl.abnamro.signing.grip://signing?itemId=10101010101
         */
        private String intent;

        public void setRequiredVersion(int requiredMinimumVersion) {
            this.requiredMinimumVersion = requiredMinimumVersion;
        }
    }

    @Data
    public static class Desktop {
        private String url;
    }
}
