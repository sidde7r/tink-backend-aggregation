package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BancoPostaConstants {
    public static class Urls {
        public static class AuthUrl {
            public static final URL AUTH_REQ_AZ =
                    new URL(
                            "https://oidc-proxy.mobile.poste.it/jod-oidc-proxy/federation/mobile/v1/az-req");
            public static final URL AUTH_JWT =
                    new URL("https://securelogin.mobile.poste.it/jod-fcc/authorizationJwt");
            public static final URL REGISTER_INIT =
                    new URL(Base.BASE_AUTH2 + Endpoints.REGISTER_INIT);
            public static final URL ACTIVATION = new URL(Base.BASE_AUTH2 + Endpoints.ACTIVATION);
            public static final URL REGISTER = new URL(Base.BASE_AUTH2 + Endpoints.REGISTER);
            public static final URL REGISTER_APP =
                    new URL(Base.BASE_AUTH3 + Endpoints.REGISTER_APP);
            public static final URL CHECK_REGISTER =
                    new URL(Base.BASE_AUTH3 + Endpoints.CHECK_REGISTER);
            public static final URL AUTHORIZE_TRANSACTION =
                    new URL(Base.BASE_AUTH3 + Endpoints.AUTHORIZE_TRANSACTION);
            public static final URL INIT_SYNC_WALLET =
                    new URL(Base.BASE_API + Endpoints.INIT_SYNC_WALLET);
            public static final URL SEND_OTP = new URL(Base.BASE_API + Endpoints.SEND_OTP);
            public static final URL ELIMINA_WALLET =
                    new URL(Base.BASE_API + Endpoints.ELIMINA_WALLET);
            public static final URL INIT_CODE_VERIFICATION =
                    new URL(Base.BASE_API + Endpoints.INIT_CODE_VERIFICATION);
            public static final URL SEND_POSTE_CODE =
                    new URL(Base.BASE_API + Endpoints.SEND_POSTE_CODE);
            public static final URL ONBOARDING_VERIFICATION =
                    new URL(Base.BASE_API + Endpoints.ONBOARDING_VERIFICATION);
            public static final URL AUTH_OPENID_AZ =
                    new URL(Base.BASE_AUTH4 + Endpoints.AUTH_OPENID_AZ);
            public static final URL CHALLENGE = new URL(Base.BASE_AUTH4 + Endpoints.CHALLENGE);
        }

        public static class CheckingAccUrl {
            public static final URL FETCH_ACCOUNTS =
                    new URL(Base.BASE_DATA + Endpoints.FETCH_ACCOUNTS);
            public static final URL FETCH_ACCOUNT_DETAILS =
                    new URL(Base.BASE_API + Endpoints.ACCOUNT_DETAILS);
            public static final URL FETCH_TRANSACTIONS =
                    new URL(Base.BASE_API + Endpoints.FETCH_TRANSACTIONS);
        }

        public static class SavingAccUrl {
            public static final URL FETCH_SAVING_ACCOUNTS =
                    new URL(Base.BASE_API + Endpoints.FETCH_SAVING_ACCOUNTS);
            public static final URL FETCH_SAVING_ACCOUNTS_DETAILS =
                    new URL(Base.BASE_API + Endpoints.FETCH_SAVING_ACCOUNTS_DETAILS);
            public static final URL FETCH_SAVING_TRANSACTIONS =
                    new URL(Base.BASE_API + Endpoints.FETCH_SAVING_TRANSACTIONS);
        }

        public static class Base {
            public static final String BASE_DATA = "https://pfm.poste.it/user/v1";
            public static final String BASE_API =
                    "https://appbp.mobile.poste.it/jod-mobile-server/json/services/sca";
            public static final String BASE_AUTH2 =
                    "https://appregistry.mobile.poste.it/jod-app-registry/v2";
            public static final String BASE_AUTH3 =
                    "https://sh2-web.poste.it//jod-secure-holder2-web/public/app/v1";
            public static final String BASE_AUTH4 = "https://idp-poste.poste.it/jod-idp-retail";
        }

        public static class Endpoints {
            public static final String FETCH_ACCOUNTS = "/accounts";
            public static final String FETCH_TRANSACTIONS =
                    "/v1/bancoposta/ricercaListaMovimentiConto";
            public static final String ACCOUNT_DETAILS =
                    "/v1/bancoposta/ricercaListaMovimentiConto";
            public static final String REGISTER_INIT = "/registerInit";
            public static final String ACTIVATION = "/activation";
            public static final String REGISTER = "/register";
            public static final String REGISTER_APP = "/registerApp";
            public static final String CHECK_REGISTER = "/checkRegisterApp";
            public static final String AUTHORIZE_TRANSACTION = "/authorizeTransaction";
            public static final String INIT_SYNC_WALLET = "/v1/bancoposta/initSyncWallet";
            public static final String SEND_OTP = "/v1/bancoposta/sendOtpEliminaWallet";
            public static final String ELIMINA_WALLET = "/v1/bancoposta/eliminaWallet";
            public static final String INIT_CODE_VERIFICATION =
                    "/v1/bancoposta/initAssociaContoWalletByCodiceDigital";
            public static final String SEND_POSTE_CODE =
                    "/v1/bancoposta/associaContoWalletByCodiceDigital";
            public static final String ONBOARDING_VERIFICATION =
                    "/v1/bancoposta/verificaOnboarding";
            public static final String CHALLENGE = "/securetool/v1/challenge";
            public static final String AUTH_OPENID_AZ = "/federation/v2/openid-az";
            public static final String FETCH_SAVING_ACCOUNTS = "/v2/bancoposta/listaLibrettiH24";
            public static final String FETCH_SAVING_ACCOUNTS_DETAILS =
                    "/v2/bancoposta/listaMovimentiLibrettoH24";
            public static final String FETCH_SAVING_TRANSACTIONS =
                    "/v2/bancoposta/listaMovimentiLibrettoH24";
        }
    }

    public static class JWT {
        public static class Claims {
            public static final String APP_ID = "app_id";
            public static final String APP_REGISTER_ID = "appRegisterID";
            public static final String USER_PIN = "userPIN";
            public static final String OTP = "otp";
            public static final String TRANSACTION_CHALLENGE = "transaction-challenge";
            public static final String AUTHZ_TOOL = "authzTool";
            public static final String POSTEID = "posteid";
            public static final String SIGTYPE = "sigtype";
            public static final String JWS = "JWS";
            public static final String TRANSACTION_ID = "transactionID";
            public static final String SIGNED_CHALLENGE = "signed_challenge";
            public static final String APP_BPOL = "app-bpol";
            public static final String OTP_SPECS = "otp-specs";
            public static final String KID_SHA_256 = "kid-sha256";
            public static final String DATA = "data";
            public static final String TYPE = "type";
            public static final String HMAC_SHA_1 = "HMAC-SHA1";
            public static final String MOVING_FACTOR = "movingFactor";
            public static final String REGISTER_TOKEN = "registerToken";
            public static final String IDP_ACCESS_TOKEN = "idpAccessToken";
            public static final String ACTIVATION = "activation";
            public static final String USERNAME = "username";
            public static final String PASSWORD = "password";
            public static final String REGISTER = "register";
            public static final String XDEVICE = "xdevice";
            public static final String PUB_APP_KEY = "pubAppKey";
            public static final String INIT_CODE_VERIFIER = "initCodeVerifier";
            public static final String DEVICE_SPEC_ENCODED =
                    ":MDBmYzEzYWRmZjc4NTEyMmI0YWQyODgwOWEzNDIwOTgyMzQxMjQxNDIxMzQ4MDk3ODc4ZTU3N2M5OTFkZThmMA==:IOS:13.3.1:iPhone:13.65.22:true";
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
        public static final String REGISTRATION_SESSION_TOKEN = "registrationSessionToken";
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String ACCOUNT_ALIAS = "accountAlias";
        public static final String REGISTER_TOKEN = "registerToken";
        public static final String USER_PIN_SET_REQUIRED = "userPinSetRequired";
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
        public static final String STATE = "state";
        public static final String PROMPT = "prompt";
        public static final String NONCE = "nonce";
        public static final String JTI = "jti";
        public static final String ISS = "iss";
        public static final String AUD = "aud";
    }

    public static class FormValues {
        public static final String SCOPE =
                "userid surname name client_code taxcode sex birthdate birthnation_code birthprovince_code birthtown_code birthtown_description mobileNumber email prRoleMember";
        public static final String APP_NAME = "app-bpol";
        public static final String ACR_VALUES = "https://idp.poste.it/L1";
        public static final String SUB = "poste.it";
        public static final String GRANT_TYPE = "password";
        public static final String TOKEN = "token";
        public static final String OIDC_URL = "https://oidc-proxy.poste.it";
        public static final String IDP_URL = "https://idp-poste.poste.it";
        public static final String POSTE_URL_L2 = "https://idp.poste.it/L2";
        public static final String NONE_LOGIN = "none login";
        public static final String SIGNED_CHALLENGE = "signed_challenge";
        public static final String POSTE_ID = "posteID";
    }

    public static class ErrorCodes {
        public static final String MAX_DEVICES_LIMIT = "PIN-ERR-1";
        public static final String PIN_SET_REQUIRED = "DEVICE-ERR-2";
        public static final String INCORRECT_CREDENTIALS = "WS_CALL_ERROR";
    }
}
