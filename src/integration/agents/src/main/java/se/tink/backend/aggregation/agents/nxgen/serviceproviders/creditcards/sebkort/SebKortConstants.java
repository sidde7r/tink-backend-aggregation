package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SebKortConstants {
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Stockholm");
    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ");
    public static final String AUTHORIZATION_UUID =
            "ca4d47c0-e61f-30ad-b0c0-86d20f26d5eb"; // Required (and the same) for all SEB Kort

    public static class Urls {
        private static final String SEBKORT_HOST = "https://secure.eurocard.se";
        public static final URL SEBKORT_LOGIN = new URL(SEBKORT_HOST + "/sea/external/EidLogin");
        public static final URL SEBKORT_LOGOUT = new URL(SEBKORT_HOST + "/pkmslogout");
        public static final URL SEBKORT_AUTH =
                new URL(SEBKORT_HOST + "/nauth4/Authentication/Auth");
        public static final URL SEBKORT_CARDS =
                new URL(SEBKORT_HOST + "/api/common/cardcontracts/v1/");
        public static final URL SEBKORT_RESERVATIONS =
                new URL(SEBKORT_HOST + "/api/common/reservations/v1/");
        public static final URL SEBKORT_TRANSACTIONS =
                new URL(SEBKORT_HOST + "/api/common/transactions/v1/");

        private static final String BANKID_HOST = "https://id.signicat.com";
        public static final URL BANK_ID_INIT = new URL(BANKID_HOST + "/std/method/seb");
    }

    public static class StorageKey {
        public static final String AUTHORIZATION = "Authorization";
        public static final String CARD_ID = "CARD_ID";
        public static final String CARD_CONTRACT_ID = "CARD_CONTRACT_ID";
    }

    public static class QueryKey {
        public static final String LANGUAGE_CODE = "languageCode";
        public static final String CARD_ACCOUNT_ID = "cardAccountId";
        public static final String CARD_CONTRACT_ID = "cardContractId";
        public static final String FROM_DATE = "fromDate";
        public static final String TO_DATE = "toDate";
        public static final String METHOD = "method";
        public static final String PROFILE = "profile";
        public static final String LANGUAGE = "language";
        public static final String PREFILLED_SUBJECT = "prefilled.subject";
        public static final String TARGET = "target";
        public static final String REDIRECT = "redirect";
    }

    public static class QueryValue {
        public static final String LANGUAGE_CODE = "EN";
        public static final String PROFILE = "app";
        public static final String LANGUAGE = "en";
        public static final String TARGET = Urls.SEBKORT_HOST + "/mbidcomplete";
        public static final String REDIRECT = "/nis/m/%s/external/logout";
    }

    public static class FormKey {
        public static final String PRODGROUP = "prodgroup";
        public static final String SAML_RESPONSE = "SAMLResponse";
        public static final String SEB_REFERER = "SEB_Referer";
        public static final String TARGET_URL = "targetUrl";
        public static final String COUNTRY_CODE = "countryCode";
        public static final String UID = "UID";
        public static final String TARGET = "target";
        public static final String TYPE = "TYPE";
        public static final String SECRET = "scrt";
        public static final String SEB_AUTH_MECHANISM = "SEB_Auth_Mechanism";
    }

    public static class FormValue {
        public static final String COUNTRY_CODE = "SE";
        public static final String TARGET_URL = Urls.SEBKORT_HOST + "/mbidcomplete";
        public static final String SEB_REFERER = "/nis";
        public static final String TARGET = "/nis/m/%s/login/loginSuccess";
        public static final String TYPE = "EID";
        public static final String SEB_AUTH_MECHANISM = "5";
    }
}
