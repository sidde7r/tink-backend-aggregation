package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.HeaderEnum;

public class EuroInformationConstants {

    public static final String EMPTY_RECOVERY_KEY = "";

    public enum Headers implements HeaderEnum {
        USER_AGENT("User-Agent", "AndroidVersion:6.0;Model:HTC One_M8"),
        CONTENT_TYPE("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        private final String key;
        private final String value;

        Headers(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getValue() {
            return this.value;
        }
    }

    public static class Tags {
        public static final String ACCOUNT_LIST = "accountList";
        public static final String WEB_ID = "webId";
        public static final String INVESTMENT_ACCOUNTS = "investmentAccounts";
        public static final String INVESTMENT_ACCOUNT = "investmentAccountDetails";
        public static final String PFM_ENABLED = "pfm";
        public static final String LOGIN_RESPONSE = "login_response";
    }

    public static class LoggingTags {
        public static final LogTag creditcardTransactionsTag =
                LogTag.from("euroinformation_creditcard_transactions");
        public static final LogTag creditcardLogTag =
                LogTag.from("euroinformation_creditcard_accounts");
        public static final LogTag loanAccountLogTag = LogTag.from("euroinformation_loan_accounts");
        public static final LogTag unknownAccountTypesTag =
                LogTag.from("euroinformation_unknown_accounts");
        public static final LogTag investmentLogTag =
                LogTag.from("euroinformation_investment_data");
    }

    // Some of these values are for Targo bank, so probably will be moved when using this provider
    // for other banks with same backend
    public static class RequestBodyValues {
        public static final String USER = "_cm_user";
        public static final String PASSWORD = "_cm_pwd";
        public static final String APP_VERSION = "appversion";
        public static final String TARGET = "_cible";
        public static final String WS_VERSION = "_wsversion";
        public static final String WS_VERSION_VALUE_1 = "1";
        public static final String WS_VERSION_VALUE_2 = "2";
        public static final String WS_VERSION_VALUE_7 = "7";
        public static final String MEDIA = "_media";
        public static final String MEDIA_VALUE = "AN";
        public static final String CATEGORIZE = "categorize";
        public static final String CATEGORIZE_VALUE = "1";
        public static final String WEB_ID = "webid";
        // Pfm initialization
        public static final String INIT = "init";
        public static final String ACTION = "action";

        // Investment accounts
        public static final String CURRENT_PAGE = "CurrentPage";
        public static final String MAX_ELEMENTS = "NbElemMaxByPage";
        public static final String MAX_ELEMENTS_VALUE = "25";
        public static final String SECURITY_ACCOUNT = "SecurityAccount";

        // Transactions with PFM enabled
        public static final String RECOVERY_KEY = "recoveryKey";
        public static final String MAX_ITEMS = "max_items";
        public static final String MAX_ITEMS_VALUE = String.valueOf(100);
    }

    public static class Url {
        public static final String LOGIN = "IDE.html";
        public static final String ACCOUNTS = "PRC2.html";
        public static final String INIT = "PFMINIT.html";
        public static final String TRANSACTIONS_NOT_PAGINATED = "LSTMVT2.html";
        public static final String TRANSACTIONS_PAGINATED = "PFMOPES.html";
        public static final String LOGOUT = "DCNX.html";
        public static final String INVESTMENT_ACCOUNT = "bourse/SecurityAccountOverview.aspx";
        public static final String INVESTMENT_ACCOUNTS = "bourse/SecurityAccountList.aspx";
    }
}
