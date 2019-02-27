package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco;

import se.tink.backend.aggregation.nxgen.http.URL;

public class EvoBancoConstants {

    public static class ApiService {
        static final String LOGIN_INIT_PATH = "login_be";
        static final String LOGOUT_PATH = "SOA_RVIA/Empresa/PS/rest/v1/SE_RVA_Logout";
        static final String KEEP_ALIVE_PATH =
                "evobanco/payment-methods/bizum/v1/api/user/validate/{" + UrlParams.UID + "}";
    }

    public static class Urls {
        private static final String BASE_API = "https://api.evobanco.com:8443/";
        private static final String BASE_MOBILE_SERVICES = "https://serviciosmoviles.evobanco.com/";

        public static final URL LOGIN = new URL(BASE_API + ApiService.LOGIN_INIT_PATH);
        public static final URL LOGOUT = new URL(BASE_MOBILE_SERVICES + ApiService.LOGOUT_PATH);
        public static final URL KEEP_ALIVE = new URL(BASE_API + ApiService.KEEP_ALIVE_PATH);
    }

    public static class StatusCodes {
        public static final int INCORRECT_USERNAME_PASSWORD = 400;
    }

    public static class QueryParams {
        public static final String AGREEMENT_BE = "acuerdoBE";
        public static final String ENTITY_CODE = "codigoEntidad";
        public static final String USER_BE = "usuarioBE";
    }

    public static class Storage {
        public static final String ACCESS_TOKEN = "access-token";
        public static final String USER_ID = "user-id";
    }

    public static class FormKey {
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
    }

    public static class UrlParams {
        public static final String UID = "uid";
    }
}
