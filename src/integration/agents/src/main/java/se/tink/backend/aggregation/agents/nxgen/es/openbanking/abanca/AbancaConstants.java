package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public final class AbancaConstants {

    private AbancaConstants() {
        throw new AssertionError();
    }

    public static class Urls {

        public static final String BASE_API_URL = "https://api.abanca.com/psd2";
        public static final String BASE_AUTH_URL = "https://api.abanca.com";

        public static final URL AUTHORIZATION = new URL(BASE_AUTH_URL + Endpoints.AUTHORIZATION);
        public static final URL TOKEN = new URL(BASE_AUTH_URL + Endpoints.TOKEN);
        public static final URL ACCOUNTS = new URL(BASE_API_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(BASE_API_URL + Endpoints.TRANSACTIONS);
        public static final URL BALANCE = new URL(BASE_API_URL + Endpoints.BALANCE);
    }

    public static class Endpoints {
        public static final String AUTHORIZATION = "/oauth/{clientId}/Abanca";
        public static final String ACCOUNTS = "/me/accounts";
        public static final String TRANSACTIONS = "/me/accounts/{accountId}/transactions";
        public static final String TOKEN = "/oauth2/token";
        public static final String BALANCE = "/me/accounts/{accountId}/balance";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
    }

    public static class HeaderKeys {
        public static final String AUTH_KEY = "AuthKey";
        public static final String CHALLENGE_ID = "x-challenge-id";
        public static final String CHALLENGE_RESPONSE = "x-challenge-response";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String INVALID_BALANCE_RESPONSE = "Invalid balance response";
    }

    public class UrlParameters {

        public static final String ACCOUNT_ID = "accountId";
        public static final String CLIENT_ID = "clientId";
    }

    public class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
    }

    public class QueryValues {
        public static final String CODE = "CODE";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String APPLICATION = "APLICACION";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String GRANT_TYPE_CODE = "authorization_code";
        public static final String GRANT_TYPE_REFRESH = "refresh_token";
    }

    public static class ResponseErrorCodes {
        public static final String CHALLENGE_REQUIRED = "API_00005";
        public static final String INVALID_CHALLENGE_VALUE = "API_00006";
    }

    public enum UserMessage implements LocalizableEnum {
        GET_CHALLENGE_RESPONSE_DESCRIPTION(
                new LocalizableKey("Enter the code you received from the bank")),
        INVALID_CHALLENGE_RESPONSE(new LocalizableKey("Failed to read user input"));

        private final LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return this.userMessage;
        }
    }
}
