package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco;

import java.util.UUID;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NovoBancoConstants {

    public static class URLs {
        public static final URL LOGIN = new URL(Endpoints.LOGIN0);
        public static final URL KEEP_ALIVE = new URL(Endpoints.GET_ACCOUNTS);
        public static final URL GET_ACCOUNTS = new URL(Endpoints.GET_ACCOUNTS);
        public static final URL GET_ALL_TRANSACTIONS = new URL(Endpoints.GET_ALL_TRANSACTIONS);
        public static final URL GET_DETAILS = new URL(Endpoints.GET_DETAILS);
        public static final URL GET_LOANS = new URL(Endpoints.GET_LOANS);
        public static final URL GET_LOAN_DETAILS = new URL(Endpoints.GET_LOAN_DETAILS);
    }

    private static class Endpoints {
        public static final String API_BASE = "https://sec.novobanco.pt/";
        public static final String LOGIN0 = API_BASE + "mv2/api2/Security/Login0";
        public static final String GET_ACCOUNTS = API_BASE + "mv2/api2/Movimentos/ObterLista";
        public static final String GET_ALL_TRANSACTIONS = API_BASE + "mv2/api2/Movimentos/ObterMais";
        public static final String GET_DETAILS = API_BASE + "mv2/api2/PosicaoIntegrada/Obter";
        public static final String GET_LOANS = API_BASE + "mv2/api2/PosicaoIntegrada/ObterSeccao";
        public static final String GET_LOAN_DETAILS = API_BASE + "mv2/api2/PosicaoIntegrada/ObterDetalheCreditoHabitacao";
    }

    public static class Secrets {
        public static final String API_KEY = "0102206970E0B44A5A960BD304A95E7FD3";
        // generated  key, encoded to B64 and converted to string (US-ASCII charset + trim)
        public static final String INSTANCE_KEY = "zT/upIt3YlRJSteHtdF3FaeMOzKea0+y5j0qiD0VMS0=";
    }

    public static class SessionKeys {
        public static final String AUTH_COOKIE_KEY = "AUTH_COOKIE";
        public static final String SESSION_COOKIE_KEY = "SESSION_COOKIE";
        public static final String DEVICE_ID_KEY = "DEVICE_ID";
        public static final String ACCOUNT_GENERAL_INFO_ID = "ACCOUNT_GENERAL_INFO";
        public static final String PAGING_TOKEN = "PAGING_TOKEN";
        public static final String OP_TOKEN = "OP_TOKEN";
    }

    public static class ServiceIds {
        public static final int MOVEMENTS_ID = 3396;
        public static final int LOANS_ID = 194;
    }

    public static class Header {
        public static final String CONNECTION_KEY = "Connection";
        public static final String CONNECTION_VALUE = "keep-alive";

        public static final String ENCODING_VALUE = "br, gzip, deflate";

        public static final String USER_AGENT_KEY = "User-Agent";
        public static final String USER_AGENT_VALUE = "NovoMobile/3.4.5 (iPhone; iOS 12.4; Scale/2.00)";

        public static final String NB_SIGNATURE_KEY = "X-NB-Signature";
    }

    public static class FieldValues {
        public static final String OS = "iOS";
        public static final String MODEL = "iPhone 8";
        public static final String OS_VERSION = "12.4";
        public static final double LATITUDE = 52.22267804387367936;
        public static final double LONGITUDE = 21.01073071171693824;
        public static final String DEVICE_NAME = "Tink";
        public static final String DEFAULT_DEVICE_ID = UUID.randomUUID().toString().toUpperCase();
        public static final String REQUEST_ID = UUID.randomUUID().toString().toUpperCase();
        public static final String LANGUAGE = "PT";
        public static final int LOGIN_MODE = 1;
        public static final String CTX_ACCOUNTS = "contas";
        public static final int LOANS_SECTION_TYPE = 15;


        public static final String APP_VERSION = "3.4.5";
        public static final String APP_BUILD = "5466";
    }

    public static class ResponseCodes {
        public static final int INVALID_LOGIN = 50;
        public static final int SESSION_EXPIRED = 40;
        public static final int OK = 0;
    }
}
