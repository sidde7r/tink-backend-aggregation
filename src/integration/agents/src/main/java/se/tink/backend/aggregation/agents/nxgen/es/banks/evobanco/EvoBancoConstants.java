package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco;

import se.tink.backend.aggregation.nxgen.http.URL;

public class EvoBancoConstants {

    public static class ApiService {
        static final String LOGIN_INIT_PATH = "login_be";
        static final String EE_LOGIN_PATH = "SOA_RVIA/Empresa/PS/rest/v3/SE_RVA_Login";
        static final String KEEP_ALIVE_PATH =
                "SOA_RVIA/Empresa/PS/rest/v1/SE_RVA_MantenimientoSesion";
        static final String GLOBAL_POSITION_FIRST_TIME_PATH =
                "SOA_RVIA/Empresa/PS/rest/v3/SE_RVA_PosicionGlobalPrimeraVezBE";
    }

    public static class Urls {
        private static final String BASE_API = "https://api.evobanco.com:8443/";
        private static final String BASE_MOBILE_SERVICES = "https://serviciosmoviles.evobanco.com/";

        public static final URL LOGIN = new URL(BASE_API + ApiService.LOGIN_INIT_PATH);
        public static final URL EE_LOGIN = new URL(BASE_MOBILE_SERVICES + ApiService.EE_LOGIN_PATH);
        public static final URL KEEP_ALIVE = new URL(BASE_MOBILE_SERVICES + ApiService.KEEP_ALIVE_PATH);
        public static final URL GLOBAL_POSITION_FIRST_TIME = new URL(BASE_MOBILE_SERVICES + ApiService.GLOBAL_POSITION_FIRST_TIME_PATH);
    }

    public static class StatusCodes {
        public static final int INCORRECT_USERNAME_PASSWORD = 400;
    }

    public static class QueryParamsKeys {
        public static final String AGREEMENT_BE = "acuerdoBE";
        public static final String USER_BE = "usuarioBE";
        public static final String ENTITY_CODE = "codigoEntidad";
        public static final String INTERNAL_ID_PE = "idInternoPe";
    }

    public static class HeaderKeys {
        public static final String COD_SEC_USER = "CODSecUser";
        public static final String COD_SEC_TRANS = "CODSecTrans";
        public static final String COD_SEC_ENT = "CODSecEnt";
        public static final String COD_TERMINAL = "CODTerminal";
        public static final String COD_SEC_IP = "CODSecIp";
        public static final String COD_CANAL = "CODCanal";
        public static final String COD_APL = "CODApl";
    }

    //TODO: Remove this as soon as we find out how to get these values from their backend
    public static class HeaderValues {
        public static final String COD_SEC_IP = "10.1.245.2";
        public static final String COD_CANAL = "18";
        public static final String COD_APL = "BDP";
    }

    //TODO: Remove this as soon as we find out how to get these values from their backend
    public static class RequestValues {
        public static final String OPERATING_SYSTEM = "IOS";
        public static final String DEVICE_ID = "5E4A4188-84C9-4F27-8397-8";
        public static final String APP_ID = "1";
        public static final String APP_VERSION = "12.12.1";
        public static final String MOBILE_ACCESS = "S";
        public static final String API_VERSION = "2";
    }

    public static class Storage {
        public static final String ACCESS_TOKEN = "access-token";
        public static final String USER_ID = "user-id";
        public static final String AGREEMENT_BE = "agreement-be";
        public static final String USER_BE = "user-be";
        public static final String ENTITY_CODE = "entity-code";
        public static final String COD_SEC_IP = HeaderKeys.COD_SEC_IP;
        public static final String INTERNAL_ID_PE = "internal-id-pe";
    }

    public static class FormKey {
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
    }

    public static class UrlParams {
        public static final String UID = "uid";
    }
}
