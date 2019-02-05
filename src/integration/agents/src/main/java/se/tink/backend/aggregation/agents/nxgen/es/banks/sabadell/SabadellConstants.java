package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class SabadellConstants {

    public static final class Authentication {
        public static final String PUBLIC_KEY_B64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4Ug848S9Hf0wg4YUbr"
                + "KKTP2m54k2yB9Nh7PhQKmdNfel9LkRea4hFis3Kdzv0KW/ZeUNA5KKj5hZO3HG7ZIz5Ee8OG8j7965MsuLkU1IPl0k+egF5U"
                + "LjcVz8OU5sZMtihUhcPf3eZEUql4I+lJ/8b9dTzjb0EjRofnjOYunLyMX72Puvdh8PqDmWk6Lh+co9y414WC/crwa9vZOxWx"
                + "3lG28UeEwRCkSxnwqs/EkQi8cTymohQ9xJAXO8jonNNoTecEUA7ZnRNGqAat7Knsn+TLV2+pEPRfpEassZkVVpSGZ+JchrRL"
                + "br2kR1wyJybVgg12VJmhE0yvy5McZEzm3T8wIDAQAB";
    }

    public static final class Urls {
        public static final String BASE_URL = "https://www.bancsabadell.mobi/bsmobil/api";

        public static final URL INITIATE_SESSION = new URL(BASE_URL + "/session");
        public static final URL FETCH_ACCOUNTS = new URL(BASE_URL + "/accounts/position");
        public static final URL FETCH_CREDIT_CARDS = new URL(BASE_URL + "/cards");
        public static final URL FETCH_ACCOUNT_TRANSACTIONS = new URL(BASE_URL + "/accounts/movements/grouped");
        public static final URL FETCH_CREDIT_CARD_TRANSACTIONS = new URL(BASE_URL + "/cards/movements/grouped");
        public static final URL FETCH_LOANS = new URL(BASE_URL + "/loans");
        public static final URL FETCH_DEPOSITS = new URL(BASE_URL + "/deposits");
        public static final URL FETCH_SERVICING_FUNDS = new URL(BASE_URL + "/servicingfunds/contracts");
        public static final URL FETCH_PENSION_PLANS = new URL(BASE_URL + "/pensionplans");
        public static final URL FETCH_SAVINGS = new URL(BASE_URL + "/savings");
    }

    public static final class Headers {
        public static final String SABADELL_ACCEPT = "application/vnd.idk.bsmobil-v1852+json";
    }

    public enum QueryParamPairs {
        NO_ERROR("noError", "true"),
        CTA_VISTA("type", "CTA_VISTA"),
        CTA_CARD_ALL("filter", "CTA_CARD_ALL"),
        PAGE("page", "1"),
        ORDER_DESC("order", "desc"),
        ORDER_0("order", "0");

        private final String key;
        private final String value;

        QueryParamPairs(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public static final class InitiateSessionRequest {
        public static final String DEVICE_INFO = "en-US GEO() IOS 10.2 iPhone8,1 NATIVE_APP 18.3.0 STANDARD";
        public static final String LAST_REQUEST_DATE = "";
        public static final String BRAND = "SAB";
        public static final String GEO_LOCATION_DATA = "";
        public static final boolean NEW_DEVICE = false;
        public static final String REQUEST_ID = "SDK";
        public static final int LOGIN_TYPE = 1;
        public static final String COMPILATION_TYPE = "release";
        public static final String CONTRACT = "";
        public static final String DEVICE_PRINT = "";
        public static final int LAST_KNOWN_BRAND = 0;
        public static final String TRUSTEER = "";
        public static final String USERNAME_BS_KEY = "userName";
        public static final String PASSWORD_BS_KEY = "password";
    }

    public static final class FetcherRequest {
        public static final String ITEMS_PER_PAGE = "itemsPerPage";
    }

    public static final class CreditCardTransactionsRequest {
        public static final int ITEMS_PER_PAGE = 100;
        public static final String ORDER_DESC = "desc";
        public static final String DATE_FROM = "";
        public static final String DATE_TO = "";
    }

    public static final class LoansRequest {
        public static final String ITEMS_PER_PAGE = "20";
    }

    public static final class PensionPlansRequest {
        public static final String ITEMS_PER_PAGE = "5";
    }

    public static final class ErrorCodes {
        public static final String INCORRECT_CREDENTIALS = "CDSO031";
        public static final String NO_PRODUCTS = "CDSO114";
    }

    public static final class Tags {
        public static final String LOGIN_ERROR = "es_sabadell_login_error";
        public static final String UNKNOWN_ACCOUNT_TYPE = "es_sabadell_unknown_account_type";
        public static final String LOAN_ERROR = "es_sabadell_loan_error";
        public static final String DEPOSITS_ERROR = "es_sabadell_deposits";
        public static final String SERVICING_FUNDS_ERROR = "es_sabadell_servicing_funds";
        public static final String PENSION_PLANS_ERROR = "es_sabadell_pension_plans";
        public static final String SAVINGS_ERROR = "es_sabadell_savings";
        public static final LogTag CREDIT_CARD_TRANSACTIONS = LogTag.from("es_sabadell_credit_card_transactions");
        public static final LogTag LOANS = LogTag.from("es_sabadell_loans");
        public static final LogTag DEPOSITS = LogTag.from("es_sabadell_deposits");
        public static final LogTag SERVICING_FUNDS = LogTag.from("es_sabadell_servicing_funds");
        public static final LogTag PENSION_PLANS = LogTag.from("es_sabadell_pension_plans");
        public static final LogTag SAVINGS = LogTag.from("es_sabadell_savings");
    }

    public static final class AccountTypes {
        public static final String CUENTA_RELACION = "CUENTA RELACIÓN";
        public static final String SALARY_ACCOUNT = "CUENTA EXPANSIÓN";
        public static final String CREDIT_CARD_CREDIT = "credit";
        public static final String CREDIT_CARD_SIN = "sin";
        public static final String CURRENCY_ACCOUNT = "CUENTA EN DIVISA";

    }
}
