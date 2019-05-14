package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.sun.jersey.api.uri.UriTemplate;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public abstract class SbabConstants {
    public static final String INTEGRATION_NAME = "sbab";
    public static final String CURRENCY = "SEK";

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(
                            AccountTypes.SAVINGS,
                            "savings",
                            "savings_account",
                            "minor_savings_account")
                    .build();

    public enum Environment {
        @JsonAlias({"sandbox", "SANDBOX"})
        SANDBOX("developer", "sandbox"),
        @JsonAlias({"production", "PRODUCTION"})
        PRODUCTION("api", "api");

        private final String prefix;
        private final String baseUri;

        Environment(String prefix, String baseUri) {
            this.prefix = prefix;
            this.baseUri = baseUri;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getBaseUri() {
            return baseUri;
        }

        @JsonCreator
        public static Environment fromString(String baseUri) {
            return Environment.valueOf(baseUri.toUpperCase());
        }

        @Override
        public String toString() {
            return baseUri;
        }
    }

    public static final class Uris {
        private static final String BASE_URL = "https://{prefix}.sbab.se/{environment}";

        private static final String AUTH = "/auth/1.0";
        private static final String AUTHORIZE = AUTH + "/authorize";
        private static final String AUTH_STATUS = AUTH + "/status?pending_code={pendingCode}";
        private static final String TOKEN = AUTH + "/token";
        private static final String CUSTOMER = "/savings/1.0/customers";
        private static final String ACCOUNTS = "/savings/2.0/accounts";
        private static final String ACCOUNT = ACCOUNTS + "/{accountNo}";
        private static final String TRANSFERS = ACCOUNT + "/transfers";
        private static final String TRANSFER = TRANSFERS + "/{transferId}";
        private static final String TRANSFER_STATUS = TRANSFER + "/status/{referenceId}";
        private static final String LOANS = "/loan/2.0/loans";
        private static final String LOAN = LOANS + "/{loanNumber}";
        private static final String AMORTISATION = LOAN + "/amortisation";

        public static String GET_BASE_URL(Environment env) {
            return new UriTemplate(BASE_URL).createURI(env.getPrefix(), env.getBaseUri());
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

        public static String GET_CUSTOMER() {
            return new UriTemplate(CUSTOMER).createURI();
        }
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_SANDBOX_ACCESS_TOKEN =
                String.format(
                        INVALID_CONFIGURATION + " when running in Sandbox", "Sandbox Access Token");
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class LogTags {}

    public static class QueryKeys {
        public static final String START_DATE = "startDate";
        public static final String END_DATE = "endDate";
    }

    public static class StorageKeys {
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
