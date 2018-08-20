package se.tink.backend.aggregation.agents.nxgen.es.banks.ing;

import java.time.ZoneId;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public abstract class IngConstants {

    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final String DATE_OF_BIRTH = "date-of-birth";
    public static final String ORIGINAL_ENTITY = "originalEntity";
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Madrid");
    public static final String PROVIDER_NAME = "es-ing-password";
    public static final String MARKET = "es";
    public static final String CURRENCY = "EUR";
    private static final String URL_BASE = "https://ing.ingdirect.es";

    public final class Url {
        public static final String GENOMA_LOGIN_REST_SESSION = URL_BASE + "/genoma_login/rest/session";
        public static final String GENOMA_API_REST_COMMUNICATION = URL_BASE + "/genoma_api/rest/communication/";
        public static final String GENOMA_API_REST_SESSION = URL_BASE + "/genoma_api/rest/session";
        public static final String GENOMA_API_REST_CLIENT = URL_BASE + "/genoma_api/rest/client";
        public static final String GENOMA_API_REST_PRODUCTS = URL_BASE + "/genoma_api/rest/products";
        public static final String GENOMA_API_REST_PRODUCTS_MOVEMENTS = URL_BASE + "/genoma_api/rest/products/{product}/movements";
        public static final String GENOMA_API_LOGIN_AUTH_RESPONSE = URL_BASE + "/genoma_api/login/auth/response";
    }

    public static class Default {
        public static final String MOBILE_PHONE = "mobilePhone";
        public static final String SESSION_NAME_ALL = "all";
        public static final String ACTION_NAME_LOGOUT = "logout";
        public static final String OPERATION_NAME_EMPTY = "";
    }

    public static class UsernameType {
        // These types might need to be made more granular at a later time.

        // The following formats of usernames matches (regex) NON_NIE (0):
        //  [0-9]+z
        //  [0-9]+y
        public static final int NON_NIE = 0;

        // The following formats of usernames matches (regex) NIE (1):
        //  x[0-9]+[A-Z]
        public static final int NIE = 1;
    }

    public static class ErrorCode {
        public static final String INVALID_LOGIN_DOCUMENT_TYPE = "ESValidLoginDocument.loginDocument";
    }

    public static class ProductType {
        /** Tarjeta Débito */
        public static final int DEBIT_CARD = 1;
        /** Cuenta de valores */
        public static final int VALUE_ACCOUNT = 42;
        /** Cuenta sin nomina */
        public static final int ACCOUNT_WITHOUT_PAYROLL = 17;
        /** Cuenta NARANJA */
        public static final int ORANGE_ACCOUNT = 20;
    }

    public static class Query {
        public static final String FROM_DATE = "fromDate";
        public static final String TO_DATE = "toDate";
        public static final String LIMIT = "limit";
        public static final String OFFSET = "offset";
        public static final String TICKET = "ticket";
        public static final String DEVICE = "device";
        public static final String SESSION_NAME = "sessionName";
        public static final String ACTION_NAME = "actionName";
        public static final String OPERATION_NAME = "operationName";
    }

    public final class FetchControl {
        /** max allowed is 100 */
        public static final int PAGE_SIZE = 100;
    }

    public static class Logging {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from(PROVIDER_NAME + "-unknown-account-type");
    }
}
