package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class FortisConstants {

    public static final String APP_VERSION = "30.4.6";
    public static final String LANGUAGE = "EN";
    public static final String MINIMUM_DAC_LEVEL = "3";
    public static final String EASY_PIN_PUBLIC_KEY_ID = "prod-bnppf-eps-public-key01";
    public static final String EASY_PIN_METHOD = "GETAPPLICATIONSCREDENTIALS";
    public static final String EASY_PIN_PROTOCOL_VERSION = "3";
    public static final String DEVICE_NAME = "Tink Phone";
    public static final String SIGNATURE_TYPE = "PIN";
    public static final String SECURITY_TYPE = "EA";
    public static final String APPLICATION_CODE = "EBIA-pr50";
    public static final String TYPE_EASYPIN_BIOMETRIC = "EASYPIN_BIOMETRIC";
    public static final String LEGACY_DISTRIBUTION_CHANNEL_ID = "49";
    public static final String LEGACY_STATIC_CARDFRAME_ID = "010119659";

    public static final String PUBLIC_PROD_KEY =
            "308201a2300d06092a864886f70d01010105000382018f003082018a02820181"
                    + "00ca539588173065e2635fd26389c347eb8d4e8bcc1928f5b6b7c75770b416e6"
                    + "0ea07c4292b9840fb52680f6e6e987ed491d3b60c403fc545102d46485a93f3e"
                    + "46bbd43c156982668f7e081628ea2f3c7399cfcc904851b1b71fdb373bdab0d6"
                    + "e2de6ef38c2d678d0c926d917e5246922e002212c45af88e2efcf21b36b9fb4a"
                    + "11dcb1eee04602340fd2cacf484268d8aa40d89791f2c25b9054ce42051d6033"
                    + "b90b8a7612ea61b74cb824b92a11f418e3952eb6253caaf19329c054ebd0963f"
                    + "19c8765996e4448a2eaeebb36dd9a9a2d1aa837aa4610c73ebde692df0ad1ca3"
                    + "c012548902c669d8b87f18adb62b9e85dd065b68d79c7e46dfaea63da0ee6691"
                    + "7d578bf587c8b7a596f222acc8d9aed08f98c38e7d1ac0fe3096a2a9484937ce"
                    + "31d51308cd68412d227ec2b6b85353f3397ca3ed0cfcd8666ab724193f307bdc"
                    + "14385c3fb4c75d246ef78eb42f7789b332bee28f91a8a06d345bf4e448718173"
                    + "ddf9fefa115e2079e4eab02aff5bc92f57d6d60231c143610fc8e9bb00667a1b"
                    + "8b0203010001";

    public static final String NEGATIVE_TRANSACTION_TYPE = "F";

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
        public static final String INITIALIZE_LOGIN_TRANSACTION =
                "/EBIA-pr01/rpc/identAuth/initializeLoginTransaction";
        public static final String CHECK_LOGIN_RESULT = "/EBIA-pr01/rpc/identAuth/checkLoginResult";
        public static final String EBEW_APP_LOGIN_V2 =
                "/SEPLJ00/sps/authsvc/policy/ebew_app_login_v2";
        public static final String GET_USER_INFO = "/TFPL-pr01/rpc/intermediatelogon/getUserinfo";
        public static final String GET_COUNTRY_LIST = "/OCPL-pr01/rpc/getCountryList";
        public static final String EASYPIN_CREATE = "/SDPL-pr50/rpc/easypin/create";
        public static final String EASYPIN_PROVISION = "/SDPL-pr50/rpc/easypin/provision";
        public static final String EASYPIN_ACTIVATE = "/SDPL-pr50/rpc/easypin/activate";
        public static final String INITIATE_SIGN_TRANSACTION =
                "/EBIA-pr50/rpc/sign/initiateSignTransaction";
        public static final String RETRIEVE_AUTHORISED_SIGN_MEANS =
                "/EBIA-pr50/rpc/sign/retrieveAuthorisedSignMeans";
        public static final String EXECUTE_SIGN_TRANSACTION =
                "/EBIA-pr50/rpc/sign/executeSignTransaction";

        public static final String GET_VIEW_ACCOUNT_LIST =
                "/AC52-pr01/rpc/accounts/getViewAccountList";
        public static final String TRANSACTIONS =
                "/DBPL-pr01/rpc/transaction/GetInitialMovementList";
        public static final String UPCOMING_TRANSACTIONS =
                "/DBPL-pr01/rpc/transaction/getUpcomingList";

        public static class Legacy {
            public static final String CHECK_FORCED_UPGRADE =
                    "/EBIA-pr01/rpc/forceUpgrade/checkForcedUpgrade";
            public static final String CREATE_AUTHENTICATION_PROCESS =
                    "/EBIA-pr01/rpc/auth/createAuthenticationProcess";
            public static final String GET_DISTRIBUTOR_AUTHENTICATION_MEANS =
                    "/EBIA-pr01/rpc/means/getDistributorAuthenticationMeans";
            public static final String GET_E_BANKING_USERS =
                    "/EBIA-pr01/rpc/identAuth/getEBankingUsers";
            public static final String GENERATE_CHALLENGES =
                    "/EBIA-pr01/rpc/auth/generateChallenges";
            public static final String AUTHENTICATION_URL = "/SEEA-pa01/SEEAServer";
        }

        public static final String LOGOUT = "/SEEA-pa01/logoff";
    }

    public static class Form {
        public static final String METHOD = "METHOD";
        public static final String PROTOCOL_VERSION = "PROTOCOL_VERSION";
        public static final String PUBLIC_KEY_ID = "PUBLIC_KEY_ID";
        public static final String ENC_QUERY_DATA = "ENC_QUERY_DATA";
    }

    public static class MeanIds {
        public static final String UCR = "08";
        public static final String EAPI = "30";
        public static final String LEGACY = "15";
    }

    public static class ErrorCode {
        public static final String ERROR_CODE = "ErrCode";
        public static final String INVALID_SIGNATURE = "EEBA0028";
        public static final String MAXIMUM_NUMBER_OF_TRIES = "EEBA0026";
        public static final String INVALID_SIGNATURE_KO = "EWAS0372";
        public static final String COMBINATION_HARDWARE_ID_AND_LOGIN_ID_NOT_FOUND = "EEBW6112";
        public static final String MUID_OK = "EBW0000";
    }

    public static class Headers {
        public static final String USER_AGENT = "User-Agent";
        public static final String CSRF = "CSRF";
        public static final String COOKIE = "Cookie";
        public static final String CONTENT_TYPE = "Content-Type";
    }

    public static class HeaderValues {
        public static final String DEVICE_FEATURES_VALUE = "0|1|0|0|0|1|1|0|0|1|0|0|0|1|1|750";
    }

    public static class Cookie {
        public static final String CSRF = "CSRF";
        public static final String AXES = "axes";
        public static final String DEVICE_FEATURES = "deviceFeatures";
        public static final String DISTRIBUTOR_ID = "distributorid";
        public static final String EUROPOLICY = "europolicy";
        public static final String EUROPOLICY_OPTIN = "optin";
    }

    public static class Encryption {
        public static final String LEGACY_OCRA_S064 = "OCRA-1:HOTP-SHA256-8:QH64-PSHA1-S064";
        public static final String OCRA_T10S = "OCRA-1:HOTP-SHA256-8:QH64-T10S";
    }

    public static class Storage {
        public static final String ACCOUNT_PRODUCT_ID = "accountProductId";
    }

    public static class LoggingTag {
        public static final LogTag TRANSACTION_VALIDATION_ERROR =
                LogTag.from("FORTIS_TRANSACTION_VALIDATION_ERROR");
        public static final LogTag MULTIPLE_CHALLENGES = LogTag.from("FORTIS_MULTIPLE_CHALLENGES");
    }

    static class HttpClient {
        public static final int MAX_RETRIES = 4;
        public static final int RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
