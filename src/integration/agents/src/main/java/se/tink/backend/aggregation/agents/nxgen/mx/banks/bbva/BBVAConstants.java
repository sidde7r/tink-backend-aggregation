package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva;

import java.text.SimpleDateFormat;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;

public class BBVAConstants {

    //TODO: use id's
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "CH")
                    .put(AccountTypes.SAVINGS, "AH")
                    .put(AccountTypes.CREDIT_CARD, "CARDS")
                    .put(AccountTypes.LOAN, "HIPOTECARIO")
                    .build();

    public static final String APPLICATION_CODE = "RETAILMX";
    public static final String APPLICATION_CODE_VERSION = "1.0";
    public static final String NEW_LINE = "\n";
    public static final String DELIMITER = "--";

    public static class DATE {
        public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        public static final SimpleDateFormat TRANSACTION_DATE_FORAMT =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    }

    public static class URLS {
        public static final String HOST_GLOMO = "https://glomo.bancomermovil.com";
        public static final String GRANT_TICKET =
                "/QSRV_A02/TechArchitecture/mx/grantingTicket/V02";
        public static final String VALIDATE_SUBSCRIPTION =
                "/SRVS_A02/notifications/v0/notifications/validate-subscription";
        public static final String DEVICE_ACTIVATION =
                "/SRVS_A02/customers/v1/customers/device-activation";
        public static final String DIGITAL_ACTIVATION =
                "/SRVS_A02/customers/v1/customers/%s/digital-activation/call-up";
        public static final String CONTACT_TOKEN =
                "/SRVS_A02/security/v1/tokens?contactDetail.contact=%s";
        public static final String TOKEN_AUTH_CODE = "/SRVS_A02/security/v1/software-tokens";
        public static final String TOKEN_ACTIVATION_HASH =
                "/SRVS_A02/security/v1/software-tokens/%s/activation-data-hash/%s";
        public static final String REGISTER_TOKEN =
                "/SRVS_A02/security/v1/software-tokens/register";
        public static final String UPDATE_DEVICE = "/SRVS_A02/devices/v0/devices/%s";

        private static final String FETCH =
                "/SRVS_A02/contracts/v0/financial-overview?contracts.productType=%s";
        public static final String CUSTOMER_INFO =
                "/SRVS_A02/customers/v1/customers?expand=avatars";
        public static final String ACCOUNTS = String.format(FETCH, "ACCOUNTS,DEPOSITS");
        public static final String CARDS = String.format(FETCH, "CARDS,LOANS");
        public static final String TRANSACTIONS = "/SRVS_A02/accounts/v1/accounts/%s/transactions";
    }

    public static class HEADERS {
        public static final String DEVICE_OS_NAME = "vnd.bbva.device-os-name";
        public static final String DEVICE_DPI = "vnd.bbva.device.dpi";
        public static final String DEVICE_OS_VERSION = "vnd.bbva.device-os-version";
        public static final String DEVICE_ID = "vnd.bbva.device-id";
        public static final String DEVICE_APP_NAME = "vnd.bbva.device.app-name";
        public static final String DEVICE_APP_VERSION = "vnd.bbva.device.app-version";
        public static final String DEVICE_MODEL_NAME = "vnd.bbva.device-model-name";
        public static final String DEVICE_MODEL_FACTURER = "vnd.bbva.device-model-facturer";
        public static final String DEVICE_SCREEN_SIZE = "vnd.bbva.device-screen-size";
        public static final String TSEC = "tsec";
        public static final String AUTHENTICATION_TYPE = "authenticationtype";
        public static final String AUTHENTICATION_TYPE_VALUE = "58";
        public static final String AUTHENTICATION_DATA = "authenticationdata";
        public static final String AUTHENTICATION_DATA_DEVICE_ID = "deviceid=%s";

        public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data; boundary=%s";
    }

    public static class QUERY {
        public static final String FROM_DATE = "fromOperationDate";
        public static final String PAGE_SIZE = "pageSize";
        public static final String PAGE_SIZE_VALUE = "100";
        public static final String TO_DATE = "toOperationDate";
    }

    public static class VALUES {
        public static final String DEVICE_OS_NAME = DeviceProfileConfiguration.IOS_STABLE.getOs();
        public static final String DEVICE_DPI = "2X";
        public static final String DEVICE_OS_VERSION =
                DeviceProfileConfiguration.IOS_STABLE.getOsVersion();
        public static final String DEVICE_APP_NAME = "BBVA";
        public static final String DEVICE_APP_VERSION = "1.0.20190129";
        public static final String DEVICE_MODEL_NAME =
                DeviceProfileConfiguration.IOS_STABLE.getPhoneModel();
        public static final String DEVICE_MODEL_FACTURER =
                DeviceProfileConfiguration.IOS_STABLE.getMake();
        public static final String DEVICE_SCREEN_SIZE = "1334x750";
        public static final String USER_AGENT_VALUE =
                "BBVA-Mexico-Prod/1.0.20190129 CFNetwork/808.1.4 Darwin/16.1.0";
        public static final String ACCEPT_LANGUAGE = "es";
        public static final String CONSUMER_ID = "10000033";
        public static final String AUTHENTICATION_PASSWORD = "02";
        public static final String CONTENT_DISPOSITION_DATA =
                "Content-Disposition: form-data;name=\"data\"";
        public static final String CONTENT_DISPOSITION_BIOMETRIC =
                "Content-Disposition: form-data;name=\"biometricFile\"";
        public static final String CURRENT_BALANCE = "CURRENT_BALANCE";
        public static final String LOAN_BALANCE = "CURRENT_CAPITAL_BALANCE";
    }

    public static class STORAGE {
        public static final String PHONE_NUMBER = "phoneNumber";
        public static final String PASSWORD = "password";
        public static final String CARD_NUMBER = "cardNumber";
        public static final String DEVICE_IDENTIFIER = "deviceIdentifier";
        public static final String HOLDERNAME = "holdername";
        public static final String ACCOUNT_ID = "accountId";
        public static final String TSEC = "tsec";
    }

    public static class FIELDS {
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String CARD_NUMBER = "cardNumber";
    }

    public static class ENCRYPTION {
        public static final String PUBLIC_KEY_HEX_DER =
                "30820122300d06092a864886f70d01010105000382010f003082010a0282010100d8ccfa19a1e665ae13c0892b2a29bccaae5793f06d7604eb9f72857ca4743341e592c677500119cd279b0daaa2b1ef917b44ef05afa98177f6c0f7397d665b8ea8a5d5e413e6944087b5fd543f9400bc78396679896c1a04c328d1e8c9302057b4a51aa1044609fc4e00125dceb8609ffd8e5acbf7fe2f14e443933188c64931f696bcf16c939a47ba5899d674eee384c65540aeaa23c38ed72b309250a90b410eb503d82b01c77521158b31bd679a962e78ead1f383fab7d49db3161e4269ece4433e286fa2eeaf887c3b06af7535e30ccee5901ffd35803d87aed90bc8a340d0123b1c34763e7fa24d50b6844f89b4aca6f1a6157ad8b204453da2c600d6130203010001";
    }

    public static class LOGGING {
        public static final LogTag DATE_PARSING_ERROR = LogTag.from("BBVA_DATE_PARSING_ERROR");
        public static final LogTag TRANSACTION_PARSING_ERROR =
                LogTag.from("BBVA_TRANSACTION_PARSING_ERROR");
        public static final LogTag ACCOUNT_PARSING_ERROR =
                LogTag.from("BBVA_ACCOUNT_PARSING_ERROR");
        public static final LogTag AUTO_AUTH = LogTag.from("BBVA_AUTO_AUTH");
    }
}
