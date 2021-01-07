package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class SabadellConstants {

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

    public static final class Crypto {
        public static final String RSA_MODULUS =
                "28439240429073869445176380997167701932818260635090736843326083260339271383738240021817050501243881798752377083361384445136453394471735368886785360561395796482581081992434698373359558811210803240458598980299934401302052633969945864284110040125828803754404066691079159847934026963609689073268489019046367863391712572974308649795456260959837968975851737667663005999948625162689067074686007634339965952434532527609515110241345900755381476784270241391403204227136740459788389176362521875065537019413375905738398712759387266095327923797289746384844448015151948419393684484454600328858276863140057401141966479509689750901747";
        public static final long RSA_EXPONENT = 65537;
    }

    public static class UrlParams {
        public static final String MARKET = "market";
    }

    public static final class Urls {
        public static final String BASE_URL = "https://www.bancsabadell.mobi/bsmobil/api";

        public static final URL INITIATE_SESSION = new URL(BASE_URL + "/session");
        public static final URL FETCH_ACCOUNTS = new URL(BASE_URL + "/accounts/position");
        public static final URL FETCH_CREDIT_CARDS = new URL(BASE_URL + "/cards");
        public static final URL FETCH_ACCOUNT_TRANSACTIONS =
                new URL(BASE_URL + "/accounts/movements/grouped");
        public static final URL FETCH_CREDIT_CARD_TRANSACTIONS =
                new URL(BASE_URL + "/cards/movements/grouped");
        public static final URL FETCH_LOANS = new URL(BASE_URL + "/loans");
        public static final URL FETCH_DEPOSITS = new URL(BASE_URL + "/deposits");
        public static final URL FETCH_SERVICING_FUNDS =
                new URL(BASE_URL + "/servicingfunds/contracts");
        public static final URL FETCH_PENSION_PLANS = new URL(BASE_URL + "/pensionplans");
        public static final URL FETCH_SAVINGS = new URL(BASE_URL + "/savings");
        public static final URL FETCH_PRODUCTS = new URL(BASE_URL + "/products");
        public static final URL FETCH_STOCKS =
                new URL(BASE_URL + "/securities/markets/{" + UrlParams.MARKET + "}/stocks");
        public static final URL FETCH_MARKETS = new URL(BASE_URL + "/securities/markets");
        public static final URL FETCH_SERVICING_FUNDS_ACCOUNT_DETAILS =
                new URL(BASE_URL + "/servicingfunds/refunds");
        public static final URL FETCH_SAVINGS_PLAN_DETAILS = new URL(BASE_URL + "/savings/detail");
        public static final URL FETCH_LOAN_DETAILS = new URL(BASE_URL + "/loans");
    }

    public static final class Headers {
        public static final String SABADELL_ACCEPT = "application/vnd.idk.bsmobil-v1933+json";
        public static final String ACCEPT_LANGUAGE = "es";
    }

    public static final class InitiateSessionRequest {
        public static final String DEVICE_INFO =
                "en-US GEO() IOS 13.3.1 iPhone9,3 NATIVE_APP 20.1.0 STANDARD";
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
        public static final String ARXAN_BS_KEY = "arxan";
        public static final String ARXAN_DATA =
                "{csid} DebuggerOK RootKO SwizzlingOK VerifyCertificatesOK | ";
        public static final String FLOATING_KEYBOARD_KEY_PREFIX = "@0#";
        public static final String FLOATING_KEYBOARD_ENABLED = "N";
    }

    public static final class Authentication {
        public static final String TYPE_SCA = "indSca";
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
        public static final String NO_TRANSACTIONS = "CDSO602";
    }

    public static final class Tags {
        public static final String LOGIN_ERROR = "es_sabadell_login_error";
        public static final String UNKNOWN_ACCOUNT_TYPE = "es_sabadell_unknown_account_type";
        public static final String LOAN_ERROR = "es_sabadell_loan_error";
        public static final String DEPOSITS_ERROR = "es_sabadell_deposits";
        public static final String SERVICING_FUNDS_ERROR = "es_sabadell_servicing_funds";
        public static final String PENSION_PLANS_ERROR = "es_sabadell_pension_plans";
        public static final String SAVINGS_ERROR = "es_sabadell_savings";
    }

    public static final class AccountTypes {
        public static final String RELATIONSHIP_ACCOUNT = "CUENTA RELACIÓN";

        public static final String SALARY_ACCOUNT = "CUENTA EXPANSIÓN";
        public static final String SALARY_PREMIUM_ACCOUNT = "CUENTA EXPANSIÓN PREMIUM";
        public static final String UNDERAGED_ACCOUNT = "CUENTA EXPANSIÓN PRIMERA";
        public static final String MANAGED_ACCOUNT = "CUENTA GESTIONADA";
        public static final String CREDIT_CARD_CREDIT = "credit";
        public static final String CREDIT_CARD_SIN = "sin";
        public static final String CURRENCY_ACCOUNT = "CUENTA EN DIVISA";
        public static final String BUSINESS_EXPANSION_ACCOUNT = "CUENTA EXPANSIÓN NEGOCIOS";
    }

    public static final class Constants {
        public static final String NOT_AVAILABLE_ABBREVIATION = "N.D.";
    }

    public static final class Storage {
        public static final String CSID_KEY = "csid";
        public static final String SESSION_KEY = "sabadell-session-data";
    }

    public static final class QueryParamsKeys {
        public static final String CONTRACT_CODE = "contractCode";
        public static final String ENTITY_CODE = "entityCode";
        public static final String PRODUCT_TYPE = "productType";
    }
}
