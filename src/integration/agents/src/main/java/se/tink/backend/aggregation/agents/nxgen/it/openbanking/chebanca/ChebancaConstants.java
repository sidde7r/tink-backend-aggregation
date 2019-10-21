package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class ChebancaConstants {

    public static final String INTEGRATION_NAME = "chebanca";
    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, "CONTO CORRENTE")
                    .put(AccountTypes.CREDIT_CARD, "CARTA DI CREDITO")
                    .build();

    private ChebancaConstants() {
        throw new AssertionError();
    }

    enum HeadersToSign {
        REQUEST_TARGET("(request-target)"),
        DIGEST("Digest"),
        TPP_REQUEST_ID("TPP-Request-ID"),
        DATE("Date");
        private String header;

        HeadersToSign(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        public static final String BASE_URL = "https://external-api.chebanca.io";

        public static final URL AUTHORIZE = new URL(BASE_URL + ApiServices.AUTHORIZE);
        public static final URL TOKEN = new URL(BASE_URL + ApiServices.TOKEN);
        public static final URL CUSTOMER_ID = new URL(BASE_URL + ApiServices.CUSTOMER_ID);
        public static final URL ACCOUNTS = new URL(BASE_URL + ApiServices.ACCOUNTS);
        public static final URL BALANCES = new URL(BASE_URL + ApiServices.BALANCES);
        public static final URL TRANSACTIONS = new URL(BASE_URL + ApiServices.TRANSACTIONS);
        public static final URL CONSENT = new URL(BASE_URL + ApiServices.CONSENT);
        public static final URL CONSENT_AUTHORIZATION =
                new URL(BASE_URL + ApiServices.CONSENT_AUTHORIZATION);
        public static final URL CONSENT_CONFIRMATION =
                new URL(BASE_URL + ApiServices.CONSENT_CONFIRMATION);
    }

    public static class ApiServices {
        public static final String AUTHORIZE = "/auth/oauth/v2/authorize";
        public static final String TOKEN = "/auth/oauth/v2/token";
        public static final String CUSTOMER_ID = "/private/customers/customerid-info";
        public static final String ACCOUNTS = "/private/customers/{customerId}/accounts";
        public static final String BALANCES =
                "/private/customers/{customerId}/products/{productId}/balance/retrieve";
        public static final String TRANSACTIONS =
                "/private/customers/{customerId}/products/{productId}/transactions/retrieve";
        public static final String CONSENT = "/private/customers/{customerId}/consent/request";
        public static final String CONSENT_AUTHORIZATION =
                "/private/auth/security/sca/{resourceId}/approach";
        public static final String CONSENT_CONFIRMATION =
                "/private/customers/{customerId}/consent/request/{resourceId}/confirm";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CUSTOMER_ID = "customer-id";
        public static final String AUTHORIZATION_URL = "authorization-url";
    }

    public static class QueryKeys {

        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String REQUEST_TARGET = "(request-target)";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {

        public static final String CODE = "code";
    }

    public static class HeaderKeys {
        public static final String TPP_REQUEST_ID = "TPP-Request-ID";
        public static final String DIGEST = "Digest";
        public static final String DATE = "Date";
        public static final String SIGNATURE = "Signature";
        public static final String GET_METHOD = "get";
        public static final String POST_METHOD = "post";
        public static final String PUT_METHOD = "put";
        public static final String LOCATION = "Location";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
    }

    public static class HeaderValues {
        public static final String SHA_256 = "SHA-256=";
        public static final String SIGNATURE_HEADER =
                "keyId=\"TINK\",algorithm=\"rsa-sha256\",headers=\"%s\",signature=\"%s\"";
    }

    public static class IdTags {
        public static final String CUSTOMER_ID = "customerId";
        public static final String PRODUCT_ID = "productId";
        public static final String RESOURCE_ID = "resourceId";
    }

    public static class FormKeys {}

    public static class FormValues {

        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String ACCOUNT_INFO = "ACCOUNT_INFO";
    }

    public static class LogTags {}
}
