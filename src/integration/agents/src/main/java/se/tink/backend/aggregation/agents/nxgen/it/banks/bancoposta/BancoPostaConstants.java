package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BancoPostaConstants {
    public static class Urls {
        public static final URL AUTH_REQ_AZ =
                new URL(
                        "https://oidc-proxy.mobile.poste.it/jod-oidc-proxy/federation/mobile/v1/az-req");
        public static final URL AUTH_JWT =
                new URL("https://securelogin.mobile.poste.it/jod-fcc/authorizationJwt");
        public static final URL REGISTER_INIT = new URL(Base.BASE_AUTH2 + Endpoints.REGISTER_INIT);
        public static final URL ACTIVATION = new URL(Base.BASE_AUTH2 + Endpoints.ACTIVATION);
        public static final URL REGISTER = new URL(Base.BASE_AUTH2 + Endpoints.REGISTER);
        public static final URL REGISTER_APP = new URL(Base.BASE_AUTH3 + Endpoints.REGISTER_APP);
        public static final URL CHECK_REGISTER =
                new URL(Base.BASE_AUTH3 + Endpoints.CHECK_REGISTER);
        public static final URL AUTHORIZE_TRANSACTION =
                new URL(Base.BASE_AUTH3 + Endpoints.AUTHORIZE_TRANSACTION);
        public static final URL INIT_SYNC_WALLET =
                new URL(Base.BASE_AUTH + Endpoints.INIT_SYNC_WALLET);
        public static final URL SEND_OTP = new URL(Base.BASE_AUTH + Endpoints.SEND_OTP);
        public static final URL ELIMINA_WALLET = new URL(Base.BASE_AUTH + Endpoints.ELIMINA_WALLET);
        public static final URL INIT_CODE_VERIFICATION =
                new URL(Base.BASE_AUTH + Endpoints.INIT_CODE_VERIFICATION);
        public static final URL SEND_POSTE_CODE =
                new URL(Base.BASE_AUTH + Endpoints.SEND_POSTE_CODE);
        public static final URL ONBOARDING_VERIFICATION =
                new URL(Base.BASE_AUTH + Endpoints.ONBOARDING_VERIFICATION);
        public static final URL FETCH_ACCOUNTS = new URL(Base.BASE_DATA + Endpoints.FETCH_ACCOUNTS);
        public static final URL AUTH_OPENID_AZ =
                new URL(Base.BASE_AUTH4 + Endpoints.AUTH_OPENID_AZ);
        public static final URL CHALLENGE = new URL(Base.BASE_AUTH4 + Endpoints.CHALLENGE);

        public static class Base {
            public static final String BASE_DATA = "https://pfm.poste.it/user/v1";
            public static final String BASE_AUTH =
                    "https://appbp.mobile.poste.it/jod-mobile-server/json/services/sca/v1/bancoposta";
            public static final String BASE_AUTH2 =
                    "https://appregistry.mobile.poste.it/jod-app-registry/v2";
            public static final String BASE_AUTH3 =
                    "https://sh2-web.poste.it//jod-secure-holder2-web/public/app/v1";
            public static final String BASE_AUTH4 = "https://idp-poste.poste.it/jod-idp-retail";
        }

        public static class Endpoints {
            public static final String FETCH_ACCOUNTS = "/accounts";
            public static final String REGISTER_INIT = "/registerInit";
            public static final String ACTIVATION = "/activation";
            public static final String REGISTER = "/register";
            public static final String REGISTER_APP = "/registerApp";
            public static final String CHECK_REGISTER = "/checkRegisterApp";
            public static final String AUTHORIZE_TRANSACTION = "/authorizeTransaction";
            public static final String INIT_SYNC_WALLET = "/initSyncWallet";
            public static final String SEND_OTP = "/sendOtpEliminaWallet";
            public static final String ELIMINA_WALLET = "/eliminaWallet";
            public static final String INIT_CODE_VERIFICATION =
                    "/initAssociaContoWalletByCodiceDigital";
            public static final String SEND_POSTE_CODE = "/associaContoWalletByCodiceDigital";
            public static final String ONBOARDING_VERIFICATION = "/verificaOnboarding";
            public static final String CHALLENGE = "/securetool/v1/challenge";
            public static final String AUTH_OPENID_AZ = "/federation/v2/openid-az";
        }
    }

    public static class JWT {
        public static class Claims {
            public static final String APP_ID = "app_id";
            public static final String APP_REGISTER_ID = "appRegisterID";
            public static final String USER_PIN = "userPIN";
            public static final String OTP = "otp";
        }
    }

    public static class HeaderValues {
        public static final String XKEY = "X-KEY";
        public static final String ACCEPT = "*/*";
        public static final String BEARER = "Bearer ";
    }

    public static class Storage {
        public static final String KEY_PAIR = "keyPair";
        public static final String APP_ID = "appId";
        public static final String SECRET_APP = "secretApp";
        public static final String APP_REGISTER_ID = "appRegisterId";
        public static final String ACCESS_BASIC_TOKEN = "accessBasicToken";
        public static final String ACCESS_DATA_TOKEN = "accessDataToken";
        public static final String USER_PIN = "userPin";
        public static final String PUB_SERVER_KEY = "pubServerKey";
        public static final String OTP_SECRET_KEY = "otpSecretKey";
        public static final String MANUAL_AUTH_FINISH_FLAG = "manualAuthFinishFlag";
    }

    public static class FormParams {
        public static final String SCOPE = "scope";
        public static final String APP_NAME = "app_name";
        public static final String ACR_VALUES = "acr_values";
        public static final String SUB = "sub";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REQUEST = "request";
        public static final String CREDENTIALS = "credentials";
        public static final String RESPONSE_TYPE = "response_type";
    }

    public static class FormValues {
        public static final String SCOPE =
                "userid surname name client_code taxcode sex birthdate birthnation_code birthprovince_code birthtown_code birthtown_description mobileNumber email prRoleMember";
        public static final String APP_NAME = "app-bpol";
        public static final String ACR_VALUES = "https://idp.poste.it/L1";
        public static final String SUB = "poste.it";
        public static final String GRANT_TYPE = "password";
        public static final String RESPONSE_TYPE = "token";
    }
}
