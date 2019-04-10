package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec;

import se.tink.backend.aggregation.nxgen.http.URL;

public abstract class BecConstants {

    public static class Urls {
        public static final String BASE_URL = "https://api.sandbox.openbanking.bec.dk";

        public static final URL GET_ACCOUNTS = new URL(BASE_URL + ApiService.GET_ACCOUNTS);
        public static final URL GET_TRANSACTIONS = new URL(BASE_URL + ApiService.GET_TRANSACTIONS);
    }

    public static class ApiService {
        public static final String GET_ACCOUNTS = "/bg/openbanking/v1/accounts";
        public static final String GET_TRANSACTIONS =
                "/bg/openbanking/v1/accounts/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String CLIENT_ID = "clientId";
    }

    public static class QueryKeys {
        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class QueryValues {
        public static final String TRUE = "true";
        public static final String BOTH = "both";
    }

    public static class HeaderKeys {
        public static final String X_IBM_CLIENT_ID = "x-ibm-client-id";
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "bec";
    }

    public static class IdTags {

        public static final String ACCOUNT_ID = "accountId";
    }
}
