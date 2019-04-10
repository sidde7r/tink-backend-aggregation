package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest;

import java.util.Locale;
import java.util.TimeZone;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SparebankenVestConstants {
    public static final class Urls {
        static final String HOST = "https://m.spv.no";
        static final String HOST_SECURITY = "https://security.spv.no";

        static final String LOAN_TYPE_PARAM = "LOAN_TYPE_PARAM";
        static final String LOAN_NUMBER_GUID_PARAM = "LOAN_NUMBER_GUID_PARAM";

        static final URL LOGIN = new URL(HOST + "/mobil/innlogget/");
        static final URL AUTHENTICATE =
                new URL(
                        HOST_SECURITY
                                + "/Innlogging/privat-mobil/LoginServiceSAMobile2/Lsam/Authenticated");
        static final URL STS_PRIVATE_WEB = new URL(HOST_SECURITY + "/STS/privat-web/Default.aspx");
        static final URL ACCOUNTS = new URL(HOST + "/konto/api/konto/konti/");
        static final URL TRANSACTIONS = new URL(HOST + "/konto/api/DepositMovement/find/");
        static final URL CREDIT_CARD_TRANSACTIONS =
                new URL(HOST + "/konto/api/transactionsearch/search/");
        static final URL DUE_PAYMENTS = new URL(HOST + "/betaling/api/Payment/findDuePayments/");
        static final URL LOANS = new URL(HOST + "/laan/api/LoanList");
        static final URL LOAN_DETAILS =
                new URL(
                        HOST
                                + "/laan/api/loanlist/{"
                                + LOAN_TYPE_PARAM
                                + "}/{"
                                + LOAN_NUMBER_GUID_PARAM
                                + "}");
        static final URL CURRENCY_LOAN_DETAILS =
                new URL(HOST + "/laan/api/currencyloan/{" + LOAN_NUMBER_GUID_PARAM + "}");

        static final URL CREDIT_CARD_ACCOUNTS = new URL(HOST + "/kort/api/kreditt/");
        static final URL INVESTMENTS = new URL(HOST + "/verdipapir/api/FondPlasseringer");
        static final URL KEEP_ALIVE = new URL(HOST + "/kunde/api/loggedinstatus");
    }

    public static final class QueryParams {
        static final String NO_CACHE_KEY = "no-cache";
        static final String NO_CACHE_VALUE = "true";
        static final String SO_KEY = "so";
        static final String IS_NEW_ACTIVATION_KEY = "isNewActivation";
        static final String IS_NEW_ACTIVATION_VALUE = "true";
        static final String HARDWARE_ID_KEY = "hwid";
        static final String ACCOUNT_NUMBER_KEY = "AccountNumber";
        static final String PREVENT_CACHE_KEY = "preventCache";
        static final String ACCOUNTS_KEY = "accounts";
        static final String STEP_KEY = "step";
        static final String START_KEY = "start";
        static final String CARD_NUMBER_GUID_KEY = "CardnumberGuids";
        static final String KID_GUID_KEY = "kidGuid";
        static final String FROM_DATE_KEY = "fromDate";
        static final String TO_DATE_KEY = "toDate";
        public static final ThreadSafeDateFormat DATE_FORMATTER =
                new ThreadSafeDateFormat.ThreadSafeDateFormatBuilder(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                new Locale("sv", "SE"),
                                TimeZone.getTimeZone("UTC"))
                        .build();
    }

    public static class Investments {
        public static String FUND_TYPE = "Fond";
        public static String PENSION_PORTFOLIO_TYPE = "Pensjonsfond";
    }

    public static class Transactions {
        public static String AGREEMENT_SUSPENDED = "AGREEMENT_SUSPENDED";
    }

    public static final class Headers {
        static final String USER_AGENT =
                "Tink Mobile/2.5.28 (iOS; 10.2, iPhone) SpvApp/3.2.4 Cordova/4.5.3 SpvID/5146244D-D272-4584-BA30-B4F3755276EE";
        static final String MOBILE_NAME_COOKIE_KEY = "mobileName";
        static final String MOBILE_NAME_COOKIE_VALUE = "Tink";
        static final String ORIGIN_KEY = "Origin";
        static final String RANGE_KEY = "Range";
        public static final String RANGE_ITEMS = "items=";
        public static final String RANGE_DASH = "-";
        static final String XCSRF_TOKEN = "X-CsrfToken";
    }

    public static final class Cookies {
        static final String CSRFTOKEN = "csrftoken";
    }

    public static final class AccountTypes {
        public static final String UNSPECIFIED = "uspesifisert";
        public static final String TAX_DEDUCTION = "skattetrekkskonto";
        public static final String DEPOSITUM = "depositum";
    }

    public static final class PagePagination {
        static final int START_INDEX = 0;
        public static final int MAX_TRANSACTIONS_IN_BATCH = 50;
    }

    public static final class SecurityParameters {
        public static final String NAME = "name";
        public static final String WA = "wa";
        public static final String WRESULT = "wresult";
        public static final String WCTX = "wctx";
    }

    public static class LogTags {
        public static final LogTag UPCOMING_TRANSACTIONS =
                LogTag.from("#SparebankenVest_upcoming_transactions");
        public static final LogTag LOANS = LogTag.from("#SparebankenVest_loans");
        public static final LogTag INVESTMENTS = LogTag.from("#SparebankenVest_investments");
    }
}
