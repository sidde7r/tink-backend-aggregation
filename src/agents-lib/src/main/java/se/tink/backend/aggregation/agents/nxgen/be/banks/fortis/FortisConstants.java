package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

public class FortisConstants {
    
    public static class URLS {
        public static final String HOST = "https://app.easybanking.bnpparibasfortis.be";
        public static final String GET_DISTRIBUTOR_AUTHENTICATION_MEANS = "/EBIA-pr01/rpc/means/getDistributorAuthenticationMeans";
        public static final String CREATE_AUTHENTICATION_PROCESS = "/EBIA-pr01/rpc/auth/createAuthenticationProcess";
        public static final String GENERATE_CHALLENGES = "/EBIA-pr01/rpc/auth/createAuthenticationProcess";
        public static final String GET_USER_INFO = "/TFPL-pr01/rpc/intermediatelogon/getUserinfo";
        public static final String GET_ILLEGAL_PASSWORD_LIST = "/TFPL-pr01/rpc/password/getIllegalPasswordList";
        public static final String  PREPARE_CONTRACT_UPDATE = "/TFPL-pr01/rpc/contractUpdate/prepareContractUpdate";
        public static final String  EXECUTE_CONTRACT_UPDATE = "/TFPL-pr01/rpc/contractUpdate/executeContractUpdate";
        public static final String GET_AVAILABILITY = "/TFPL-pr01/rpc/availability/getAvailability";
        public static final String GET_VIEW_ACCOUNT_LIST = "/AC52-pr01/rpc/accounts/getViewAccountList";
        public static final String  CHECK_DOCUMENT_AVAILABILITY = "/ZMPL-pr50/rpc/zoomit/V1/checkDocumentsAvailability";
        public static final String GET_E_BANKING_USERS = "/EBIA-pr01/rpc/identAuth/getEBankingUsers";
        public static final String AUTHENTICATION_URL = "/SEEA-pa01/SEEAServer";
    }

    public static class HEADERS {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String COOKIE = "Cookie";
        public static final String CSRF = "CSRF";
        public static final String AXES = "axes";
        public static final String DEVICE_FEATURES = "deviceFeatures";
        public static final String DISTRIBUTOR_ID = "distributorid";
        public static final String EURO_POLICY = "europolicy";
    }

    public static class HEADER_VALUES {
        public static final String CSRF_VALUE = "KVo1FnfyuRHJCcvR1fjG3zOLAKvry8vJeIILXv8X8ZdH53xWHFhY4wfETezv1vfrtjcxwrUxqiI7AuzBZOPi3kFvtq6a6AnEEndu5PtjVBvRn2l2hKRzAVfG8LQzFnYW";
        public static final String AXES_VALUE = "en|TAB|fb|priv|TAB|6dc2a55c53e34e3a9c31dd5332239e75|";
        public static final String DEVICE_FEATURES_VALUE = "0|1|0|0|0|1|1|0|0|1|0|0|0|1|1|1242";
        public static final String DISTRIBUTOR_ID_VALUE = "49FB001";
        public static final String EURO_POLICY_VALUE = "optin";
        public static final String CONTENT_TYPE_VALUE = "application/json";
    }

    public static class COOKIE {
        public static final String JESSIONID = "JSESSIONID";
        public static final String TS0111CC9C = "TS0111cc9c";
        public static final String EBIA = "ebia-pr01_JSESSIONID";
        public static final String GSN = "gsn";
        public static final String EBEW = "per_ebew";
    }
}
