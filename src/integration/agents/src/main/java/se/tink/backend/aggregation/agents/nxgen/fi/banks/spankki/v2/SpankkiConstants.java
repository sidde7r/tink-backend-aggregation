package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2;

import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;
import se.tink.libraries.account.enums.AccountFlag;

public class SpankkiConstants {
    public static final DeviceProfile DEVICE_PROFILE = DeviceProfileConfiguration.IOS_STABLE;

    public static class Urls {
        public static final String HOST = "https://mobile.s-pankki.fi";
        public static final String ENCAP_HOST =
                "https://tunnistus.s-pankki.fi/platform-smartdevice/client";

        public static final URL KEEP_ALIVE = new URL(HOST + Endpoints.KEEP_ALIVE);
        public static final URL REQUEST_CHALLENGE = new URL(HOST + Endpoints.REQUEST_CHALLENGE);
        public static final URL RESPONSE_CHALLENGE = new URL(HOST + Endpoints.RESPONSE_CHALLENGE);
        public static final URL LOGIN_USERPWD = new URL(HOST + Endpoints.LOGIN_USERPWD);
        public static final URL LOGIN_KEYCARD = new URL(HOST + Endpoints.LOGIN_KEYCARD);
        public static final URL START_ENCAP = new URL(HOST + Endpoints.START_ENCAP);
        public static final URL POLL_ENCAP = new URL(HOST + Endpoints.POLL_ENCAP);
        public static final URL GET_PHONENUMBER = new URL(HOST + Endpoints.GET_PHONENUMBER);
        public static final URL RECEIVE_OTP = new URL(HOST + Endpoints.RECEIVE_OTP);
        public static final URL VERIFY_OTP = new URL(HOST + Endpoints.VERIFY_OTP);
        public static final URL ENCAP = new URL(ENCAP_HOST);
        public static final URL FETCH_ACCOUNTS = new URL(HOST + Endpoints.FETCH_ACCOUNTS);
        public static final URL FETCH_TRANSACTIONS = new URL(HOST + Endpoints.FETCH_TRANSACTIONS);
    }

    public static class Endpoints {
        public static final String VERSION = "/v2";

        public static final String KEEP_ALIVE = VERSION + "/bank/keepalive/refresh";
        public static final String REQUEST_CHALLENGE = VERSION + "/authentication/device/chreq";
        public static final String RESPONSE_CHALLENGE = VERSION + "/authentication/device/chresp";
        public static final String LOGIN_USERPWD = VERSION + "/authentication/usrpwd";
        public static final String LOGIN_KEYCARD = VERSION + "/authentication/tan";
        public static final String START_ENCAP = VERSION + "/authentication/encap/start";
        public static final String POLL_ENCAP = VERSION + "/authentication/encap/perform";
        public static final String GET_PHONENUMBER = VERSION + "/identification/phonenumber";
        public static final String RECEIVE_OTP = VERSION + "/identification/phonenumber/activate";
        public static final String VERIFY_OTP = VERSION + "/identification/activation/start10";
        public static final String FETCH_ACCOUNTS = VERSION + "/bank/customer/accounts/get";
        public static final String FETCH_TRANSACTIONS =
                VERSION + "/bank/customer/transactions/get/{accountId}/{page}";
    }

    public static class Authentication {
        public static final String REQUEST_TOKEN_HASH_SALT = "f3803b2dcc4d4a718c01f1efc0da0f16";
        public static final String CHALLENGE_RESPONSE_HASH_SALT =
                "439aa95dc6c943e1a88611f1f466b27d";
        public static final String B64_ELLIPTIC_CURVE_PUBLIC_KEY =
                "MFIwEAYHKoZIzj0CAQYFK4EEABoDPgAEAdI87D0d2WOaZq5LBZRBxbmeZnaDvpNutQJNjvb3AVtGSsf2rq1lu4PAmk4bnl2DRlSbtV8spT4l4tDi";
        public static final int KEY_CARD_VALUE_LENGTH = 4;
        public static final int SMS_OTP_VALUE_LENGTH = 4;
    }

    public static class EncapMessage {
        public static final String APPLICATION_ID = "sbaEncapSba";
        public static final String CLIENT_ONLY = "false";
    }

    public static class Request {
        public static final String CLIENT_INFO_APP_NAME = "SBank2.0";
        public static final String CLIENT_INFO_APP_VERSION = "2.3.0.2";
        public static final String CLIENT_INFO_LANG = "sv";
    }

    public static class Headers {
        public static final String X_SMOB_KEY = "X-smob";
        public static final String SPANKKI_USER_AGENT = "spankki/2.3.0";
    }

    public static class Storage {
        public static final String SESSION_ID = "sessionId";
        public static final String DEVICE_ID = "deviceId";
        public static final String HARDWARE_ID = "hardwareId";
        public static final String LOGIN_TOKEN = "loginToken";
        public static final String CUSTOMER_ID = "customerId";
        public static final String CUSTOMER_USER_ID = "customerUserId";
        public static final String CUSTOMER_ENTITY = "customerEntity";
    }

    public static class StatusMessages {
        public static final String INTERNAL_ERROR_CODE = "98";
        public static final String INTERNAL_ERROR_MESSAGE = "INTERNAL_SERVER_ERROR";
        public static final String SESSION_EXPIRED_MESSAGE = "SESSION_EXPIRED";
        public static final String USER_LOCKED = "USER_LOCKED";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAGE = "page";
    }

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "418",
                            "415")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "443",
                            "419",
                            "427",
                            "416")
                    .build();

    public static class Regex {
        public static final String WHITE_SPACE = "\\s+";
    }
}
