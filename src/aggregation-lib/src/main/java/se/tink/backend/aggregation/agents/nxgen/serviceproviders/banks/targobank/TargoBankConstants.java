package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank;

import se.tink.backend.aggregation.nxgen.http.HeaderEnum;

public class TargoBankConstants {

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
    }

    // Some of these values are for Targo bank, so probably will be moved when using this provider for other banks with same backend
    public static class RequestBodyValues {
        public static final String USER = "_cm_user";
        public static final String PASSWORD = "_cm_pwd";
        public static final String APP_VERSION = "appversion";
        public static final String APP_VERSION_VALUE = "4.31.0";
        public static final String TARGET = "_cible";
        public static final String TARGET_VALUE = "TARGO";
        public static final String WS_VERSION = "_wsversion";
        public static final String MEDIA = "_media";
        public static final String MEDIA_VALUE = "AN";
        public static final String CATEGORIZE = "categorize";
        public static final String CATEGORIZE_VALUE = "1";
        public static final String WEB_ID = "webid";
        // Used for INIT call that seems to not be needed
        public static final String INIT = "init";
        public static final String ACTION = "action";

        public static final String CURRENT_PAGE = "CurrentPage";
        public static final String MAX_ELEMENTS = "NbElemMaxByPage";
    }

    public static class Url {
        public static final String LOGIN = "IDE.html";
        public static final String ACCOUNTS = "PRC2.html";
        public static final String INIT = "PFMINIT.html";
        public static final String TRANSACTIONS = "LSTMVT2.html";
        public static final String LOGOUT = "DCNX.html";
        public static final String INVESTMENT_ACCOUNT = "bourse/SecurityAccountOverview.aspx";
        public static final String INVESTMENT_ACCOUNTS = "bourse/SecurityAccountList.aspx";
    }
}
