package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

public abstract class LansforsakringarConstants {

    public static class Urls {
        public static final String BASE_URL = "https://sandbox.bank.lansforsakringar.se:443";
        public static final String AUTHENTICATE = BASE_URL + "/v1/oauth2/token";
        public static final String GET_ACCOUNTS = BASE_URL + "/openbanking/ais/v1/accounts";
        public static final String GET_TRANSACTIONS =
                BASE_URL + "/openbanking/ais/v1/accounts/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String CLIENT_ID = "clientId";
        public static final String CLIENT_SECRET = "clientSecret";
        public static final String CONSENT_ID = "consentId";
        public static final String ACCESS_TOKEN = "access-token";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String WITH_BALANCE = "withBalance";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
        public static final String TRUE = "true";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String PSU_USER_AGENT = "PSU-User-Agent";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class HeaderValues {
        //TODO: We need to support these PSU headers in production.
        public static final String PSU_IP_ADDRESS = "127.0.0.1";
        public static final String PSU_USER_AGENT = "Desktop Mode";
    }

    public static class FormKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String GRANT_TYPE = "grant_type";
    }

    public static class FormValues {
        public static final String CLIENT_CREDENTIALS = "client_credentials";
    }

    public class Market {
        public static final String INTEGRATION_NAME = "lansforsakringar";
    }

    public class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }
}
