package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public abstract class BnpParibasFortisConstants {

    public static final String INTEGRATION_NAME = "bnpparibasfortis";

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "CACC")
                    .put(AccountTypes.SAVINGS, "SVGS")
                    .put(AccountTypes.OTHER, "OTHR")
                    .build();

    public static class Urls {
        public static final String OAUTH = "/authorize";
        public static final String TOKEN = "/token";
        public static final String ACCOUNTS = "/v1/accounts";
        public static final String PAYMENTS = "/v1/payment-requests";
        public static final String GET_PAYMENT = PAYMENTS + "/%s";
        public static final String AUTH_PAYMENT = "/payment-requests/{paymentId}/consent";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = "OAUTH_TOKEN";
        public static final String ACCOUNT_LINKS = "ACCOUNT_LINKS";
    }

    public static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE = "aisp;pisp";
    }

    public static class HeaderKeys {
        public static final String ORGANIZATION_ID = "X-Openbank-Organization";
        public static final String OPENBANK_STET_VERSION = "X-Openbank-Stet-Version";
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String SIGNATURE = "Signature";
        public static final String APPLICATION_HAL_JSON = "application/hal+json";
    }

    public static class FormValues {
        public static final String GRANT_TYPE = "authorization_code";
        public static final String SCOPE = "aisp;pisp";
    }

    public static class Accounts {
        public static final String BALANCE_TYPE_OTHER = "OTHR";
    }

    public static class Transactions {
        public static final String PENDING_STATUS = "PDNG";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class IdTags {
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class PaymentRequestValues {
        public static final String INITIATING_PARTY = "TINK";
        public static final int NUMBER_OF_TRANSACTIONS = 1;
        public static final String REMITTANCE_INFORMATION = "Text message to creditor";
        public static final String AUTHENTICATION_METHODS = "REDIRECT";
    }
}
