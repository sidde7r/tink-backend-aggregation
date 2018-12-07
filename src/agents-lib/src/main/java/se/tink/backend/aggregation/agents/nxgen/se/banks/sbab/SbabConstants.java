package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.sun.jersey.api.uri.UriTemplate;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.rpc.AccountTypes;

public abstract class SbabConstants {
    public static final String CURRENCY = "SEK";
    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(
                            AccountTypes.SAVINGS,
                            "savings",
                            "savings_account",
                            "minor_savings_account")
                    .build();

    public enum Environment {
        @JsonAlias({"sandbox", "SANDBOX"})
        SANDBOX("sandbox"),
        @JsonAlias({"production", "PRODUCTION"})
        PRODUCTION("api");

        private final String baseUri;

        Environment(String baseUri) {
            this.baseUri = baseUri;
        }

        public static Environment getEnvironmentOrDefault(String environmentString) {
            try {
                return Environment.valueOf(environmentString.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Environment.PRODUCTION;
            }
        }

        public String getBaseUri() {
            return baseUri;
        }

        @Override
        public String toString() {
            return baseUri;
        }
    }

    public static final class Urls {
        private static final String HOST = "https://api.sbab.se/{environment}";
        private static final String AUTH = HOST + "/auth/1.0";
        private static final String AUTHORIZE = AUTH + "/authorize";
        private static final String AUTH_STATUS = AUTH + "/status?pending_code={pendingCode}";
        private static final String TOKEN = AUTH + "/token";
        private static final String INVALIDATE_TOKEN = TOKEN + "/invalidate";
        private static final String ACCOUNTS = HOST + "/savings/2.0/accounts";
        private static final String ACCOUNT = ACCOUNTS + "/{accountNo}";
        private static final String TRANSFERS = ACCOUNT + "/transfers";
        private static final String TRANSFER = TRANSFERS + "/{transferId}";
        private static final String TRANSFER_STATUS = TRANSFER + "/status/{referenceId}";
        private static final String LOANS = HOST + "/loan/2.0/loans";
        private static final String LOAN = LOANS + "/{loanNumber}";
        private static final String AMORTISATION = LOAN + "/amortisation";

        public static String GET_PENDING_AUTH_CODE(Environment env) {
            return new UriTemplate(AUTHORIZE).createURI(env.toString());
        }

        public static String GET_AUTH_STATUS(Environment env, String pendingCode) {
            return new UriTemplate(AUTH_STATUS).createURI(env.toString(), pendingCode);
        }

        public static String GET_ACCESS_TOKEN(Environment env) {
            return new UriTemplate(ACCOUNT).createURI(env.toString());
        }

        public static String INVALIDATE_ACCESS_TOKEN(Environment env) {
            return new UriTemplate(INVALIDATE_TOKEN).createURI(env.toString());
        }

        public static String LIST_ACCOUNTS(Environment env) {
            return new UriTemplate(ACCOUNTS).createURI(env.toString());
        }

        public static String GET_ACCOUNT(Environment env, String accountNo) {
            return new UriTemplate(ACCOUNT).createURI(env.toString(), accountNo);
        }

        public static String INIT_TRANSFER(Environment env, String accountNo) {
            return new UriTemplate(TRANSFERS).createURI(env.toString(), accountNo);
        }

        public static String LIST_TRANSFERS(Environment env, String accountNo) {
            return new UriTemplate(TRANSFERS).createURI(env.toString(), accountNo);
        }

        public static String GET_TRANSFER(Environment env, String accountNo, String transferId) {
            return new UriTemplate(TRANSFER).createURI(env.toString(), accountNo, transferId);
        }

        public static String DELETE_TRANSFER(Environment env, String accountNo, String transferId) {
            return new UriTemplate(TRANSFER).createURI(env.toString(), accountNo, transferId);
        }

        public static String GET_TRANSFER_STATUS(Environment env, String accountNo, String refId) {
            return new UriTemplate(TRANSFER_STATUS).createURI(env.toString(), accountNo, refId);
        }

        public static String LIST_LOANS(Environment env) {
            return new UriTemplate(LOANS).createURI(env.toString());
        }

        public static String GET_LOAN(Environment env, String loanNumber) {
            return new UriTemplate(LOAN).createURI(env.toString(), loanNumber);
        }

        public static String GET_AMORTISATION(Environment env, String loanNumber) {
            return new UriTemplate(AMORTISATION).createURI(env.toString(), loanNumber);
        }
    }

    public static class ErrorCodes {}

    public static class LogTags {}

    public static class QueryKey {
        public static final String START_DATE = "startDate";
        public static final String END_DATE = "endDate";
    }

    public static class StorageKey {
        public static final String BASIC_AUTH_USERNAME = "basic_auth_username";
        public static final String BASIC_AUTH_PASSWORD = "basic_auth_password";
        public static final String PENDING_AUTH_CODE = "pending_auth_code";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String OAUTH2_TOKEN = "oauth2_token";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String USER_ID = "user_id";
    }
}
