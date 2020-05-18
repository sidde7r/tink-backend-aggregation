package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class FortisConstants {

    public static final String APP_VERSION = "24.1.2";
    public static final String NEGATIVE_TRANSACTION_TYPE = "F";
    public static final String CARD_FRAME_ID = "010119659";

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            "HELLO4YOU",
                            "ESSENTIAL PRO",
                            "COMFORT PACK",
                            "COMPTE VUE PRO")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            "CPTE EPARGNE",
                            "SPAARREKENING",
                            "PRO SPAAR",
                            "ster spaarrek.")
                    .build();

    public static class Urls {
        public static final String GET_DISTRIBUTOR_AUTHENTICATION_MEANS =
                "/EBIA-pr01/rpc/means/getDistributorAuthenticationMeans";
        public static final String CREATE_AUTHENTICATION_PROCESS =
                "/EBIA-pr01/rpc/auth/createAuthenticationProcess";
        public static final String GENERATE_CHALLENGES = "/EBIA-pr01/rpc/auth/generateChallenges";
        public static final String GET_USER_INFO = "/TFPL-pr01/rpc/intermediatelogon/getUserinfo";
        public static final String PREPARE_CONTRACT_UPDATE =
                "/TFPL-pr01/rpc/contractUpdate/prepareContractUpdate";
        public static final String EXECUTE_CONTRACT_UPDATE =
                "/TFPL-pr01/rpc/contractUpdate/executeContractUpdate";
        public static final String GET_VIEW_ACCOUNT_LIST =
                "/AC52-pr01/rpc/accounts/getViewAccountList";
        public static final String GET_E_BANKING_USERS =
                "/EBIA-pr01/rpc/identAuth/getEBankingUsers";
        public static final String AUTHENTICATION_URL = "/SEEA-pa01/SEEAServer";
        public static final String CHECK_FORCED_UPGRADE =
                "/EBIA-pr01/rpc/forceUpgrade/checkForcedUpgrade";
        public static final String TRANSACTIONS =
                "/DBPL-pr01/rpc/transaction/GetInitialMovementList";
        public static final String LOGOUT = "/SEEA-pa01/logoff";
        public static final String UPCOMING_TRANSACTIONS =
                "/DBPL-pr01/rpc/transaction/getUpcomingList";
    }

    public static class ErrorCode {
        public static String ERROR_CODE = "ErrCode";
        public static String INVALID_SIGNATURE = "EEBA0028";
        public static String MAXIMUM_NUMBER_OF_TRIES = "EEBA0026";
        public static String INVALID_SIGNATURE_KO = "EWAS0372";
        public static String COMBINATION_HARDWARE_ID_AND_LOGIN_ID_NOT_FOUND = "EEBW6112";
        public static String MUID_OK = "EBW0000";
    }

    public static class Headers {
        public static final String USER_AGENT = "User-Agent";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CSRF = "CSRF";
    }

    public static class HeaderValues {
        public static final String DEVICE_FEATURES_VALUE = "0|1|0|0|0|1|1|0|0|1|0|0|0|1|1|1242";
        public static final String AUTHENTICATION_DEVICE_PINNING = "08";
        public static final String AUTHENTICATION_PASSWORD = "15";
    }

    public static class Field {
        public static final String CLIENTNUMBER = "clientnumber";
    }

    public static class Values {
        public static final String TCFLAG = "0";
        public static final String UCR = "UCR";
        public static final String DIDAP = "DIDAP";
        public static final String PASSWORD = "tinktink";
    }

    public static class Cookie {
        public static final String CSRF = "CSRF";
        public static final String AXES = "axes";
        public static final String DEVICE_FEATURES = "deviceFeatures";
        public static final String DISTRIBUTOR_ID = "distributorid";
        public static final String EUROPOLICY = "europolicy";
        public static final String EUROPOLICY_OPTIN = "optin";
    }

    public static class AuthenticationMeans {
        public static final String DISTRIBUTION_CHANNEL_ID = "49";
        public static final String MINIMUM_DAC_LEVEL = "3";
    }

    public static class Security {
        public static final String CSRF_CHARS =
                "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
        public static final String AXES = "en|TAB|fb|priv|TAB|";
        public static final String AXES_CHARS = "0123456789abcdef";
        public static final String A_TO_F = "0123456789ABCDEF";
    }

    public static class Encryption {
        public static final String OCRA = "OCRA-1:HOTP-SHA256-8:QH64-PSHA1-S064";
    }

    public static class Storage {
        public static final String ACCOUNT_PRODUCT_ID = "accountProductId";
        public static final String PASSWORD = "password";
        public static final String SMID = "smid";
        public static final String AGREEMENT_ID = "agreementId";
        public static final String MUID = "muid";
        public static final String DEVICE_FINGERPRINT = "devicefingerprint";
        public static final String CALCULATED_CHALLENGE = "calculatedChallenge";
        public static final String CHALLENGE = "challenge";
        public static final String MANUAL_AUTHENTICATION_REQUIRED = "manualAuthenticationRequired";
    }

    public static class LoggingTag {
        public static final LogTag NO_USER_DATA_FOUND = LogTag.from("NO_USER_DATA_FOUND");
        public static final LogTag LOGIN_ERROR = LogTag.from("FORTIS_LOGIN_ERROR");
        public static final LogTag TRANSACTION_VALIDATION_ERROR =
                LogTag.from("FORTIS_TRANSACTION_VALIDATION_ERROR");
        public static final LogTag MULTIPLE_CHALLENGES = LogTag.from("FORTIS_MULTIPLE_CHALLENGES");
        public static final LogTag MULTIPLE_USER_ENTITIES =
                LogTag.from("FORTIS_MULTIPLE_USER_ENTITIES");
        public static final LogTag UPCOMING_TRANSACTIONS =
                LogTag.from("FORTIS_UPCOMING_TRANSACTIONS");
    }

    static class HttpClient {
        public static final int MAX_RETRIES = 4;
        public static final int RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
