package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen;

import se.tink.backend.aggregation.nxgen.http.URL;

public class RaiffeisenConstants {

    public static class Url {
        public static final URL ACCOUNTS = new URL("https://mein.elba.raiffeisen.at/api/pfp-pfm/vermoegen-ui-services/rest/vermoegen/konten");
        public static final URL KEEP_ALIVE = new URL("https://mein.elba.raiffeisen.at/api/pfp-widgetsystem/widgetsystem-ui-services/rest/legal");
        public static final URL HOME = new URL("https://banking.raiffeisen.at/");
        public static final URL LOGOUT = new URL("https://logout.raiffeisen.at/rest/36000/0");
        public static final URL REDIRECT = new URL("https://mein.elba.raiffeisen.at/pfp-widgetsystem/");
        public static final URL REFERER = new URL("https://mein.elba.raiffeisen.at/pfp-widgetsystem/");
        public static final URL REFERER_SSO = new URL("https://mein.elba.raiffeisen.at/");
        public static final URL SSO_BASE = new URL("https://sso.raiffeisen.at");
        public static final URL SSO_OAUTH = new URL("https://sso.raiffeisen.at/as/authorization.oauth2");
        public static final URL TRANSACTIONS = new URL("https://mein.elba.raiffeisen.at/api/pfp-umsatz/umsatz-ui-services/rest/umsatz-page-fragment/umsaetze");
    }

    public static class Header {
        public static final String ACCEPT_ACCOUNTS = "application/json, text/plain, */*";
        public static final String ACCEPT_ENCODING = "gzip, deflate, br";
        public static final String ACCEPT_LANGUAGE = "en-GB,en-US;q=0.9,en;q=0.8,sv;q=0.7";
        public static final String ACCEPT_MISC = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8";
        public static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";
        public static final String APP_VERSION = "8.4.1";
        public static final String CONNECTION_KEEP_ALIVE = "keep-alive";
        public static final String CONNECTION_KEY = "Connection";
        public static final String HOST = "app.raiffeisen.at";
        public static final String REFERER = "Referer";
        public static final String UPGRADE_INSECURE_REQUESTS = "1";
        public static final String UPGRADE_INSECURE_REQUESTS_KEY = "Upgrade-Insecure-Requests";
     }

    public static class RegExpPatterns {
        public static final String ACCESS_TOKEN = ".*access_token=([^&]+)";
        public static final String TOKEN_TYPE = ".*token_type=([^&]+)";
    }

    public static class IntValues {
        public static final int PASSWORD_LENGTH = 5;
        public static final int USERNAME_LENGTH = 8;
    }

    public enum Storage {
        WEB_LOGIN_RESPONSE
    }
}
