package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import java.util.Arrays;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.HeaderEnum;

public final class BbvaConstants {
    public static final int PAGE_SIZE = 20;

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

    public static final class StorageKeys {
        public static final String ACCOUNT_ID = "accountId";
        public static final String HOLDER_NAME = "holderName";
        public static final String USER_ID = "userId";
        public static final String ID_TYPE_CODE = "idTypeCode";
        public static final String TSEC = "tsec";
    }

    public static final class Defaults {
        public static final String CURRENCY = "EUR";
        public static final String CHARSET = "UTF-8";
    }

    public static final class AccountType {
        public static final String CREDIT_CARD = "credit";
        public static final String CREDIT_CARD_SHORT_TYPE = "C";
    }

    public static final class QueryKeys {
        public static final String PAGINATION_OFFSET = "paginationKey";
        public static final String PAGE_SIZE = "pageSize";
        public static final String CONTRACT_ID = "contractId";
        public static final String CARD_TRANSACTION_TYPE = "cardTransactionType";
        public static final String DASHBOARD_CUSTOMER_ID = "$customer.id";
    }

    public static final class LogTags {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("bbva_unknown_account_type");
        public static final LogTag INVESTMENT_INTERNATIONAL_PORTFOLIO =
                LogTag.from("bbva_investment_international_portfolio");
        public static final LogTag PRODUCTS_FULL_RESPONSE =
                LogTag.from("bbva_products_full_response");
        public static final LogTag INVESTMENT_MANAGED_FUNDS =
                LogTag.from("bbva_investment_managed_funds");
        public static final LogTag INVESTMENT_WEALTH_DEPOSITARY =
                LogTag.from("bbva_investment_wealth_depositary");
        public static final LogTag LOAN_DETAILS = LogTag.from("bbva_loan_details");
        public static final LogTag LOAN_MULTI_MORTGAGE = LogTag.from("bbva_loan_multi_mortgage");
        public static final LogTag LOAN_REVOLVING_CREDIT =
                LogTag.from("bbva_loan_revolving_credit");
        public static final LogTag LOAN_WORKING_CAPITAL = LogTag.from("bbva_loan_working_capital");
        public static final LogTag UTILS_SPLIT_GET_PAGINATION_KEY =
                LogTag.from("bbva_utils_split_get_pagination_key");
    }

    public static final class Url {
        public static final String BASE_URL = "https://servicios.bbva.es";

        public static final String PARAM_ID = "ID";

        public static final String LOGIN = BASE_URL + "/DFAUTH/slod/DFServletXML";
        public static final String SESSION =
                BASE_URL + "/ENPP/enpp_mult_web_mobility_02/sessions/v1";
        public static final String PRODUCTS =
                BASE_URL + "/ENPP/enpp_mult_web_mobility_02/products/v2";
        public static final String FINANCIAL_DASHBOARD = BASE_URL + "/ASO/financialDashBoard/V03/";
        public static final String ACCOUNT_TRANSACTION =
                BASE_URL + "/ASO/accountTransactions/V02/accountTransactionsAdvancedSearch";
        public static final String CREDIT_CARD_TRANSACTIONS =
                BASE_URL + "/ASO/cardTransactions/V01/";
        public static final String LOAN_DETAILS = BASE_URL + "/ASO/loans/V01/{" + PARAM_ID + "}";
        public static final String CARD_TRANSACTIONS =
                BASE_URL + "/ASO/cardTransactions/V01/{" + PARAM_ID + "}";
        public static final String SECURITY_PROFITABILITY =
                BASE_URL + "/ASO/securityActions/V01/listProfitability";
        public static final String IDENTITY_DATA =
                BASE_URL + "/ASO/customers/V02/{" + PARAM_ID + "}";
    }

    public enum Headers implements HeaderEnum {
        CONSUMER_ID("ConsumerID", "00000013"),
        BBVA_USER_AGENT(
                "BBVA-User-Agent",
                "%s;iPhone;Apple;iPhone9,3;750x1334;iOS;10.1.1;WOODY;6.14.1;xhdpi"),
        ORIGIN("Origin", "https://movil.bbva.es"),
        REFERER("Referer", "https://movil.bbva.es/versions/woody/6.14.5/index.html");

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
        public static final String CONTENT_TYPE_URLENCODED_UTF8 =
                "application/x-www-form-urlencoded; charset=utf-8";
        public static final String TSEC_KEY = "tsec";;
    }

    public static final class PostParameter {
        public static final String ORIGEN_KEY = "origen";
        public static final String ORIGEN_VALUE = "enpp";
        public static final String EAI_TIPOCP_KEY = "eai_tipoCP";
        public static final String EAI_TIPOCP_VALUE = "up";
        public static final String EAI_USER_KEY = "eai_user";
        public static final String EAI_USER_VALUE_PREFIX = "0019-";
        public static final String EAI_PASSWORD_KEY = "eai_password";
        public static final String CONSUMER_ID_KEY = "consumerID";
        public static final String CONSUMER_ID_VALUE = Headers.CONSUMER_ID.value;
        public static final String SEARCH_TYPE = "SEARCH";

        public static final int START_DATE_YEAR_AGO = -30;
    }

    public static final class Message {
        public static final String OK = "ok";
        public static final String LOGIN_SUCCESS = "login successful";
        public static final String LOGIN_WRONG_CREDENTIAL_CODE = "eai0000";
    }

    public static final class IdTypeCodes {
        public static final String NIF = "nif";
        public static final String NIE = "nie";
    }
}
