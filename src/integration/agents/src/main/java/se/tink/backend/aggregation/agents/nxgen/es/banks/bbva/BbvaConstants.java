package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import java.util.Arrays;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.header.HeaderEnum;

public final class BbvaConstants {

    public static final class Fetchers {
        public static final long BACKOFF = 3000;
        public static final int MAX_TRY_ATTEMPTS = 5;
        public static final int PAGE_SIZE = 40;
    }

    public enum Error {
        BANK_SERVICE_UNAVAILABLE("ENPP0000"),
        UNKNOWN("");

        private final String code;

        Error(String code) {
            this.code = code;
        }

        public static Error find(String errorCode) {
            return Arrays.stream(Error.values())
                    .filter(error -> error.code.equalsIgnoreCase(errorCode))
                    .findFirst()
                    .orElse(UNKNOWN);
        }
    }

    public static final class CredentialKeys {
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
    }

    public static final class StorageKeys {
        public static final String USER_ID = "userId";
        public static final String TSEC = "tsec";
    }

    public static final class Defaults {
        public static final String CURRENCY = "EUR";
        public static final String TIMEZONE_CET = "CET";
    }

    public static final class AccountType {
        public static final String CREDIT_CARD_SHORT_TYPE = "C";
    }

    public static final class QueryKeys {
        public static final String PAGINATION_OFFSET = "paginationKey";
        public static final String PAGE_SIZE = "pageSize";
        public static final String CONTRACT_ID = "contractId";
        public static final String CARD_TRANSACTION_TYPE = "cardTransactionType";
        public static final String DASHBOARD_CUSTOMER_ID = "$customer.id";
        public static final String DASHBOARD_FILTER = "$filter";
        public static final String ISALIVE_CUSTOMER_ID = "customerId";
    }

    public static final class QueryValues {
        public static final String FIRST_PAGE_KEY = "0";
        public static final String DASHBOARD_FILTER = "(showPending==true);(hasSicav==false)";
    }

    public static final class LogTags {
        public static final LogTag TRANSACTIONS_RETRYING =
                LogTag.from("bbva_transactions_retrying");
        public static final LogTag LOAN_DETAILS = LogTag.from("bbva_loan_details");
        public static final LogTag UTILS_SPLIT_GET_PAGINATION_KEY =
                LogTag.from("bbva_utils_split_get_pagination_key");
    }

    public static final class Url {
        public static final String BASE_URL = "https://servicios.bbva.es";

        public static final String PARAM_ID = "ID";

        public static final String TICKET = BASE_URL + "/ASO/TechArchitecture/grantingTickets/V02";
        public static final String REFRESH_TICKET =
                BASE_URL + "/ASO/grantingTicketActions/V01/refreshGrantingTicket";
        public static final String FINANCIAL_DASHBOARD = BASE_URL + "/ASO/financialDashBoard/V03";
        public static final String ASO = BASE_URL + "/ASO";
        public static final String ACCOUNT_TRANSACTION =
                BASE_URL + "/ASO/accountTransactions/V02/accountTransactionsAdvancedSearch";
        public static final String CREDIT_CARD_TRANSACTIONS =
                BASE_URL + "/ASO/cardTransactions/V01/";
        public static final String LOAN_DETAILS = BASE_URL + "/ASO/loans/V01/{" + PARAM_ID + "}";
        public static final String IDENTITY_DATA =
                BASE_URL + "/ASO/contextualData/V02/{" + PARAM_ID + "}";
        public static final String HISTORICAL_DATE =
                BASE_URL + "/ASO/contracts/v0/financial-investment/historical-date";
        public static final String FINANCIAL_INVESTMENTS =
                BASE_URL + "/ASO/contracts/v0/financial-investment/daily-summaries";
        public static final String PARTICIPANTS =
                BASE_URL + "/ASO/contracts/V01/{" + PARAM_ID + "}/participants/";
    }

    public enum Headers implements HeaderEnum {
        CONSUMER_ID("ConsumerID", LoginParameter.CONSUMER_ID),
        BBVA_USER_AGENT(
                "BBVA-User-Agent",
                "%s;iPhone;Apple;iPhone9,3;750x1334;iOS;10.1.1;WOODY;6.14.1;xhdpi"),
        REFERER("Referer", "https://movil.bbva.es/versions/woody/7.3.7/index.html");

        private final String key;
        private final String value;

        Headers(String key, String value) {
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

    public static final class HeaderKeys {

        public static final String TSEC_KEY = "tsec";
    }

    public static final class PostParameter {

        public static final String SEARCH_TYPE = "SEARCH";
        public static final String ISIN_ID_TYPE = "ISIN";
        public static final String ANY_ISIN = "000000000000";
        public static final String ANY_MARKET = "0000";
        public static final int START_DATE_YEAR_AGO = -30;
    }

    public static final class LoginParameter {
        public static final String OTP_DATA_ID = "otp";
        public static final String AUTH_DATA_ID = "password";
        public static final String AUTH_TYPE = "02";
        public static final String AUTH_OTP_TYPE = "121";
        public static final String USER_VALUE_PREFIX = "0019-";
        public static final String CONSUMER_ID = "00000013";
    }

    public static final class Messages {
        public static final String OK = "ok";
    }

    public static final class AuthenticationStates {
        public static final String OK = "OK";
        public static final String GO_ON = "GO_ON";
    }

    public static final class ErrorMessages {
        public static final String TEMPORARILY_UNAVAILABLE =
                "El servicio no est&aacute; disponible temporalmente.";
        public static final String MAX_TRY_ATTEMPTS =
                String.format("Reached max retry attempts of %d", Fetchers.MAX_TRY_ATTEMPTS);
    }

    public static final class IdTypeCodes {
        public static final String NIF = "1";
        public static final String NIE = "6";
    }

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }

    public static class Proxy {
        public static final String COUNTRY = "es";
        public static final String ES_PROXY = "esProxy";
    }

    public static class ErrorCode {
        public static final String CONTRACT_NOT_OPERABLE = "contractNotOperable";
    }

    public static class ProductTypes {
        public static final String COMPLEMENTARY = "COMPLEMENTARIAS";
    }

    public static class HolderTypes {
        public static final String OWNER = "TIT";
    }
}
