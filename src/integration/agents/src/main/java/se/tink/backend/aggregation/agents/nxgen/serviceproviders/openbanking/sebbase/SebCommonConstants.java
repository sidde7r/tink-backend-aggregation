package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase;

import java.net.InetAddress;
import java.time.ZoneId;

public class SebCommonConstants {

    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Stockholm");
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DUMMY_IP = "0.0.0.0";

    public static String getPsuIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return SebCommonConstants.DUMMY_IP;
        }
    }

    public static class Urls {
        public static final String BASE_URL = "https://tpp-api.sebgroup.com";
        private static final String BASE_AUTH = "/mga/sps/oauth";

        public static final String OAUTH = BASE_AUTH + "/oauth20/authorize";
        public static final String TOKEN = BASE_AUTH + "/oauth20/token";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String GRANT_TYPE = "grant_type";
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE_TOKEN = "code";
        public static final String SCOPE = "psd2_accounts psd2_payments";
        public static final String GRANT_TYPE = "authorization_code";
        public static final String BOOKED_TRANSACTIONS = "booked";
        public static final String WITH_BALANCE = "true";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class StorageKeys {
        public static final String BASE_URL = "BASE_URL";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String CLIENT_SECRET = "CLIENT_SECRET";
        public static final String REDIRECT_URI = "REDIRECT_URI";
        public static final String TOKEN = "TOKEN";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class Accounts {
        public static final String AVAILABLE_BALANCE = "interimAvailable";
        public static final String STATUS_ENABLED = "enabled";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class ACCOUNT_TYPES {
        public static final String SAVINGS = "sparkonto";
    }
}
