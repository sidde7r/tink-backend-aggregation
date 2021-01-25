package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class AbancaConstants {

    private AbancaConstants() {
        throw new AssertionError();
    }

    public static final class Urls {
        private Urls() {}

        public static final String BASE_API_URL = "https://api.abanca.com";

        public static final URL AUTHORIZATION = new URL(BASE_API_URL + Endpoints.AUTHORIZATION);
        public static final URL TOKEN = new URL(BASE_API_URL + Endpoints.TOKEN);
        public static final URL ACCOUNTS = new URL(BASE_API_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(BASE_API_URL + Endpoints.TRANSACTIONS);
        public static final URL BALANCE = new URL(BASE_API_URL + Endpoints.BALANCE);
    }

    public static final class Endpoints {
        private Endpoints() {}

        public static final String AUTHORIZATION = "/oauth/{clientId}/Abanca";
        public static final String TOKEN = "/oauth2/token";
        public static final String ACCOUNTS = "/psd2/me/accounts";
        public static final String TRANSACTIONS = "/psd2/me/accounts/{accountId}/transactions";
        public static final String BALANCE = "/psd2/me/accounts/{accountId}/balance";
    }

    public static final class StorageKeys {
        private StorageKeys() {}

        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
    }

    public static final class HeaderKeys {
        private HeaderKeys() {}

        public static final String AUTH_KEY = "AuthKey";
        public static final String CHALLENGE_ID = "x-challenge-id";
        public static final String CHALLENGE_RESPONSE = "x-challenge-response";
    }

    public static final class ErrorMessages {
        private ErrorMessages() {}

        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String INVALID_BALANCE_RESPONSE = "Invalid balance response";
    }

    public class UrlParameters {
        private UrlParameters() {}

        public static final String ACCOUNT_ID = "accountId";
        public static final String CLIENT_ID = "clientId";
    }

    public class QueryKeys {
        private QueryKeys() {}

        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
    }

    public class QueryValues {

        private QueryValues() {}

        public static final String CODE = "CODE";
    }

    public static class FormKeys {
        private FormKeys() {}

        public static final String GRANT_TYPE = "grant_type";
        public static final String APPLICATION = "APLICACION";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static final class FormValues {
        private FormValues() {}

        public static final String GRANT_TYPE_CODE = "authorization_code";
        public static final String GRANT_TYPE_REFRESH = "refresh_token";
    }

    public static final class ResponseErrorCodes {
        private ResponseErrorCodes() {}

        public static final String CHALLENGE_REQUIRED = "API_00005";
        public static final String INVALID_CHALLENGE_VALUE = "API_00006";
    }

    public static final class ChallengeType {
        private ChallengeType() {}

        public static final String OTP_SMS = "otp_sms";
        public static final String OTP_MOBILE = "otp_mobile";
        public static final String OTP_DEVICE = "otp_device";
    }

    public static final class SupplementalFields {
        private SupplementalFields() {}

        public static final String OTP_SMS_DESCRIPTION = "otp_sms_description";
        public static final String OTP_MOBILE_DESCRIPTION = "otp_mobile_description";
        public static final String OTP_DEVICE_DESCRIPTION = "otp_device_description";
        public static final String CHALLENGE_RESPONSE = "response";
    }
}
