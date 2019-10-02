package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.errors.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.i18n.LocalizableKey;

public class N26Constants {

    public static final String BASIC_AUTHENTICATION_TOKEN =
            "Basic aXBob25lOnNlY3JldA=="; // Deocded: iphone:secret
    public static final String BEARER_TOKEN = "Bearer ";
    public static final String CURRENCY_EUR = "EUR";
    public static final int ONETHOUSAND = 1000;
    public static final String SPACE_ID = "spaceId";
    public static final String MFA_REQUIRED = "mfa_required";
    public static final String DEVICE_TOKEN = "device-token";

    public static class Errors {
        public static List<String> continueList =
                Arrays.asList("authorization_pending", "mfa_required");
        public static GenericTypeMapper<AgentError, String> errorsMap =
                GenericTypeMapper.<AgentError, String>genericBuilder()
                        .put(SupplementalInfoError.NO_VALID_CODE, "invalid_otp")
                        .put(ThirdPartyAppError.CANCELLED, "invalid_grant")
                        .put(LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE, "too_many_sms")
                        .ignoreKeys(continueList)
                        .build();

        public static final ImmutableMap<ThirdPartyAppStatus, LocalizableKey>
                THIRD_PARTY_APP_ERROR =
                        ImmutableMap.<ThirdPartyAppStatus, LocalizableKey>builder()
                                .put(
                                        ThirdPartyAppStatus.CANCELLED,
                                        new LocalizableKey(
                                                "Authentication cancelled by the Codes app. Please try again."))
                                .put(
                                        ThirdPartyAppStatus.TIMED_OUT,
                                        new LocalizableKey("Authentication timed out."))
                                .put(
                                        ThirdPartyAppStatus.ALREADY_IN_PROGRESS,
                                        new LocalizableKey(
                                                "Another client is already trying to sign in. \nPlease close the Codes app and try again."))
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
        public static final String FIXED_SAVINGS = "/api/hub/savings/fixedterms/accounts";
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
        public static final String FULL = "full";
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
