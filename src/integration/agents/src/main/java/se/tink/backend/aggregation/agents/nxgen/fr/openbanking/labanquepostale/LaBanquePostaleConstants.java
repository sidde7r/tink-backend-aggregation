package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;

public final class LaBanquePostaleConstants {

    public static class Urls {

        public static final String OAUTH = "/authorize";
        static final String GET_TOKEN = "/token";
        public static final String FETCH_ACCOUNTS = "/accounts";
        static final String FETCH_BALANCES = "/accounts/%s/balances";
        public static final String FETCH_TRANSACTIONS = "/accounts/%s/transactions";
        static final String FETCH_IDENTITY_DATA = "/end-user-identity";
        static final String FETCH_TRUSTED_BENEFICIARIES = "/trusted-beneficiaries";
        static final String PAYMENT_INITIATION = "/payment-requests";
        public static final String GET_PAYMENT = "/payment-requests/%s";
        static final String CONFIRM_PAYMENT = "/payment-requests/%s/confirmation";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
    }

    public static class HeaderKeys {
        public static final String SIGNATURE = "Signature";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String PSU_DATE = "PSU-Date";
    }

    public static class HeaderValues {

        public static final String CONTENT_TYPE = "application/json";
    }

    public static class Payload {
        public static final String EMPTY = "";
    }

    public static class PaymentTypeInformation {
        public static final String CATEGORY_PURPOSE = "DVPM";
        public static final String LOCAL_INSTRUMENT = "INST";
        public static final String SERVICE_LEVEL = "SEPA";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String PAYMENT_NOT_FOUND = "Payment can not be found";
    }

    public static class QueryValues {
        static final String SCORE = "aisp";
    }
}
