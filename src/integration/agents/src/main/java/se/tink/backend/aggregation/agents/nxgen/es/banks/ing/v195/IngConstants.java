package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

import java.time.ZoneId;

public class IngConstants {
    public static final String PROVIDER_NAME = "es-ing2-password";
    public static final String MARKET = "es";
    public static final String CURRENCY = "EUR";
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Madrid");
    private static final String URL_BASE = "https://ing.ingdirect.es";

    public static final String DATE_OF_BIRTH = "date-of-birth";

    public static final class Url {

        // Login urls
        public static final String LOGIN_REST_SESSION = URL_BASE + "/genoma_login/rest/session";
        public static final String LOGIN_AUTH_RESPONSE = URL_BASE + "/genoma_api/login/auth/response";

        // user information
        public static final String API_REST_CLIENT = URL_BASE + "/genoma_api/rest/client";

        //Logout
        public static final String API_REST_COMMUNICATION = URL_BASE + "/genoma_api/rest/communication/";
        public static final String API_REST_SESSION = URL_BASE + "/genoma_api/rest/session";

    }

    public static final class UsernameTypes {
        public static final int NIF = 0;
        public static final int NIE = 1;
        public static final int PASSPORT = 2;
    }


    public static final class Default {
        public static final String MOBILE_PHONE = "mobilePhone";
    }

    public static final class Form {
        public static final String TICKET = "ticket";
        public static final String DEVICE = "device";
    }

    public static final ImmutableMap<String, String> LOGOUT_QUERY = ImmutableMap.of(
            "sectionName", "all",
            "actionName", "logout",
            "operationName", ""
    );

    public static class Logging {

        public static final LogTag MISSING_PINPAD_POSITION = LogTag.from("IngDirect_ES_Auth_Missing_pin_position");
        public static final LogTag INVALID_PINPAD_NUMBERS = LogTag.from("IngDirect_ES_Auth_Invalid_pinpad_numbers");
        public static final LogTag NON_NUMERIC_PASSWORD = LogTag.from("IngDirect_ES_Auth_Non_numeric_password");

    }
}
