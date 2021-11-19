package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SodexoConstants {

    public static final String CURRENCY = "EUR";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Urls {
        static final String BASE_URL = "https://mobile.sodexobeneficios.pt";
        static final URL PRELOGIN_STATUS = new URL(BASE_URL + "/api/v2/get-prelogin-status/");
        static final URL SING_IN_REGISTER = new URL(BASE_URL + "/api/v2/sign-in-registered/");
        static final URL SING_IN = new URL(BASE_URL + "/api/v2/sign-in/");
        static final URL RESET_PIN = new URL(BASE_URL + "/api/v2/reset-pin/");
        static final URL BALANCE = new URL(BASE_URL + "/api/v2/get-detailed-balance/");
        static final URL GET_TRANSACTION = new URL(BASE_URL + "/api/v2/get-transactions/0/");
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RequestBodyValues {
        public static final String NIF = "nif";
        public static final String PASSWORD = "password";
        public static final String PIN_NEW = "pin-new";
        public static final String PIN = "pin";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Headers {
        static final String X_APP_VERSION = "x-app-version";
        static final String X_APP_VERSION_VALUE = "2.3.1";
        static final String CONTENT_TYPE = "Content-Type";
        static final String CONTENT_TYPE_X_FORM = "application/x-www-form-urlencoded";
        static final String AUTHORIZATION = "Authorization";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Storage {
        static final String USER_TOKEN = "USER_TOKEN";
        static final String SESSION_TOKEN = "SESSION_TOKEN";
        static final String PIN = "PIN";
        static final String NAME = "NAME";
        static final String SURNAME = "SURNAME";
        static final String CARD_NUMBER = "CARD_NUMBER";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class ErrorMessages {
        static final String AUTH_ERROR = "A autenticação falhou.";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class HttpClient {
        static final int MAX_RETRIES = 3;
        static final int RETRY_SLEEP_MILLISECONDS = 3000;
    }
}
