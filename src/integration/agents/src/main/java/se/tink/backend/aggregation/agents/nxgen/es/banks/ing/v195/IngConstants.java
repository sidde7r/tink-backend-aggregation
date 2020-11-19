package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.time.ZoneId;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class IngConstants {
    public static final String PROVIDER_NAME = "es-ing-password";
    public static final String MARKET = "es";
    public static final String CURRENCY = "EUR";
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Madrid");
    private static final String URL_BASE = "https://ing.ingdirect.es";

    public static final String DATE_OF_BIRTH = "date-of-birth";

    public static final class Url {

        // Login urls
        public static final String LOGIN_REST_SESSION = URL_BASE + "/genoma_login/rest/session";
        public static final String LOGIN_AUTH_RESPONSE =
                URL_BASE + "/genoma_api/login/auth/response";
        public static final String LOGIN_SCA_STATUS = URL_BASE + "/genoma_login/rest/getScaStatus";

        // user information
        public static final String API_REST_CLIENT = URL_BASE + "/genoma_api/rest/client";

        // Information about account/products customer has
        public static final String API_REST_PRODUCTS = URL_BASE + "/genoma_api/rest/products";

        // Information about transactions for a product
        public static final String API_REST_PRODUCTS_MOVEMENTS =
                URL_BASE + "/genoma_api/rest/products/{product}/movements";

        // Logout
        public static final String API_REST_COMMUNICATION =
                URL_BASE + "/genoma_api/rest/communication/";
        public static final String API_REST_SESSION = URL_BASE + "/genoma_api/rest/session";
    }

    public static class Query {
        public static final String FROM_DATE = "fromDate";
        public static final String TO_DATE = "toDate";
        public static final String LIMIT = "limit";
        public static final String OFFSET = "offset";
        public static final String SEC_PROCESS_ID = "secProcessId";
        public static final String IS_LOGIN = "isLogin";
        public static final String TRUE = "true";
        public static final String FALSE = "false";
    }

    public static final class UsernameTypes {
        public static final int NIF = 0;
        public static final int NIE = 1;
        public static final int PASSPORT = 2;
    }

    public static final class AccountTypes {
        public static final Integer CREDIT_CARD = 3;
        public static final Integer TRANSACTION_ACCOUNT = 17;
        public static final Integer SAVINGS_ACCOUNT_20 = 20;
        public static final Integer SAVINGS_ACCOUNT_26 = 26; // From old agent
        public static final Integer ZERO_TAX_ACCOUNT = 27; // From Kibana

        public static final Integer INVESTMENT_FUND = 40;
        public static final Integer PENSION_PLAN = 41;

        public static final Integer MORTGAGE_ACCOUNT = 70;
        public static final Integer LOAN_ACCOUNT = 77;
    }

    public static final class AccountCategories {
        public static final ImmutableList<Integer> TRANSACTION_ACCOUNTS =
                ImmutableList.of(AccountTypes.TRANSACTION_ACCOUNT);

        public static final ImmutableList<Integer> SAVINGS_ACCOUNTS =
                ImmutableList.of(
                        AccountTypes.SAVINGS_ACCOUNT_20,
                        AccountTypes.SAVINGS_ACCOUNT_26,
                        AccountTypes.ZERO_TAX_ACCOUNT);
        public static final ImmutableList<Integer> INVESTMENT =
                ImmutableList.of(AccountTypes.INVESTMENT_FUND, AccountTypes.PENSION_PLAN);
        public static final ImmutableList<Integer> LOANS =
                ImmutableList.of(AccountTypes.MORTGAGE_ACCOUNT, AccountTypes.LOAN_ACCOUNT);
    }

    public static final class AccountStatus {
        public static final String ACTIVE = "0"; // Activa
        public static final String OPERATIVE = "5"; // Operative
        public static final String SINGLE_CONTRACT = "7"; // Solo Contrato
    }

    public static final class Default {
        public static final String MOBILE_PHONE = "mobilePhone";
    }

    public static final class Form {
        public static final String TICKET = "ticket";
        public static final String DEVICE = "device";
    }

    public static final ImmutableMap<String, String> LOGOUT_QUERY =
            ImmutableMap.of(
                    "sectionName", "all",
                    "actionName", "logout",
                    "operationName", "");

    public static final class FetchControl {
        // mobile app uses a page size of 30
        public static final int PAGE_SIZE = 30;
    }

    public static class Logging {

        public static final LogTag NON_NUMERIC_PASSWORD =
                LogTag.from("IngDirect_ES_Auth_Non_numeric_password");
    }

    public static class Storage {
        public static final String DEVICE_ID = "deviceId";
        public static final String CREDENTIALS_TOKEN = "credentialsToken";
        public static final String LOGIN_PROCESS_ID = "loginProcessId";
    }

    public static class ErrorCodes {
        public static final String LOGIN_DOCUMENT_FIELD = "loginDocument";
        public static final String BIRTHDAY_FIELD = "birthday";
        public static final String MOBILE_VALIDATION_ENROLLMENT_REQUIRED = "19902";
        public static final String INVALID_PIN = "403001";
        public static final String GENERIC_LOCK = "403002";
    }

    public static class ScaConfig {
        public static final int CONFIRM = 0;
        public static final int TIME_OUT_ERROR = 11187;
        public static final int GENERIC_ERROR = 10013;
        public static final int NEXT_STEP = 11141;
        public static final int NOT_CORRECT_CODE_ERROR = 11188;
    }

    public static class ScaStatus {
        public static final int IN_PROGRESS = 1;
        public static final int APPROVED = 2;
    }

    public static class ScaMethod {
        public static final int SMS = 5;
        public static final int PUSH = 12;
    }
}
