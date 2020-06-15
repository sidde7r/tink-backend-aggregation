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
            "Basic aXBob25lOnNlY3JldA=="; // Deocded: iphone:secret
    public static final String BEARER_TOKEN = "Bearer ";
    public static final String SPACE_ID = "spaceId";
    public static final String DEVICE_TOKEN = "device-token";

    public static class Errors {
        public static List<String> continueList =
                Arrays.asList("authorization_pending", "mfa_required");
        public static GenericTypeMapper<AgentError, String> errorsMap =
                GenericTypeMapper.<AgentError, String>genericBuilder()
                        .put(SupplementalInfoError.NO_VALID_CODE, "invalid_otp")
                        .put(LoginError.INCORRECT_CREDENTIALS, "invalid_grant")
                        .put(LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE, "too_many_sms")
                        .ignoreKeys(continueList)
                        .build();
    }

    public static class URLS {
        public static final String HOST = "https://api.tech26.de";
        public static final String BASE_AUTHENTICATION = "/oauth/token";
        public static final String APP_AUTHENTICATION = "/api/mfa/challenge";
        public static final String ME = "/api/me?full=true";
        public static final String ACCOUNT = "/api/accounts";
        public static final String TRANSACTION = "/api/smrt/transactions";
        public static final String SAVINGS = "/api/hub/savings/accounts";
        public static final String SPACES_TRANSACTIONS = "/api/spaces/{spaceId}/transactions";
        public static final String SPACES_SAVINGS = "/api/spaces";
        public static final String LOGOUT = "/api/me/logout";
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
