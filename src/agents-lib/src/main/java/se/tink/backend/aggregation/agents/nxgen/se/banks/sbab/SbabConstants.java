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

    public static final class Uris {
        private static final String BASE_URL = "https://api.sbab.se/{environment}";

        private static final String AUTH = "/auth/1.0";
        private static final String AUTHORIZE = AUTH + "/authorize";
        private static final String AUTH_STATUS = AUTH + "/status?pending_code={pendingCode}";
        private static final String TOKEN = AUTH + "/token";
        private static final String INVALIDATE_TOKEN = TOKEN + "/invalidate";
        private static final String ACCOUNTS = "/savings/2.0/accounts";
        private static final String ACCOUNT = ACCOUNTS + "/{accountNo}";
        private static final String TRANSFERS = ACCOUNT + "/transfers";
        private static final String TRANSFER = TRANSFERS + "/{transferId}";
        private static final String TRANSFER_STATUS = TRANSFER + "/status/{referenceId}";
        private static final String LOANS = "/loan/2.0/loans";
        private static final String LOAN = LOANS + "/{loanNumber}";
        private static final String AMORTISATION = LOAN + "/amortisation";

        public static String GET_BASE_URL(Environment env) {
            return new UriTemplate(BASE_URL).createURI();
        }

        public static String GET_PENDING_AUTH_CODE() {
            return new UriTemplate(AUTHORIZE).createURI();
        }

        public static String GET_AUTH_STATUS(String pendingCode) {
            return new UriTemplate(AUTH_STATUS).createURI(pendingCode);
        }

        public static String GET_ACCESS_TOKEN() {
            return new UriTemplate(ACCOUNT).createURI();
        }

        public static String INVALIDATE_ACCESS_TOKEN() {
            return new UriTemplate(INVALIDATE_TOKEN).createURI();
        }

        public static String LIST_ACCOUNTS() {
            return new UriTemplate(ACCOUNTS).createURI();
        }

        public static String GET_ACCOUNT(String accountNo) {
            return new UriTemplate(ACCOUNT).createURI(accountNo);
        }

        public static String INIT_TRANSFER(String accountNo) {
            return new UriTemplate(TRANSFERS).createURI(accountNo);
        }

        public static String LIST_TRANSFERS(String accountNo) {
            return new UriTemplate(TRANSFERS).createURI(accountNo);
        }

        public static String GET_TRANSFER(String accountNo, String transferId) {
            return new UriTemplate(TRANSFER).createURI(accountNo, transferId);
        }

        public static String DELETE_TRANSFER(String accountNo, String transferId) {
            return new UriTemplate(TRANSFER).createURI(accountNo, transferId);
        }

        public static String GET_TRANSFER_STATUS(String accountNo, String refId) {
            return new UriTemplate(TRANSFER_STATUS).createURI(accountNo, refId);
        }

        public static String LIST_LOANS() {
            return new UriTemplate(LOANS).createURI();
        }

        public static String GET_LOAN(String loanNumber) {
            return new UriTemplate(LOAN).createURI(loanNumber);
        }

        public static String GET_AMORTISATION(String loanNumber) {
            return new UriTemplate(AMORTISATION).createURI(loanNumber);
        }
    }

    public static class ErrorCodes {}

    public static class LogTags {}

    public static class QueryKey {
        public static final String START_DATE = "startDate";
        public static final String END_DATE = "endDate";
    }

    public static class StorageKey {
        public static final String ENVIRONMENT = "environment";
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
