package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BoursoramaConstants {
    public static final class Urls {
        static final String HOST = "https://api.boursorama.com/";
        static final String API_VERSION = "services/api/v1.7/";
        static final URL NUMPAD =
                new URL(HOST + API_VERSION + "_public_/session/auth/generatematrix");
        public static final URL LOGIN = new URL(HOST + API_VERSION + "_public_/session/auth/login");
    }

    public static final class UserUrls {
        static final String HOST = "https://api.boursorama.com/";
        static final String API_VERSION = "services/api/v1.7/";

        // %s is the user hash, received from response.
        static final String LIST_ACCOUNTS =
                HOST + API_VERSION + "_user_/_%s_/bank/accounts/summary";
        public static final String IDENTITY_DATA =
                HOST + API_VERSION + "_user_/_%s_/customer/profile/basic";
        // First %s is the user hash, second one is the account key.
        static final String LIST_TRANSACTIONS_FROM_ACCOUNT =
                HOST + API_VERSION + "_user_/_%s_/bank/account/operations/";
        public static final String LOGOUT = HOST + API_VERSION + "_user_/_%s_/session/auth/logout";
        public static final String KEEP_ALIVE =
                HOST + API_VERSION + "_user_/_%s_/session/auth/refresh";
    }

    public static final class Errors {
        static final int INVALID_USERNAME_OR_PASSWORD = 23;
    }

    public static final class AccountFlags {
        public static final String EXTERNAL_ACCOUNT_FLAG = "EXTERNAL_ACCOUNT";
    }

    public static final class AccountCategories {
        public static final String CHECKING_ACCOUNT_ID = "BANK";
        public static final String SAVINGS_ACCOUNT_ID = "SAVINGS";
    }

    public static final class Storage {
        public static final String USER_HASH = "userHash";
        public static final String ACCOUNT_KEY = "accountKey";
        public static final String DEVICE_ENROLMENT_TOKEN_VALUE = "deviceEnrolmentTokenValue";
        public static final String UDID = "udid";
        public static final String LOGGED_IN_BEARER_TOKEN = "loggedInBearerToken";
    }

    public static final class Transaction {
        public static final String PERFORMED_ON_LABEL = "operation_date";
        public static final String VALUED_ON_LABEL = "value_date";
        public static final String TRANSACTION_DESCRIPTION_LABEL = "operation_label";
        public static final String CONTINUATION_TOKEN_QUERY_KEY = "continuationToken";
    }

    public static final class Auth {
        // API_KEY extracted from Info.plist in the app folder on a jailbroken phone.
        static final String API_KEY =
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlJTMjU2In0.eyJqdGkiOiI2ZWRjZWE3ZWEwM2YxIiwic3ViIjoiYXBwbGljYXRpb24tZnItaW9zLXY2IiwiYXVkIjoiLipcXC5ib3Vyc29yYW1hXFwuY29tIiwiZXhwIjoxODQ2NzQ1NzQxLCJpYXQiOjE1MzEzODU3NDEsIm5iZiI6MTUzMTM4NTc0MSwic2Vzc2lvbiI6eyJsZXZlbCI6IlZJU0lUT1IifSwiaXNzIjoiQWRtaW4gSldUIEJvdXJzb3JhbWEiLCJvcmciOiJJUEhPTkUiLCJvYXV0aCI6ImYxOGI0ZDhiN2NhMTljNTFmY2RjMzliNDFmYTcxYzhiODhmNmRmNzgifQ.opYuHCI-_uQ5tBvWFUvYWLm2auL81hypWHN6zqe-qnM3114pZXShc8YFKmpzAGLjAcX2kHYoglEkMVZi3SyADbtRp4gg8U6UbS1TCNUowKXL1G7EFQD8qRdGoP4MRIP3dDcFP7-nQvaDQHE4B5xhrX0vPkyMp-Nw-4sRrfe5D_RMOBI2YAaSeNfqkIMADa0EWvKbYA0W1bWnarov-FCH6feJqHK05DKYxP9yjKs3YgJQUp1wgcObtykIdCNQF1q92CsfauST8H7rRwsIcMRKKQ0TwmolAh92rntHn1R56vZw84f0IZ2EC_dnhhkI1zs9LJNl9VP3KMzhkIjDHv_3FQ";
        public static final String FIRST_AUTH = "regular";
        public static final String USER_HASH_HEADER = "brsuserhash";
        // Verified to be `ckln` prepended for two ambassadors.
        public static final String DEVICE_ENROLMENT_TOKEN_VALUE_PREFIX = "ckln";
    }

    public static final class Numpad {
        static final String IMAGE_SET_VERSION = "appV6";
        static final int NUMPAD_MAX_NUMBER_OF_BUTTONS = 10;
    }
}
