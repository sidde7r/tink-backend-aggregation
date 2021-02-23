package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.header.HeaderEnum;

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

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(TransactionalAccountType.CHECKING, "01")
                    .put(TransactionalAccountType.SAVINGS, "02")
                    .build();

    public static class Tags {
        private Tags() {}

        public static final String ACCOUNT_LIST = "accountList";
        public static final String WEB_ID = "webId";
        public static final String INVESTMENT_ACCOUNTS = "investmentAccounts";
        public static final String PFM_ENABLED = "pfm";
    }

    public static class Storage {
        private Storage() {}

        public static final String LOGIN_RESPONSE = "login_response";
    }

    // Some of these values are for Targo bank, so probably will be moved when using this provider
    // for other banks with same backend
    public static class RequestBodyValues {
        private RequestBodyValues() {}

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
        private Url() {}

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
