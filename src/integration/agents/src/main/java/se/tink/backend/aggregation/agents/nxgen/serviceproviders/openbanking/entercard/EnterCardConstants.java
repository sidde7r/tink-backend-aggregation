package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class EnterCardConstants {

    public static final String INTEGRATION_NAME = "entercard";
    public static final String DEFAULT_CURRENCY = "SEK";

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.LOAN, "loan")
                    .put(AccountTypes.CREDIT_CARD, "card")
                    .build();

    private EnterCardConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final List<String> INVALID_TOKEN = ImmutableList.of("invalid_grant");
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        public static final URL AUTHORIZATION = new URL(Endpoints.BASE_URL + Endpoints.AUTHORIZE);
        public static final URL TOKEN = new URL(Endpoints.BASE_URL + Endpoints.TOKEN);
        public static final URL ACCOUNTS = new URL(Endpoints.BASE_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(Endpoints.BASE_URL + Endpoints.MOVEMENTS);
    }

    public static class Endpoints {
        public static final String BASE_URL = "https://openbanking.entercard.com";
        public static final String AUTHORIZE = "/oauth/v2/oauth-authorize";
        public static final String TOKEN = "/oauth/v2/oauth-token";
        public static final String ACCOUNTS = "/accountdetails/api/v1/account";
        public static final String MOVEMENTS = "/accountmovements/api/v1/movements";
        public static final String BASE_URL_PAYMENTS =
                "https://private-anon-8a2aa31897-paymentservices4.apiary-mock.com/payment-services";
        public static final String PAYMENT = "/v1/payments";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CODE_VERIFIER = "CODE_VERIFIER";
        public static final String AUTH_CODE = "AUTHORIZATION_CODE";
        public static final String TOKEN = "TOKEN";
        public static final String ACCOUNT_NUMBER = "ACCOUNT_NUMBER";
    }

    public static class QueryKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE = "code";
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String CODE_CHALLENGE = "codeChallenge";
        public static final String CODE_CHALLENGE_METHOD = "codeChallengeMethod";
        public static final String STATE = "state";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String CODE_VERIFIER = "codeVerifier";
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String SSN = "ssn";
        public static final String INCLUDE_CARD_MOVEMENTS = "includeCardMovements";
        public static final String START_AT_ROW_NUMBER = "startAtRowNum";
        public static final String STOP_AFTER_ROW_NUMBER = "stopAfterRowNum";
    }

    public static class QueryValues {
        public static final String SCOPE = "Read openid";
        public static final String RESPONSE_TYPE = "code";
        public static final String S256 = "S256";
        public static final String GRANT_TYPE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String TRUE = "true";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String API_KEY = "api-key";
    }

    public static class FormKeys {
        public static final String RESPONSE_TYPE = "responseType";
        public static final String CLIENT_ID = "clientId";
    }

    public static class FormValues {
        public static final String RESPONSE_TYPE = "responseType";
    }

    public static class AccountType {
        public static final String CREDIT_CARD = "card";
    }

    public static class Transactions {
        public static final String OPEN = "OPEN";
    }

    public static class Format {
        public static final String TIMEZONE = "UTC";
        public static final String TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    }

    public static class CredentialKeys {
        public static final String SSN = "username";
    }

    public static class BrandedCards {
        public static final String COOP = "COOP";
        public static final String REMEMBER = "RE:MEMBER";
        public static final String MORE_GOLF = "GOLF";
        public static final String MERVARDE = "HANDELS";
    }
}
