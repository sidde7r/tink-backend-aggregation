package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest;

import java.util.Locale;
import java.util.TimeZone;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SparebankenVestConstants {
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "Brukskonto")
                    .put(AccountTypes.SAVINGS, "bsu")
                    .put(AccountTypes.OTHER, "Uspesifisert")
                    .build();

    public static final class Urls {
        static final String HOST = "https://www.spv.no";
        static final String HOST_DBANK = "https://www2.spv.no";
        static final String HOST_M_SPV = "https://m.spv.no";
        static final String HOST_SECURITY = "https://security.spv.no";

        static final String LOAN_TYPE_PARAM = "LOAN_TYPE_PARAM";
        static final String LOAN_NUMBER_GUID_PARAM = "LOAN_NUMBER_GUID_PARAM";

        static final URL INIT = new URL(HOST + "/");
        static final URL DBANK = new URL(HOST_DBANK + "/dbank/oversikt/");
        static final URL LOGIN_INLOGGET = new URL(HOST_M_SPV + "/mobil/innlogget/");
        static final URL LOGIN_INLOGGET_KORT = new URL(HOST_M_SPV + "/mobil/innlogget/kort/");
        static final URL AUTHENTICATE =
                new URL(
                        HOST_SECURITY
                                + "/Innlogging/privat-mobil/LoginServiceSAMobile2/Lsam/Authenticated");
        static final URL LOGIN_NEW =
                new URL(
                        HOST_SECURITY
                                + "/Innlogging/privat-mobil/LoginServiceSAMobile2/NewActivation.aspx");
        static final URL CUSTOMER_ID_ACT =
                new URL(
                        HOST_SECURITY
                                + "/Innlogging/privat-mobil/LoginServiceSAMobile2/GetCustomerIdAct.aspx");
        static final URL GET_ACTIVATION_CODE =
                new URL(
                        HOST_SECURITY
                                + "/Innlogging/privat-mobil/LoginServiceSAMobile2/GetActivationCode.aspx");
        static final URL GET_MOBILE_NAME =
                new URL(
                        HOST_SECURITY
                                + "/Innlogging/privat-mobil/LoginServiceSAMobile2/GetMobileName.aspx");
        static final URL CHOOSE_PIN =
                new URL(
                        HOST_SECURITY
                                + "/Innlogging/privat-mobil/LoginServiceSAMobile2/ChoosePIN.aspx");
        static final URL STS_PRIVATE_WEB = new URL(HOST_SECURITY + "/STS/privat-web/Default.aspx");
        static final URL ACCOUNTS = new URL(HOST_DBANK + "/dbank/api/oversikt/konti");
        static final URL TRANSACTIONS = new URL(HOST_DBANK + "/dbank/api/historikk/transaksjoner");
        static final URL CREDIT_CARD_TRANSACTIONS =
                new URL(HOST + "/konto/api/transactionsearch/search/");
        static final URL DUE_PAYMENTS =
                new URL(HOST_M_SPV + "/betaling/api/Payment/findDuePayments/");
        static final URL LOANS = new URL(HOST_M_SPV + "/laan/api/LoanList");
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

        static final URL CREDIT_CARD_ACCOUNTS = new URL(HOST_M_SPV + "/kort/api/kreditt/");
        static final URL INVESTMENTS = new URL(HOST_M_SPV + "/verdipapir/api/FondPlasseringer");
        static final URL KEEP_ALIVE = new URL(HOST + "/kunde/api/loggedinstatus");
    }

    public static final class QueryParams {
        static final String SO_KEY = "so";
        static final String IS_NEW_ACTIVATION_KEY = "isNewActivation";
        static final String IS_NEW_ACTIVATION_VALUE = "true";
        static final String HARDWARE_ID_KEY = "hwid";
        static final String PREVENT_CACHE_KEY = "preventCache";
        static final String CARD_NUMBER_GUID_KEY = "CardnumberGuids";
        static final String KID_GUID_KEY = "kidGuid";
        static final String FROM_DATE_KEY = "fromDate";
        static final String TO_DATE_KEY = "toDate";
        public static final ThreadSafeDateFormat DATE_FORMATTER =
                new ThreadSafeDateFormat.ThreadSafeDateFormatBuilder(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                new Locale("sv", "SE"),
                                TimeZone.getTimeZone(Timezone.UTC))
                        .build();

        public static final String WA = "wa";
        public static final String WA_VALUE = "wsignin1.0";
        public static final String WTREALM = "wtrealm";
        public static final String WTREALM_VALUE = "https://m.spv.no/mobil/innlogget/";
        public static final String WCTX = "wctx";
        public static final String WCTX_VALUE = "rm=0&id=passive&ru=%2fmobil%2finnlogget%2fkort%2f";
        public static final String WCT = "wct";
    }

    public static final class InnloggetForm {
        public static final String WA = "wa";
        public static final String WA_VALUE = "wsignin1.0";
        public static final String W_RESULT = "wresult";
        public static final String W_CTX = "wctx";
        public static final String W_CTX_VALUE =
                "rm=0&id=passive&ru=%2fmobil%2finnlogget%2fkort%2f";
    }

    public static class Investments {
        public static String FUND_TYPE = "Fond";
        public static String PENSION_PORTFOLIO_TYPE = "Pensjonsfond";
    }

    public static class Transactions {
        public static String AGREEMENT_SUSPENDED = "AGREEMENT_SUSPENDED";
    }

    public static final class Headers {
        public static final String REFERER = "Referer";
        public static final String REFERER_VALUE =
                "https://security.spv.no/Innlogging/privat-mobil/LoginServiceSAMobile2/ChoosePIN.aspx";
        static final String USER_AGENT =
                "Mozilla/5.0 (iPhone; CPU iPhone OS 12_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 Device/iPhone9,3 SpvApp/4.2.0 Cordova/4.5.3 SpvID/193AAC18-76DB-4ACA-BDA1-88F95167CA96";
        static final String MOBILE_NAME_COOKIE_KEY = "mobileName";
        static final String MOBILE_NAME_COOKIE_VALUE = "Tink";
        static final String ORIGIN_KEY = "Origin";
        static final String CONTENT_TYPE = "Content-Type";
        static final String RANGE_KEY = "Range";
        static final String XCSRF_TOKEN = "X-CsrfToken";

        public static final String TEXT_HTML_APPLICATION_XHTML_XML =
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        static final String ACCEPT = "Accept";

        static final String APPLICATION_JSON = "application/json";
        static final String APPLICATION_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    }

    public static final class Cookies {
        static final String CSRFTOKEN = "csrftoken";
    }

    public static final class PagePagination {
        public static final int MAX_TRANSACTIONS_IN_BATCH = 50;
        public static final int CONSECUTIVE_EMPTY_PAGES_LIMIT = 3;
    }

    public static final class SecurityParameters {
        public static final String NAME = "name";
        public static final String WA = "wa";
        public static final String WRESULT = "wresult";
        public static final String WCTX = "wctx";
    }

    public static class LogTags {
        public static final LogTag LOANS = LogTag.from("#SparebankenVest_loans");
    }

    public static class HttpElements {
        public static final String VALUE = "value";
    }

    public static class Storage {
        public static final String DEVICE_TOKEN = "DeviceToken";
        public static final String HARDWARE_ID = "HardwareId";
    }

    public static class Timezone {
        public static final String UTC = "UTC";
    }
}
