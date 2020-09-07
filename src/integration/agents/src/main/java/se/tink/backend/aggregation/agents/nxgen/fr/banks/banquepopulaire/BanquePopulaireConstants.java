package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class BanquePopulaireConstants {

    public static final String APP_VER = "78.5";
    public static final String OS_VER = "13.3.1";

    public static final class Fetcher {
        public static final Pattern CARD_TRANSACTION_DESCRIPTION_PATTERN =
                Pattern.compile("[0-9]{6,8}.*\\*{2,6}[0-9]{4}");
    }

    public static final class Headers {
        public static final String IBP_WEBAPI_CALLERID_NAME = "IBPWEBAPICALLERID";

        public static final String IBP_WEBAPI_CALLERID = "917D1DD864BC8AE714C6E2F340798DC8";
        public static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=UTF-8";
        public static final String ACCEPT_LANGUAGE = "en-FR;q=1";
        public static final String USER_AGENT =
                "BanquePopulaire/3.35.0 (iPhone; iOS 13.3.1; Scale/2.00)";
        public static final String CACHE_NO_TRANSFORM = "no-transform";
    }

    public static final class Urls {
        static final String COMMON_HOST_SERVER_URL = "https://m.banquepopulaire.fr";
        public static final String COMMON_HOST_BASE_URL = COMMON_HOST_SERVER_URL + "/mapi/1.1";
        public static final String CHECK_UPDATE_PATH = "/update";
        public static final String GENERAL_CONFIG_PATH = "/general-config";
        public static final String BANK_CONFIG_PATH = "/app-config";
        public static final String MESSAGES_SERVICE_PATH = "/messages-service";
        public static final String INITIATE_SESSION_PATH = "/mon-profil";
        public static final String ACCOUNTS_PATH = "/comptes/1.1";
        public static final String TRANSACTIONS_PATH_TEMPLATE = "/comptes/%s/operations";
    }

    public static final class LogTags {
        public static final LogTag UNKNOWN_TRANSACTION_STATUS =
                LogTag.from("bp_unknown_transaction_status");
    }

    public static final class Account {
        static final AccountTypes DEFAULT_ACCOUNT_TYPE = AccountTypes.CHECKING;
        public static final Map<String, AccountTypes> ACCOUNT_TYPE_MAPPER =
                ImmutableMap.of(
                        "007", AccountTypes.CREDIT_CARD,
                        "000", AccountTypes.CHECKING,
                        "004", AccountTypes.SAVINGS,
                        "058", AccountTypes.LOAN);

        public static AccountTypes toTinkAccountType(String contractCode) {
            return ACCOUNT_TYPE_MAPPER.getOrDefault(contractCode, DEFAULT_ACCOUNT_TYPE);
        }
    }

    public static final class Currency {
        static final String DEFAULT_CURRENCY_CODE = "EUR";
        static final Map<String, String> CURRENCY_CODE_MAPPER =
                ImmutableMap.of(
                        "978", "EUR",
                        "826", "GBP",
                        "840", "USD",
                        "953", "XPF",
                        "756", "CHF");

        public static String toTinkCurrency(String currency) {
            return CURRENCY_CODE_MAPPER.getOrDefault(currency, DEFAULT_CURRENCY_CODE);
        }
    }

    public static final class Status {
        public static final Map<String, Boolean> TRANSACTION_STATUS_MAPPER =
                ImmutableMap.of("001", false);
    }
}
