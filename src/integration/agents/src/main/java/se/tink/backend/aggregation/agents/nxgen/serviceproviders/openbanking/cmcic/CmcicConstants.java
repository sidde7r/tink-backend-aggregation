package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public final class CmcicConstants {

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "some_string1_the_integratee_uses")
                    .put(AccountTypes.SAVINGS, "some_string2_the_integratee_uses")
                    .put(AccountTypes.CREDIT_CARD, "some_string3_the_integratee_uses")
                    .ignoreKeys("some_string4_the_integratee_uses")
                    .build();

    private CmcicConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        public static final String FETCH_ACCOUNTS_PATH = "stet-psd2-api/v1/accounts";
        public static final String TOKEN_PATH = "oauth2/token";
        public static final String FETCH_TRANSACTIONS_PATH =
                "stet-psd2-api/v1/accounts/%s/transactions";
        public static final String PAYMENT_REQUESTS = "stet-psd2-api/v1/payment-requests";
        public static final String SUCCESS_REPORT_PATH = "?code=123&state=";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CODE_VERIFIER = "CODE_VERIFIER";
        public static final String CLIENT_CREDENTIALS_TOKEN = "CLIENT_CREDENTIALS_TOKEN";
        public static final String STATE = "STATE";
    }

    public static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE = "aisp";
        public static final String CODE_CHALLENGE_METHOD = "S256";
    }

    public static class Signature {
        public static final String SIGNING_STRING = "(request-target): ";
        public static final String DATE = "date: ";
        public static final String TIMEZONE = "GMT";
        public static final String DIGEST = "digest: ";
        public static final String ALGORITHM = "algorithm=\"rsa-sha256\"";
        public static final String DIGEST_PREFIX = "SHA-256=";
        public static final String SIGNATURE = "Signature";
        public static final String KEY_ID_NAME = "keyId=";
        public static final String SIGNING_ALGORITHM = "RSA";
        public static final String HEADERS =
                "headers=\"(request-target) date digest x-request-id content-type\"";
        public static final String SIGNATURE_NAME = "signature=";
        public static final String HTTP_METHOD_POST = "post";
        public static final String HTTP_METHOD_GET = "get";
        public static final String X_REQUEST_ID = "x-request-id: ";
        public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
        public static final String CONTENT_TYPE = "content-type: ";
    }

    public static class HeaderKeys {
        public static final String DIGEST = "Digest";
        public static final String DATE = "Date";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String AUTHORIZATION = "Authorization";
        public static final String SIGNATURE = "Signature";
        public static final String CONTENT_TYPE = "content-type";
    }

    public static class FormKeys {

        public static final String CLIENT_ID = "client_id";
        public static final String GRANT_TYPE = "grant_type";
        public static final String SCOPE = "scope";
    }

    public static class FormValues {
        public static final String EMPTY = "";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_ID = "client_id";
        public static final String CODE = "code";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String BENEFICIARY_NAME = "myMerchant";
        public static final String INSTRUCTION_ID = "MyInstrId";
        public static final String PISP = "pisp";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
    }

    public class DateFormat {
        public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    }
}
