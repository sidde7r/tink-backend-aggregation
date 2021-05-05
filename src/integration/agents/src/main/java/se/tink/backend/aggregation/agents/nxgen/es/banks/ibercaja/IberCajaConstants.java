package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class IberCajaConstants {

    public static class ApiService {

        static final String LOGIN_INIT_PATH = "usuarios/iniciarsesion";
        static final String LOGIN = "pfm/login";
        static final String MAIN_ACCOUNT_PATH = "misproductos/productos";
        static final String ACCOUNT_TRANSACTION_PATH = "cuentas/movimientos";
        static final String CREDIT_CARD_ACCOUNT_PATH = "tarjetas/movimientos";
        static final String INVESTMENT_ACCOUNT_PATH = "valores";
        static final String CHECK_SAVINGS_ACCOUNT_PATH = "valores/cuentasvalores";
    }

    public static class Urls {

        private static final String BASE = "https://ewm.ibercajadirecto.com/api/";
        private static final String KEEPALIVE = "https://ewm.ibercajadirecto.com//api/";

        public static final URL INIT_LOGIN = new URL(BASE + ApiService.LOGIN_INIT_PATH);
        public static final URL LOGIN = new URL(BASE + ApiService.LOGIN);
        public static final URL FETCH_MAIN_ACCOUNT = new URL(BASE + ApiService.MAIN_ACCOUNT_PATH);
        public static final URL FETCH_ACCOUNT_TRANSACTION =
                new URL(BASE + ApiService.ACCOUNT_TRANSACTION_PATH);
        public static final URL FETCH_CREDIT_CARD_ACCOUNT =
                new URL(BASE + ApiService.CREDIT_CARD_ACCOUNT_PATH);
        public static final URL FETCH_INVESTMENT_ACCOUNT_TRANSACTION =
                new URL(BASE + ApiService.INVESTMENT_ACCOUNT_PATH);
        public static final URL KEEP_ALIVE =
                new URL(KEEPALIVE + ApiService.CHECK_SAVINGS_ACCOUNT_PATH);
    }

    public static class DefaultRequestParams {

        public static final boolean CARD = false;
        public static final String LAST_ACCESS =
                "2018-10-25T00:00:00+02:00"; // Maybe can be anything?
        public static final String REQUEST_ORDER = "1";
        public static final String REQUEST_TYPE = "2";
        public static final String PLAYBACK_MODE_REAL = "Real";
        public static final String IS_SPECIALIST = "False";
        public static final String APP_ID = "IbercajaAppV2Piloto";
        public static final String VERSION = "2.25.0";
        public static final String CHANNEL_MOBILE = "MOV";
        public static final String DEVICE = "IOSP";
        public static final String ACCESS_CARD = "False";
    }

    public static class Headers {

        public static final String USER = "Usuario";
        public static final String TICKET = "Ticket";
        public static final String PLAYBACK_MODE = "PlayBackMode";
        public static final String APP_ID = "AppID";
        public static final String VERSION = "version";
        public static final String CHANNEL = "Canal";
        public static final String DEVICE = "Dispositivo";
        public static final String ACCESS_CARD = "AccesoTarjeta";
        public static final String NICI = "NICI";
        public static final String NIP = "nip";
        public static final String TOKEN_IDENTITY = "TokenIdentity";
        public static final String CONTRACT = "contrato";
        public static final String CONTENT_TYPE = "Content-Type";
    }

    public static class QueryParams {

        public static final String ACCOUNT = "cuenta";
        public static final String REQUEST_ACCOUNT = "request.cuenta";
        public static final String REQUEST_START_DATE = "request.fechaInicio";
        public static final String REQUEST_END_DATE = "request.fechaFin";
        public static final String REQUEST_IS_SPECIALIIST = "esEspeciallista";
        public static final String REQUEST_CARD = "request.tarjeta";
        public static final String REQUEST_ORDER = "request.orden";
        public static final String REQUEST_CARD_TYPE = "request.tipoTarjeta";
    }

    public static class ErrorCodes {
        public static final int INCORRECT_USERNAME_PASSWORD = 1025;
    }

    public static class Storage {

        public static final String TICKET = "TICKET";
        public static final String USERNAME = "USER";
        public static final String FULL_NAME = "FULL_NAME";
        public static final String DOCUMENT_NUMBER = "DOCUMENT_NUMBER";
        public static final String NICI = "NICI";
        public static final String CONTRACT = "CONTRACT";
        public static final String NIP = "NIP";
        public static final String TOKEN_IDENTITY = "TOKEN_IDENTITY";
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "0")
                    .put(AccountTypes.CREDIT_CARD, "1")
                    .put(AccountTypes.INVESTMENT, "6")
                    .build();

    public static final TypeMapper<AccountTypes> CARD_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CREDIT_CARD, "2")
                    .ignoreKeys("1") // 1 is debit card
                    .build();
}
