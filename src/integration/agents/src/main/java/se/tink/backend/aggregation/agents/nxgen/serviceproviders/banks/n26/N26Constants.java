package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;

public class N26Constants {

    public static final String BASIC_AUTHENTICATION_TOKEN =
            "Basic bmF0aXZlaW9zOg=="; // Decoded value from base64: nativeios:
    public static final String BEARER_TOKEN = "Bearer ";
    public static final String SPACE_ID = "spaceId";
    public static final String DEVICE_TOKEN = "device-token";

    static class Errors {
        static final List<String> CONTINUE_LIST =
                Arrays.asList("authorization_pending", "mfa_required");
        static final GenericTypeMapper<AgentError, String> ERRORS_MAP =
                GenericTypeMapper.<AgentError, String>genericBuilder()
                        .put(SupplementalInfoError.NO_VALID_CODE, "invalid_otp")
                        .put(LoginError.INCORRECT_CREDENTIALS, "invalid_grant")
                        .put(LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE, "too_many_sms")
                        .ignoreKeys(CONTINUE_LIST)
                        .build();
    }

    static class URLS {
        static final String HOST = "https://api.tech26.de";
        static final String BASE_AUTHENTICATION = "/oauth/token";
        static final String APP_AUTHENTICATION = "/api/mfa/challenge";
        static final String ME = "/api/me?full=true";
        static final String ACCOUNT = "/api/accounts";
        static final String TRANSACTION = "/api/smrt/transactions";
        static final String SAVINGS = "/api/hub/savings/accounts";
        static final String SPACES_TRANSACTIONS = "/api/spaces/{spaceId}/transactions";
        static final String SPACES_SAVINGS = "/api/spaces";
        static final String LOGOUT = "/api/me/logout";
    }

    public static class Storage {
        public static final String TOKEN_ENTITY = "TOKEN_ENTITY";
        public static final String MFA_TOKEN = "MFA_TOKEN";
        public static final String DEVICE_TOKEN = "DEVICE_TOKEN";
    }

    public static class Queryparams {
        public static final String LASTID = "lastId";
        public static final String LIMIT = "limit";
        public static final String TRANSACTION_LIMIT_DEFAULT = "20";
        public static final String SPACE_TRANSACTIONS_SIZE = "size";
        public static final String SPACE_LIMIT_DEFAULT = "20";
        public static final String SPACE_BEFOREID = "beforeId";
    }

    public static class Body {

        public static final String GRANT_TYPE = "grant_type";

        public static class Password {
            public static final String PASSWORD = "password";
            public static final String USERNAME = "username";
        }

        public static class MultiFactor {
            public static final String APP = "oob";
            public static final String MFA_OOB = "mfa_oob";
            public static final String MFA_OTP = "mfa_otp";
            public static final String MFA_TOKEN = "mfaToken";
            public static final String SMS = "otp";
        }
    }

    public static class Logging {
        public static final LogTag TRANSACTION_PAGINATION_ERROR =
                LogTag.from("N26_TRANSACTION_PAGINATION_ERROR");
    }
}
