package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class DemobankConstants {
    public static final TypeMapper<AccountHolderType> HOLDER_TYPE_TYPE_MAPPER =
            TypeMapper.<AccountHolderType>builder()
                    .put(AccountHolderType.PERSONAL, "PERSONAL")
                    .put(AccountHolderType.BUSINESS, "BUSINESS")
                    .put(AccountHolderType.CORPORATE, "CORPORATE")
                    .build();

    public static final TypeMapper<Party.Role> PARTY_ROLE_TYPE_MAPPER =
            TypeMapper.<Party.Role>builder()
                    .put(Party.Role.HOLDER, "HOLDER")
                    .put(Party.Role.AUTHORIZED_USER, "AUTHORIZED_USER")
                    .put(Party.Role.OTHER, "OTHER")
                    .put(Party.Role.UNKNOWN, "UNKNOWN")
                    .build();

    public static final ChronoUnit DEFAULT_OB_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;
    public static final int DEFAULT_OB_TOKEN_LIFETIME = 90;

    public static class Urls {
        public static final URL BASE_URL = new URL("https://demobank.production.global.tink.se");

        public static final URL OAUTH_TOKEN = new URL(BASE_URL + "/oauth/token");
        public static final URL OAUTH_AUTHORIZE = new URL(BASE_URL + "/oauth/authorize");
        public static final URL ACCOUNTS = new URL(BASE_URL + "/api/accounts");
        public static final URL TRANSACTIONS =
                new URL(BASE_URL + "/api/account/{accountId}/transactions");
        public static final URL HOLDERS = new URL(BASE_URL + "/api/account/{accountId}/holders");
        public static final URL USER = new URL(BASE_URL + "/api/user");

        // App to app URLs
        public static final URL A2A_INIT_URL = new URL(BASE_URL + "/api/auth/ticket");
        public static final URL A2A_INIT_DECOUPLED_URL =
                new URL(BASE_URL + "/api/auth/ticket/decoupled");
        public static final URL A2A_COLLECT_URL =
                new URL(BASE_URL + "/api/auth/ticket/{ticketId}/collect");

        // Norwegian BankID mocks
        public static final URL NO_BANKID_INIT = new URL(BASE_URL + "/auth/no/init");
        public static final URL NO_BANKID_COLLECT = new URL(BASE_URL + "/auth/no/collect");

        // Denmark NemID mocks
        public static final URL DK_NEMID_GET_CHALLENGE =
                new URL(BASE_URL + "/auth/dk/mobilbank/nemid/get_challange");
        public static final URL DK_NEMID_GENERATE_CODE =
                new URL(BASE_URL + "/auth/dk/mobilbank/nemid/generatecode");
        public static final URL DK_NEMID_ENROLL =
                new URL(BASE_URL + "/auth/dk/mobilbank/nemid/inroll");
        public static final URL DK_NEMID_LOGIN =
                new URL(BASE_URL + "/auth/dk/mobilbank/nemid/login_with_installid_prop");

        // Embedded OTP
        public static final URL EMBEDDED_OTP_COMMENCE =
                new URL(BASE_URL + "/api/auth/otplogin/commence");
        public static final URL EMBEDDED_OTP_COMPLETE =
                new URL(BASE_URL + "/api/auth/otplogin/complete");

        // Payments
        public static final URL SIGN_PAYMENT =
                new URL(
                        BASE_URL
                                + "/api/payment/v1/{payment_service_type}/sign/paymentId/{payment_id}");
    }

    public static class QueryParams {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String STATE = "state";
        public static final String DATE_FROM = "from";
        public static final String DATE_TO = "to";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String PAYMENT_ID = "payment_id";
        public static final String PAYMENT_SERVICE_TYPE = "payment_service_type";

        public static final String ACCOUNT_ID = "accountId";
        public static final String TICKET_ID = "ticketId";

        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String GRANT_TYPE = "grant_type";
    }

    public static class GrantTypes {
        public static final String PASSWORD = "password";
    }

    public static class Scopes {
        public static final String PAYMENT_WRITE = "payment:write";
    }

    public static class OAuth2Params {
        public static final String CLIENT_ID = "aggregation";
        public static final String CLIENT_SECRET = "password";
    }

    public static class QueryParamsValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String CLIENT_ID = OAuth2Params.CLIENT_ID;
    }

    public static class StorageKeys {
        public static final String OAUTH2_TOKEN = "oAuth2Token";
    }

    public static class ClusterIds {
        public static final String OXFORD_PREPROD = "oxford-preprod";
        public static final String OXFORD_STAGING = "oxford-staging";
    }

    public static class AccountTypes {
        public static final String CHECKING = "CHECKING";
        public static final String CREDIT_CARD = "CREDIT_CARD";
    }

    @Getter
    @RequiredArgsConstructor
    public enum ScaApproach {
        REDIRECT("redirect"),
        OTP("otp");

        private final String approach;
    }

    public static class PaymentServiceTypes {
        public static final String PAYMENTS = "payments";
    }

    public static class ClusterSpecificCallbacks {

        public static final String OXFORD_STAGING_CALLBACK =
                "https://main.staging.oxford.tink.se/api/v1/credentials/third-party/callback";
        public static final String OXFORD_PREPROD_CALLBACK =
                "https://api.preprod.oxford.tink.com/api/v1/credentials/third-party/callback";
        public static final String OXFORD_PROD_CALLBACK =
                "https://api.tink.com/api/v1/credentials/third-party/callback";
    }

    public static class ErrorMessages {
        public static final String RESPONSE_DOES_NOT_CONTAIN_SCA_LINK =
                "Response does not contain sca redirect link";
        public static final String RESPONSE_DOES_NOT_CONTAIN_EMBEDDED_AUTH_LINK =
                "Response does not contain embedded authorisation link";
        public static final String INVALID_OTP_CODE = "Invalid OTP code";
        public static final String PAYMENT_ID_NOT_FOUND =
                "PaymentId has not been found in the storage";
        public static final String OAUTH_TOKEN_NOT_FOUND =
                "OAuthToken has not been found in the storage";
        public static final String AUTHORIZE_URL_NOT_FOUND =
                "Authorize Url has not been found in the storage";
        public static final String EMBEDDED_AUTHORIZE_URL_NOT_FOUND =
                "Embedded authorize Url has not been found in the storage";
    }

    public static final String PAYMENT_CLIENT_TOKEN_HEADER = "X-Client-Header";
    public static final String PAYMENT_CLIENT_TOKEN =
            "sXVn6Lt9P3AYaK4YwuErsu4qn2AUW3AnSE4rMzcZdLFCM2vHn8JpbUfBUJc3e7VaB4ApgJLvYB2fd7GjwkBxauXbSaRx2w5WXeh3jKgeLFefhvQHeSewMLsDXAAU63Ax3B47EMPa2UiCWE6VmJkXKC4Uv6pfYsLTGqSnw3pR4kkec8d5Y5eJ5NWrCXXZ5gFjhbuBkeZpFvnsBTnHJYbK3XWAZ8jR9Qhbqb2vQkthGZm49U5eHQ5K7emuT9szTEv2";
}
