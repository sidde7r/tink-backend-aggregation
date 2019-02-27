package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

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
        public static final String LOGIN_AUTH_RESPONSE = URL_BASE + "/genoma_api/login/auth/response";

        // user information
        public static final String API_REST_CLIENT = URL_BASE + "/genoma_api/rest/client";

        // Information about account/products customer has
        public static final String API_REST_PRODUCTS = URL_BASE + "/genoma_api/rest/products";

        // Information about transactions for a product
        public static final String API_REST_PRODUCTS_MOVEMENTS = URL_BASE + "/genoma_api/rest/products/{product}/movements";

        //Logout
        public static final String API_REST_COMMUNICATION = URL_BASE + "/genoma_api/rest/communication/";
        public static final String API_REST_SESSION = URL_BASE + "/genoma_api/rest/session";

    }

    public static class Query {
        public static final String FROM_DATE = "fromDate";
        public static final String TO_DATE = "toDate";
        public static final String LIMIT = "limit";
        public static final String OFFSET = "offset";
    }

    public static final class UsernameTypes {
        public static final int NIF = 0;
        public static final int NIE = 1;
        public static final int PASSPORT = 2;
    }

    public static final class AccountTypes {
        public static final Integer DEBIT_CARD = 1;
        public static final Integer CREDIT_CARD = 3;
        public static final Integer TRANSACTION_ACCOUNT = 17;
        public static final Integer SAVINGS_ACCOUNT_20 = 20;
        public static final Integer SAVINGS_ACCOUNT_26 = 26;    // From old agent
        public static final Integer ZERO_TAX_ACCOUNT = 27;      // From Kibana

        public static final Integer INVESTMENT_FUND = 40;
        public static final Integer PENSION_PLAN = 41;

        public static final Integer MORTGAGE_ACCOUNT = 70;
        public static final Integer LOAN_ACCOUNT = 77;

        public static final Integer LIFE_INSURANCE = 100;
    }

    public static final class AccountCategories {
        public static final ImmutableList<Integer> TRANSACTION_ACCOUNTS = ImmutableList.of(
                AccountTypes.TRANSACTION_ACCOUNT
        );

        public static final ImmutableList<Integer> SAVINGS_ACCOUNTS = ImmutableList.of(
                AccountTypes.SAVINGS_ACCOUNT_20,
                AccountTypes.SAVINGS_ACCOUNT_26,
                AccountTypes.ZERO_TAX_ACCOUNT
        );
        public static final ImmutableList<Integer> CREDIT_CARDS = ImmutableList.of(
                AccountTypes.CREDIT_CARD
        );
        public static final ImmutableList<Integer> INVESTMENT = ImmutableList.of(
                AccountTypes.INVESTMENT_FUND, AccountTypes.PENSION_PLAN
        );
        public static final ImmutableList<Integer> LOANS = ImmutableList.of(
                AccountTypes.MORTGAGE_ACCOUNT,
                AccountTypes.LOAN_ACCOUNT
        );

        public static final ImmutableList<Integer> IGNORED_ACCOUNT_TYPES = ImmutableList.of(
                AccountTypes.DEBIT_CARD,
                AccountTypes.LIFE_INSURANCE
        );

        public static final ImmutableList<Integer> ALL_KNOWN_ACCOUNT_TYPES =
                ImmutableList.copyOf(
                        ImmutableList.of(TRANSACTION_ACCOUNTS, CREDIT_CARDS, INVESTMENT, LOANS, IGNORED_ACCOUNT_TYPES)
                                .stream()
                                .flatMap(List::stream)
                                .collect(Collectors.toList())
                );
    }

    public static final class AccountStatus {
        public static final String ACTIVE = "0"; //Activa
        public static final String OPERATIVE = "5"; //Operative
        public static final String CANCELLED = "8"; //Cancelada
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

    public static final class FetchControl {
        // mobile app uses a page size of 30
        public static final int PAGE_SIZE = 30;
    }

    public static class Logging {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("IngDirect_unknown_account_type");

        public static final LogTag MISSING_PINPAD_POSITION = LogTag.from("IngDirect_ES_Auth_Missing_pin_position");
        public static final LogTag INVALID_PINPAD_NUMBERS = LogTag.from("IngDirect_ES_Auth_Invalid_pinpad_numbers");
        public static final LogTag NON_NUMERIC_PASSWORD = LogTag.from("IngDirect_ES_Auth_Non_numeric_password");

    }
}
