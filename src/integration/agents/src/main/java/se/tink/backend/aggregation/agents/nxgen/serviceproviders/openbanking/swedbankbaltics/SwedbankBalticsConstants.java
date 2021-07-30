package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics;

public class SwedbankBalticsConstants {

    public static final class TimeValues {
        public static final long SMART_ID_POLL_FREQUENCY = 90;
        public static final int SMART_ID_POLL_MAX_ATTEMPTS = 2000;
    }

    public static final class Steps {
        public static final String CHECK_IF_ACCESS_TOKEN_VALID_STEP =
                "check_if_access_token_is_valid_step";
        public static final String GET_CONSENT_FOR_ALL_ACCOUNTS_STEP =
                "create_consent_for_all_accounts_step";
        public static final String REFRESH_ACCESS_TOKEN_STEP = "refresh_access_token_step";
        public static final String INIT_STEP = "init_step";
        public static final String COLLECT_STATUS_STEP = "collect_status_step";
        public static final String EXCHANGE_CODE_FOR_TOKEN_STEP = "exchange_code_for_token_step";
        public static final String GET_ALL_ACCOUNTS_STEP = "get_all_accounts_step";
        public static final String GET_DETAILED_CONSENT_STEP = "get_detailed_consent_step";
        public static final String DETAILED_CONSENT_SCA_AUTH_STEP =
                "detailed_consent_sca_authentication_step";
    }
}
