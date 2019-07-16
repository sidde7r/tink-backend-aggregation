package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class PayPalConstants {

    public static final String INTEGRATION_NAME = "paypal";

    private PayPalConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String EMAIL_NOT_FOUND = "No valid email found for the user.";
        public static final String MISSING_NEXT_PAGE_TOKEN =
                "Next page token for transaction fetching is missing.";
        public static final String NEXT_PAGE_TOKEN_EXTRACT_FAILED =
                "Next page token for transaction fetching cannot be extracted from URL.";
    }

    public static class Urls {
        public static final String BASE = "https://api.sandbox.paypal.com";
        public static final String BASE_AUTH = "https://www.sandbox.paypal.com";
        public static final URL AUTHORIZE = new URL(BASE_AUTH + Endpoints.AUTHORIZE);
        public static final URL TOKEN = new URL(BASE + Endpoints.TOKEN);
        public static final URL IDENTITY = new URL(BASE + Endpoints.IDENTITY);
        public static final URL BALANCE = new URL(BASE + Endpoints.BALANCE);
        public static final URL TRANSACTIONS = new URL(BASE + Endpoints.TRANSACTIONS);
        public static final URL CREATE_PERSONAL_PAYMENT =
                new URL(BASE + Endpoints.PERSONAL_PAYMENT);
        public static final URL PAYMENT_DETAILS = new URL(BASE + Endpoints.PAYMENT_DETAILS);
        public static final URL ORDER_PAYMENT = new URL(BASE + Endpoints.ORDER_PAYMENT);
        public static final URL ORDER_PAYMENT_DETAILS =
                new URL(BASE + Endpoints.ORDER_PAYMENT_DETAILS);
    }

    public static class Endpoints {
        public static final String AUTHORIZE = "/signin/authorize";
        public static final String TOKEN = "/v1/oauth2/token";
        public static final String IDENTITY = "/v1/identity/oauth2/userinfo";
        public static final String BALANCE = "/v2/wallet/balance-accounts";
        public static final String TRANSACTIONS = "/v1/activities/activities";
        public static final String PERSONAL_PAYMENT = "/v1/payments/personal-payment-tokens";
        public static final String PAYMENT_DETAILS =
                "/v1/payments/personal-payment-tokens/{payment_token}";
        public static final String ORDER_PAYMENT = "/v2/checkout/orders";
        public static final String ORDER_PAYMENT_DETAILS = "/v2/checkout/orders/{paymentId}";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String SCHEMA = "schema";
        public static final String START_TIME = "start_time";
        public static final String END_TIME = "end_time";
        public static final String NEXT_PAGE_TOKEN = "next_page_token";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE =
                "openid email https://uri.paypal.com/services/identity/activities/psd2 https://uri.paypal.com/services/wallet/balance-accounts/read";
        public static final String SCHEMA = "paypalv1.1";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String GRANT_TYPE = "authorization_code";
        public static final String GRANT_TYPE_REFRESH = "refresh_token";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
    }

    public static class Formats {
        public static final String TRANSACTION_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    }

    public static class TransactionStatus {
        public static final String PENDING = "PENDING";
    }

    public static class LinkRelations {
        public static final String NEXT = "next";
    }

    public static class PathTags {
        public static final String PAYMENT_TOKEN = "payment_token";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class RequestConstants {
        public static final String CAPTURE = "CAPTURE";
        public static final String REFERENCE_TYPE = "LINKS";
    }

    public static class LinkTypes {
        public static final String APPROVE = "approve";
    }

    public static class RunConfigurationKeys {
        public static final String RUN_CONFIGURATION = "RUN_CONFIGURATION";
    }

    public static class RunConfigurationValues {
        public static final String WIP = "WiP";
    }
}
