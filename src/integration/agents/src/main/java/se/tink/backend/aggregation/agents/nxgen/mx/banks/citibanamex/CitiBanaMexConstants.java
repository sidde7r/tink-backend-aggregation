package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex;

import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public class CitiBanaMexConstants {

    public static class BaseFormRequestParams {
        public static final String APP_ID = "appID";
        public static final String APP_VERSION = "appver";
        public static final String CHANNEL = "channel";
        public static final String DEVICE_ID = "deviceId";
        public static final String LANG = "lang";
        public static final String MOBILE_IP_ADDRES = "mobileIpAddress";
        public static final String PLATFORM = "platform";
        public static final String PLATFORM_VERSION = "platformver";
        public static final String SERVICE_ID = "serviceID";
        public static final String RSA_MOBILE_SDK = "RSA_mobileSDK";
    }

    // At the time of writing CitiBanaMax seems to implement their version check as '> {current}'
    // This means that v.99.9.9 is considered supported, which should allow us to not have to
    // update it too often.
    public static class BaseFormRequestValues {
        public static final String APP_ID = "CitiBanamex";
        public static final String APP_VERSION = "99.9.9";
        public static final String CHANNEL = "rc";
        public static final String LANG = "1";
        public static final String MOBILE_IP_ADDRES = "";
        public static final String PLATFORM = "iPhone";
        public static final String PLATFORM_VERSION = "99.9.9";

        public static final int COMPROMISED = 7;
        public static final int EMULATOR = 0;
        public static final String SDK_VERSION = "3.7.HF-10";
    }

    public static class Header {
        public static final String USER_AGENT =
                "Citibanamex/28.0.0 CFNetwork/811.4.18 Darwin/16.5.0";
    }

    public static class RequestServiceIds {
        public static final String GET_CLIENT_NAME = "getClientNameBM";
        public static final String LOGIN = "login";
        public static final String GET_MULTI_BALANCE = "getMultiBalance";
        public static final String GET_PREVIOUS_MOVEMENTS = "getPreviousMovements";
        public static final String LOGOUT = "logout";
        public static final String GET_OFFERS_XSELL = "getOffersXSell";
    }

    public static class RequestParams {
        public static final String CLIENT_NUMBER = "clientNumber";
        public static final String PASSWORD = "password";
        public static final String CONTINUITY_CODE = "continuityCode";
        public static final String ACCOUNT_ID = "accountId";
    }

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(TransactionalAccountType.CHECKING, AccountFlag.PSD2_PAYMENT_ACCOUNT, "6")
                    .build();

    public static class Errors {
        public static final String BANK_SIDE_ERROR =
                "Hubo un error de comunicación, por favor inténtalo nuevamente";
        public static final String INCORRECT_CLIENT_NAME =
                "Lo sentimos\\nDebido a que no cuentas con los datos necesarios para continuar, es necesario que acudas a una sucursal para concluir tu registro";
        public static final String INCORRECT_PASSWORD = "No hay cuentas disponibles";
        public static final String MULTIPLE_SESSION_ACTIVE =
                "Solo puedes tener una sesión activa. Intenta más tarde";
    }

    public static class Urls {
        public static final String BASE_URL = "https://mobile.citibanamex.com:443";
        private static final String BASE_PATH = "/c735_015_middlewarev1/MWServlet?serviceID=%s";
        public static final String GET_CLIENT_NAME = String.format(BASE_PATH, "getClientNameBM");
        public static final String LOGIN = String.format(BASE_PATH, "login");
        public static final String GET_MULTI_BALANCE = String.format(BASE_PATH, "getMultiBalance");
        public static final String GET_PREVIOUS_TRANSACTIONS =
                String.format(BASE_PATH, "getPreviousMovements");
        public static final String LOGOUT = String.format(BASE_PATH, "logout");
        public static final String GET_OFFERS_XSELL = String.format(BASE_PATH, "getOffersXSell");
    }

    public static class Storage {
        public static final String RSA_APPLICATION_KEY = "rsaApplicationKey";
        public static final String TIMESTAMP = "timestamp";
        public static final String HARDWARE_ID = "hardwareId";
        public static final String DEVICE_ID = "deviceId";
        public static final String HOLDER_NAME = "holderName";
    }

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }

    public static final String AMOUNT_REGEX = "[$,]+";
}
