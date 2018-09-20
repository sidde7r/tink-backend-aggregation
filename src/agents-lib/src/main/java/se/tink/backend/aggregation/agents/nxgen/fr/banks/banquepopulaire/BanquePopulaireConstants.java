package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class BanquePopulaireConstants {

    public static final String TRUE = "true";

    public static final class Authentication {
        public static final String SCOPES_IN_AUTH_HEADER =
                "Basic QlBfY3liZXJwbHVzLm1vYmlsZS5pb3NfUFJPRF8zLjIxOmYxNjVkY2E4LTUzMWMtNGFiOC04Y2U2LTg3YmFjNmYwNzM4Yg==";

        public static final String AUTHENTICATION_SUCCESS = "AUTHENTICATION_SUCCESS";
        public static final String AUTHENTICATION_LOCKED = "AUTHENTICATION_LOCKED";
        public static final String AUTHENTICATION_CANCELLED = "AUTHENTICATION_CANCELED";
        public static final String AUTHENTICATION_FAILED = "FAILED_AUTHENTICATION";
    }

    public static final class Fetcher {
        public static final String ACCOUNT_PARAMETER = "ACCOUNT_PARAMETER";
    }

    public static final class Cookies {
        public static final String NAV = "nav";
        public static final String NAV_VALUE = "true";
        public static final String RPALTBE = "rpaltbe";
        public static final String RPALTBE_VALUE = "webssov3";
        public static final String CYBERPLUS_HYBRID = "cyberplus-hybrid";
        public static final String CIM_SESSION_ID = "cim-session-id";
        public static final String CIM_XITI_ID = "cim-xiti-id";
    }

    public static final class Headers {
        public static final String IBP_WEBAPI_CALLERID_NAME = "IBPWEBAPICALLERID";

        public static final String IBP_WEBAPI_CALLERID = "917D1DD864BC8AE714C6E2F340798DC8";
        public static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=UTF-8";
        public static final String ACCEPT_LANGUAGE = "en-AT;q=1";
        public static final String USER_AGENT = "BanquePopulaire/3.21.3 (iPhone; iOS 10.3.1; Scale/2.00)";
        public static final String CACHE_NO_TRANSFORM = "no-transform";
    }

    public static final class Urls {
        public static final String COMMON_HOST_BASE_URL = "https://m.banquepopulaire.fr";

        public static final String GENERAL_CONFIG = COMMON_HOST_BASE_URL + "/mapi/1.1/general-config";
        public static final String BANK_CONFIG_PATH = "/app-config";
        public static final String INITIATE_SESSION_PATH = "/mon-profil";
        public static final String ACCOUNTS_PATH = "/comptes/1.1";
        public static final String TRANSACTIONS_PATH = "/comptes/{" + Fetcher.ACCOUNT_PARAMETER + "}/operations";
        public static final String LOGOUT_PATH = "/logout";
    }

    public static final class Query {
        public static final String APP_TYPE = "apptype";
        public static final String APP_TYPE_VALUE = "par";
        public static final String APP_VERSION = "appversion";
        public static final String APP_VERSION_VALUE = "64.4";
        public static final String BRAND = "brand";
        public static final String BRAND_VALUE = "bp";
        public static final String OS = "os";
        public static final String OS_VALUE = "ios";

        public static final String PAGE_KEY = "pageKey";
        public static final String TRANSACTION_STATUS = "statutMouvement";
        public static final String TRANSACTION_STATUS_VALUE = "000";
    }

    public static final class Form {
        public static final String SAML_RESPONSE = "SAMLResponse";
    }

    public static final class QueryParams {
        public static final String TRANSACTION_ID = "transactionID";
    }

    public static final class Storage {
        public static final String TOKENS = "AuthTokens";
        public static final String APP_CONFIGURATION = "AppConfiguration";
        public static final String BANK_ENTITY = "BankEntity";
    }

    public static final class LogTags {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("bp_unknown_account_type");
        public static final LogTag UNKNOWN_TRANSACTION_STATUS = LogTag.from("bp_unknown_transaction_status");
        public static final LogTag PAGINATION_RESPONSE = LogTag.from("bp_pagination_response");
    }

    public enum AccountType {
        CURRENT_ACCOUNT("000", AccountTypes.CHECKING),
        SAVINGS("004", AccountTypes.SAVINGS),
        UNKNOWN("", AccountTypes.OTHER);

        private final String contractTypeCode;
        private final AccountTypes tinkType;

        AccountType(String contractTypeCode, AccountTypes tinkType) {
            this.contractTypeCode = contractTypeCode;
            this.tinkType = tinkType;
        }

        public static boolean isHandled(String contractType) {
            return fromContractTypeCode(contractType) != UNKNOWN;
        }
        public static AccountType fromContractTypeCode(String contractType) {
            return Arrays.stream(AccountType.values())
                    .filter(accountType -> accountType.contractTypeCode.equalsIgnoreCase(contractType))
                    .findFirst().orElse(UNKNOWN);
        }

        public AccountTypes getTinkType() {
            return tinkType;
        }
    }

    public static final class Currency {
        public static final String DEFAULT_CURRENCY_CODE = "EUR";
        public static final Map<String, String> CURRENCY_CODE_MAPPER = ImmutableMap.of(
                "978", "EUR",
                "826", "GBP",
                "840", "USD",
                "953", "XPF",
                "756", "CHF"
        );

        public static final String toTinkCurrency(String currency) {
            return CURRENCY_CODE_MAPPER.getOrDefault(currency, DEFAULT_CURRENCY_CODE);
        }
    }

    public static final class Status {
        public static final Map<String, Boolean> TRANSACTION_STATUS_MAPPER = ImmutableMap.of(
                "001", false
        );
    }
}
