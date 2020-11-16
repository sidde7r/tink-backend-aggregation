package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class SodexoConstants {

    public static final String CURRENCY = "EUR";

    public static class Urls {
        public static final String BASE_URL = "https://mobile.sodexobeneficios.pt";
        public static final URL PRELOGIN_STATUS =
                new URL(BASE_URL + "/api/v2/get-prelogin-status/");
        public static final URL SING_IN_REGISTER =
                new URL(BASE_URL + "/api/v2/sign-in-registered/");
        public static final URL SING_IN = new URL(BASE_URL + "/api/v2/sign-in/");
        public static final URL RESET_PIN = new URL(BASE_URL + "/api/v2/reset-pin/");
        public static final URL BALANCE = new URL(BASE_URL + "/api/v2/get-detailed-balance/");
        public static final URL GET_TRANSACTION = new URL(BASE_URL + "/api/v2/get-transactions/0/");
    }

    public static class RequestBodyValues {
        public static final String NIF = "nif";
        public static final String PASSWORD = "password";
        public static final String PIN_NEW = "pin-new";
        public static final String PIN = "pin";
    }

    public static class Headers {
        public static final String X_APP_VERSION = "x-app-version";
        public static final String X_APP_VERSION_VALUE = "2.1.0";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONTENT_TYPE_X_FORM = "application/x-www-form-urlencoded";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class Storage {
        public static final String USER_TOKEN = "USER_TOKEN";
        public static final String SESSION_TOKEN = "SESSION_TOKEN";
        public static final String PIN = "PIN";
        public static final String NAME = "NAME";
        public static final String SURNAME = "SURNAME";
        public static final String CARD_NUMBER = "CARD_NUMBER";
    }
}
