package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.rpc.AccountTypes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class FortisConstants {

    public static final String APP_VERSION = "18.0.15";

    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, "HELLO4YOU", "COMFORT PACK")
                    .put(AccountTypes.SAVINGS, "CPTE EPARGNE", "SPAARREKENING")
                    .build();
    
    public static class URLS {
        public static final String GET_DISTRIBUTOR_AUTHENTICATION_MEANS = "/EBIA-pr01/rpc/means/getDistributorAuthenticationMeans";
        public static final String CREATE_AUTHENTICATION_PROCESS = "/EBIA-pr01/rpc/auth/createAuthenticationProcess";
        public static final String GENERATE_CHALLENGES = "/EBIA-pr01/rpc/auth/generateChallenges";
        public static final String GET_USER_INFO = "/TFPL-pr01/rpc/intermediatelogon/getUserinfo";
        public static final String GET_VIEW_ACCOUNT_LIST = "/AC52-pr01/rpc/accounts/getViewAccountList";
        public static final String GET_E_BANKING_USERS = "/EBIA-pr01/rpc/identAuth/getEBankingUsers";
        public static final String AUTHENTICATION_URL = "/SEEA-pa01/SEEAServer";
        public static final String CHECK_FORCED_UPGRADE = "/EBIA-pr01/rpc/forceUpgrade/checkForcedUpgrade";
        public static final String TRANSACTIONS = "/DBPL-pr01/rpc/transaction/GetInitialMovementList";
        public static final String LOGOUT = "/SEEA-pa01/logoff";
        public static final String UPCOMING_TRANSACTIONS = "/DBPL-pr01/rpc/transaction/getUpcomingList";
    }

    public static class ERRORCODE {
        public static String ERROR_CODE = "ErrCode";
        public static String INVALID_SIGNATURE = "EEBA0028";
        public static String WRONG_PASSWORD = "EEBA0028";
        public static String MAXIMUM_NUMBER_OF_TRIES = "EEBA0026";
        public static String INVALID_SIGNATURE_KO = "EWAS0372";
    }

    public static class HEADERS {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CSRF = "CSRF";
    }

    public static class HEADER_VALUES {
        public static final String DEVICE_FEATURES_VALUE = "0|1|0|0|0|1|1|0|0|1|0|0|0|1|1|1242";
        public static final String AUTHENTICATION_DEVICE_PINNING = "08";
        public static final String AUTHENTICATION_PASSWORD = "15";
    }

    public static class FIELD {
        public static final String CLIENTNUMBER = "clientnumber";
    }

    public static class COOKIE {
        public static final String CSRF = "CSRF";
        public static final String AXES = "axes";
        public static final String DEVICE_FEATURES = "deviceFeatures";
        public static final String DISTRIBUTOR_ID = "distributorid";
        public static final String EUROPOLICY = "europolicy";
        public static final String EUROPOLICY_OPTIN = "optin";
    }

    public static class AUTHENTICATION_MEANS {
        public static final String DISTRIBUTION_CHANNEL_ID = "49";
        public static final String MINIMUM_DAC_LEVEL = "3";
    }

    public static class SECURITY {
        public static final String CSRF_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
        public static final String AXES = "en|TAB|fb|priv|TAB|";
        public static final String AXES_CHARS = "0123456789abcdef";
        public static final String A_TO_F = "0123456789ABCDEF";
    }

    public static class ENCRYPTION {
        public static final String OCRA = "OCRA-1:HOTP-SHA256-8:QH64-PSHA1-S064";
    }

    public static class STORAGE {
        public static final String ACCOUNT_PRODUCT_ID = "accountProductId";
        public static final String PASSWORD = "password";
        public static final String SMID = "smid";
        public static final String AGREEMENT_ID = "agreementId";
        public static final String MUID = "muid";
        public static final String DEVICE_FINGERPRINT = "devicefingerprint";
        public static final String CALCULATED_CHALLENGE = "calculatedChallenge";
        public static final String DISTRIBUTOR_ID = "distributorId";
    }

    public static class DATE {
        public static final DateFormat TRANSACTION_FORMAT = new SimpleDateFormat("yyyyMMdd");
    }

    public static class LOGTAG {
        public static final LogTag LOGIN_ERROR = LogTag.from("FORTIS_LOGIN_ERROR");
        public static final LogTag TRANSACTION_VALIDATION_ERROR = LogTag.from("FORTIS_TRANSACTION_VALIDATION_ERROR");
        public static final LogTag MULTIPLE_CHALLENGES = LogTag.from("FORTIS_MULTIPLE_CHALLENGES");
        public static final LogTag MULTIPLE_USER_ENTITIES = LogTag.from("FORTIS_MULTIPLE_USER_ENTITIES");
        public static final LogTag UPCOMING_TRANSACTIONS = LogTag.from("FORTIS_UPCOMING_TRANSACTIONS");
        public static final LogTag UPCOMING_TRANSACTIONS_ERR = LogTag.from("FORTIS_UPCOMING_TRANSACTIONS_ERR");
        public static final LogTag TRANSACTION_VALIDATION_ERR = LogTag.from("FORTIS_TRANSACTION_VALIDATION_ERR");
    }
}
