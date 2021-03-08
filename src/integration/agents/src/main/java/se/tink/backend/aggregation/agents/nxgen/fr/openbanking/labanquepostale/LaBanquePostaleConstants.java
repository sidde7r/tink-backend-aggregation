package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;

public final class LaBanquePostaleConstants {

    public static final String DEVICE_NAME = "Tink";
    public static final String CHANGE_BEARER = "SLEV";

    public static class Urls {
        public static final String BASE_URL = "https://api.labanquepostale.com/v1";
        public static final String OAUTH_BASE_URL = "https://oauth2.labanquepostale.com";
        public static final String OAUTH = "/authorize";
        static final String GET_TOKEN = "/token";
        public static final String FETCH_ACCOUNTS = "/accounts";
        static final String FETCH_BALANCES = "/accounts/%s/balances";
        public static final String FETCH_TRANSACTIONS = "/accounts/%s/transactions";
        static final String FETCH_IDENTITY_DATA = "/end-user-identity";
        static final String FETCH_TRUSTED_BENEFICIARIES = "/trusted-beneficiaries";
        static final String PAYMENT_INITIATION = "/payment-requests";
        static final String GET_PAYMENT = "/payment-requests/{paymentId}";
        static final String CONFIRM_PAYMENT = "/payment-requests/{paymentId}/confirmation";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String SIGNATURE = "Signature";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String PSU_DATE = "PSU-Date";
    }

    public static class HeaderValues {

        public static final String CONTENT_TYPE = "application/json";
        public static final String BASIC = "Basic ";
    }

    public static class Payload {
        public static final String EMPTY = "";
    }

    public static class PaymentTypeInformation {
        public static final String CATEGORY_PURPOSE = "CASH";
        public static final String LOCAL_INSTRUMENT = "INST";
        public static final String SERVICE_LEVEL = "SEPA";
    }

    public class IdTags {
        public static final String BANK = "bank";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String PAYMENT_NOT_FOUND = "Payment can not be found";
        public static final String MISSING_CONFIGURATION = "Bnp Paribas configuration missing";
        public static final String MISSING_TOKEN = "Cannot find token";
        public static final String TIME_OUT = "TIME_OUT";
    }

    public static class QueryValues {
        static final String SCORE = "aisp";
    }

    public static class CreditDebitIndicators {
        public static final String CREDIT = "CRDT";
        public static final String DEBIT = "DBIT";
    }

    public static class CreditorAgentConstants {
        public static final String BICFI = "BNKAFRPPXXX";
        public static final String NAME = "CreditorAgentName";
    }
}
