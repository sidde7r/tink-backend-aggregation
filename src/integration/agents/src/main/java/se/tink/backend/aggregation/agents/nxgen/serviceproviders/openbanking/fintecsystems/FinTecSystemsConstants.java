package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FinTecSystemsConstants {

    public static final String INTEGRATION_NAME = "fintecsystems";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class HeaderKeys {
        static final String X_REQUEST_ID = "X-Request-ID";
        static final String AUTHORIZATION = "Authorization";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Urls {
        public static final String BASE_URL = "https://api.xs2a.com/v1/";

        public static final URL PAYMENT_INITIATION = new URL(BASE_URL + "payments");
        public static final URL FETCH_PAYMENT_STATUS =
                new URL(BASE_URL + "payments/{transactionId}");
        public static final URL GET_SESSION_STATUS = new URL(BASE_URL + "sessions/{transactionId}");

        public static final String FTS_WIDGET_CDN = "https://cdn.tink.se/fts/widget.html";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class PathVariables {
        public static final String TRANSACTION_ID = "transactionId";
        public static final String WIZARD_SESSION_KEY = "wizard_session_key";
        public static final String MFA_ID = "mfa_id";
        public static final String REDIRECT_URI = "redirect_uri";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Constants {
        public static final String API_USER_NAME = "api";
        // its temporary test key to unblock SDK, later real key will be stored in Vault
        public static final String TEST_API_KEY = "dNieKjGDPzz5fipa2nz6FIwjWK8ZIEUSMKVCx86f";
        public static final String REDIRECT_URL =
                "https://api.tink.se/api/v1/credentials/third-party/callback";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ErrorMessages {
        public static final String ERROR_MESSAGE_401 =
                "Authentication failed API key missing or not valid";
        public static final String ERROR_MESSAGE_403 = "Forbidden Access blocked";
        public static final String ERROR_MESSAGE_404 =
                "Product not enabled or Not found The requested object does not exist on the server";
        public static final String ERROR_MESSAGE_422 =
                "Validation failed Something is wrong with the user input";
        public static final String ERROR_MESSAGE_500 = "An internal server problem occurred";
    }
}
