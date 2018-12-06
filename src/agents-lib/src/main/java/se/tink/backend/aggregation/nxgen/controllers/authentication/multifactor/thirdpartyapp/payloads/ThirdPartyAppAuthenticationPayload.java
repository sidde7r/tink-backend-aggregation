package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads;

public class ThirdPartyAppAuthenticationPayload {
    private Android android;
    private Ios ios;
    private String downloadTitle;
    private String downloadMessage;
    private String upgradeTitle;
    private String upgradeMessage;

    public String getDownloadTitle() {
        return downloadTitle;
    }

    public void setDownloadTitle(String downloadTitle) {
        this.downloadTitle = downloadTitle;
    }

    public String getDownloadMessage() {
        return downloadMessage;
    }

    public void setDownloadMessage(String downloadMessage) {
        this.downloadMessage = downloadMessage;
    }

    public String getUpgradeTitle() {
        return upgradeTitle;
    }

    public void setUpgradeTitle(String upgradeTitle) {
        this.upgradeTitle = upgradeTitle;
    }

    public String getUpgradeMessage() {
        return upgradeMessage;
    }

    public void setUpgradeMessage(String upgradeMessage) {
        this.upgradeMessage = upgradeMessage;
    }

    public Android getAndroid() {
        return android;
    }

    public void setAndroid(Android android) {
        this.android = android;
    }

    public Ios getIos() {
        return ios;
    }

    public void setIos(Ios ios) {
        this.ios = ios;
    }

    public static class Ios {
        /**
         * Url to AppStore where the app can be downloaded. Example: https://itunes.apple.com/nl/app/abn-amro-mobiel-bankieren/id439728011?mt=8
         */

        private String appStoreUrl;
        /**
         * Base scheme of the app. Example: abnamro.nl://
         */
        private String scheme;

        /**
         * Url that the app should open. Can be of another scheme then app scheme. Example: nl.abnamro.signing.grip://signing?itemId=10101010101
         */
        private String deepLinkUrl;

        public String getAppStoreUrl() {
            return appStoreUrl;
        }

        public void setAppStoreUrl(String appStoreUrl) {
            this.appStoreUrl = appStoreUrl;
        }

        public String getScheme() {
            return scheme;
        }

        public void setAppScheme(String scheme) {
            this.scheme = scheme;
        }

        public String getDeepLinkUrl() {
            return deepLinkUrl;
        }

        public void setDeepLinkUrl(String deepLinkUrl) {
            this.deepLinkUrl = deepLinkUrl;
        }
    }

    public static class Android {
        /**
         * Name of the package that should be opened. Example: "com.abnamro.nl.mobile.payments"
         */
        private String packageName;

        /**
         * The minimum version of the package that needs to be installed. Example: 9.03.01.01 = 9030101
         */
        private int requiredMinimumVersion;

        /**
         * Url of the intent that should be opened. Example: nl.abnamro.signing.grip://signing?itemId=10101010101
         */
        private String intent;

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public int getRequiredMinimumVersion() {
            return requiredMinimumVersion;
        }

        public void setRequiredVersion(int requiredMinimumVersion) {
            this.requiredMinimumVersion = requiredMinimumVersion;
        }

        public void setIntent(String intent) {
            this.intent = intent;
        }

        public String getIntent() {
            return intent;
        }
    }
}
