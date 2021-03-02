package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class EdenredConstants {

    public static final String APP_VERSION = "3.3";
    public static final String APP_TYPE = "IOS";
    public static final String CHANNEL = "MOBILE";
    public static final String CURRENCY = "EUR";

    protected static final Map<String, String> QUERY_PARAMS = new HashMap<>();

    static {
        QUERY_PARAMS.put("appVersion", APP_VERSION);
        QUERY_PARAMS.put("appType", APP_TYPE);
        QUERY_PARAMS.put("channel", CHANNEL);
    }

    public static class Headers {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String APPLICATION_JSON = "application/json";
        public static final String USER_AGENT = "User-Agent";
        public static final String USER_AGENT_VALUE = "EdenRED/2 CFNetwork/978.0.7 Darwin/18.7.0";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class Urls {
        public static final String BASE_URL = "https://prod.myedenred.pt/edenred-customer/api";
        public static final URL AUTHENTICATE_DEFAULT =
                new URL(BASE_URL + "/authenticate/default").queryParams(QUERY_PARAMS);
        public static final URL AUTHENTICATE_PIN =
                new URL(BASE_URL + "/authenticate/pin").queryParams(QUERY_PARAMS);
        public static final URL SET_PIN =
                new URL(BASE_URL + "/protected/pin").queryParams(QUERY_PARAMS);
        public static final URL CARD_LIST =
                new URL(BASE_URL + "/protected/card/list").queryParams(QUERY_PARAMS);
        public static final URL TRANSACTIONS =
                new URL(BASE_URL + "/protected/card/{id}/accountmovement")
                        .queryParams(QUERY_PARAMS);
        public static final URL DEVICE =
                new URL(BASE_URL + "/protected/device/update").queryParams(QUERY_PARAMS);
    }

    public static class Storage {
        public static final String PIN = "PIN";
        public static final String DEVICE_ID = "DEVICE_ID";
        public static final String USER_ID = "USER_ID";
        public static final String TOKEN = "TOKEN";
        public static final String TRANSACTIONS = "TRANSACTIONS";
    }

    public static class Errors {
        public static final String REJECTED_REQUEST = "Edenred Rejected Request";
    }

    public static class Cards {
        public static final String ACTIVE = "ACTIVE";
    }

    public static final String NO_PERMISSION = "10412";
}
