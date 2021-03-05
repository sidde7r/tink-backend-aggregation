package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class FintechblocksConstants {

    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "CurrentAccount")
                    .put(TransactionalAccountType.SAVINGS, "Savings")
                    .build();

    private FintechblocksConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_REFRESH_TOKEN = "Refresh token is missing.";
    }

    public static class ApiService {
        public static final String TOKEN = "/auth/realms/ftb-sandbox/protocol/openid-connect/token";
        public static final String CREATE_CONSENT =
                "/account-info-1.0/open-banking/v3.1/aisp/account-access-consents";
        public static final String AUTH = "/auth/realms/ftb-sandbox/protocol/openid-connect/auth";
        public static final String GET_ACCOUNTS =
                "/account-info-1.0/open-banking/v3.1/aisp/accounts";
        public static final String GET_BALANCES =
                "/account-info-1.0/open-banking/v3.1/aisp/accounts/{accountId}/balances";
        public static final String GET_TRANSACTIONS =
                "/account-info-1.0/open-banking/v3.1/aisp/accounts/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CONSENT_ID = "consent-id";
    }

    public static class BalanceTypes {
        public static final String CLOSING_AVAILABLE = "ClosingAvailable";
        public static final String FORWARD_AVAILABLE = "ForwardAvailable";
        public static final String INTERIM_AVAILABLE = "InterimAvailable";
    }

    public static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String REQUEST = "request";
        public static final String PAGE = "pg";
    }

    public static class QueryValues {
        public static final String CODE = "code";
        public static final String ACCOUNTS = "accounts";
    }

    public static class HeaderKeys {
        public static final String X_JWS_SIGNATURE = "x-jws-signature";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String SCOPE = "scope";
        public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";
        public static final String CLIENT_ASSERTION = "client_assertion";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String CLIENT_ASSERTION_TYPE =
                "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String ACCOUNTS = "accounts";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class TransactionsStatuses {
        public static final String BOOKED = "Booked";
    }

    public static class JWTHeaderKeys {
        public static final String B_64 = "b64";
        public static final String IAT = "http://openbanking.org.uk/iat";
        public static final String ISS = "http://openbanking.org.uk/iss";
    }

    public static class JWTHeaderValues {
        public static final String ALG = "RS256";
        public static final Boolean B_64 = false;
        public static final String ISS = "C=UK, ST=England, L=London, O=Acme Ltd.";
        public static final String KID = "1111";
        public static final String TYPE = "JWT";
    }

    public static class JWTPayloadValues {
        public static final String PERMISSIONS =
                "ReadAccountsBasic,"
                        + "ReadAccountsDetail,"
                        + "ReadBalances,"
                        + "ReadBeneficiariesDetail,"
                        + "ReadDirectDebits,"
                        + "ReadProducts,"
                        + "ReadScheduledPaymentsDetail,"
                        + "ReadStandingOrdersDetail,"
                        + "ReadStatementsDetail,"
                        + "ReadTransactionsDetail,"
                        + "ReadTransactionsCredits,"
                        + "ReadTransactionsDebits";
        public static final String EXPIRATION_DATE_TIME = "2030-08-27T18:08:14.922Z";
        public static final String TRANSACTION_FROM_DATE_TIME = "2012-08-27T18:08:14.922Z";
        public static final String TRANSACTION_TO_DATE_TIME = "2022-08-27T18:08:14.922Z";
    }
}
