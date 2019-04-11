package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco;

import java.time.format.DateTimeFormatter;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public class EvoBancoConstants {

    public static class ApiService {
        static final String LOGIN_INIT_PATH = "login_be";
        static final String EE_LOGIN_PATH = "SOA_RVIA/Empresa/PS/rest/v3/SE_RVA_Login";
        static final String KEEP_ALIVE_PATH =
                "SOA_RVIA/Empresa/PS/rest/v1/SE_RVA_MantenimientoSesion";
        static final String GLOBAL_POSITION_FIRST_TIME_PATH =
                "SOA_RVIA/Empresa/PS/rest/v3/SE_RVA_PosicionGlobalPrimeraVezBE";
        static final String FETCH_TRANSACTIONS_PATH =
                "SOA_RVIA/Empresa/PS/rest/v1/SE_RVA_ConsultaMovimientosVistaAplazada";
        static final String FETCH_ACCOUNTS_PATH =
                "SOA_RVIA/Empresa/PS/rest/v3/SE_RVA_PosicionGlobalBE";
        static final String LINKING_LOGIN_PATH =
                "SOA_RVIA/Empresa/PS/rest/v3/SE_RVA_VinculacionyLogin";
        static final String FETCH_CARD_TRANSACTIONS_PATH =
                "SOA_RVIA/Empresa/PS/rest/v1/SE_RVA_ConsultaMovimientosTarjetaBE";
        static final String INVESTMENTS_PATH = "EVO_PAI/v1/api/investments";
    }

    public static class Urls {
        private static final String BASE_API = "https://api.evobanco.com:8443/";
        private static final String BASE_MOBILE_SERVICES = "https://serviciosmoviles.evobanco.com/";

        public static final URL LOGIN = new URL(BASE_API + ApiService.LOGIN_INIT_PATH);
        public static final URL EE_LOGIN = new URL(BASE_MOBILE_SERVICES + ApiService.EE_LOGIN_PATH);
        public static final URL KEEP_ALIVE =
                new URL(BASE_MOBILE_SERVICES + ApiService.KEEP_ALIVE_PATH);
        public static final URL GLOBAL_POSITION_FIRST_TIME =
                new URL(BASE_MOBILE_SERVICES + ApiService.GLOBAL_POSITION_FIRST_TIME_PATH);
        public static final URL FETCH_TRANSACTIONS =
                new URL(BASE_MOBILE_SERVICES + ApiService.FETCH_TRANSACTIONS_PATH);
        public static final URL FETCH_ACCOUNTS =
                new URL(BASE_MOBILE_SERVICES + ApiService.FETCH_ACCOUNTS_PATH);
        public static final URL LINKING_LOGIN =
                new URL(BASE_MOBILE_SERVICES + ApiService.LINKING_LOGIN_PATH);
        public static final URL FETCH_CARD_TRANSACTIONS =
                new URL(BASE_MOBILE_SERVICES + ApiService.FETCH_CARD_TRANSACTIONS_PATH);
        public static final URL FETCH_INVESTMENTS =
                new URL(BASE_MOBILE_SERVICES + ApiService.INVESTMENTS_PATH);
    }

    public static class StatusCodes {
        public static final int BAD_REQUEST_STATUS_CODE = 400;
    }

    public static class ReturnCodes {
        public static final String UNSUCCESSFUL_RETURN_CODE = "0";
    }

    public static class ErrorCodes {
        public static final String NO_TRANSACTIONS_FOUND = "00350";
        public static final String AUTHENTICATION_ERROR = "1500";
    }

    public static class QueryParamsKeys {
        public static final String AGREEMENT_BE = "acuerdoBE";
        public static final String USER_BE = "usuarioBE";
        public static final String ENTITY_CODE = "codigoEntidad";
        public static final String INTERNAL_ID_PE = "idInternoPe";
        public static final String PAGE_NUM = "numeroLlamadas";
        public static final String CARD_NUMBER = "panToken";
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

    // TODO: Remove this as soon as we find out how to get these values from their backend
    public static class HeaderValues {
        public static final String COD_SEC_IP = "10.1.245.2";
        public static final String COD_CANAL = "18";
        public static final String COD_APL = "BDP";
    }

    // TODO: Remove this as soon as we find out how to get these values from their backend
    public static class HardCodedValues {
        public static final String OPERATING_SYSTEM = "IOS";
        public static final String APP_ID = "1";
        public static final String APP_VERSION = "12.12.1";
        public static final String MOBILE_ACCESS = "S";
        public static final String API_VERSION = "2";
        public static final String MODEL = "iPhone 6";
        public static final String FIRST_LINKING_SIGNATURE = "N";
        public static final String SECOND_LINKING_SIGNATURE = "V";
    }

    public static class Storage {
        public static final String ACCESS_TOKEN = "access-token";
        public static final String USER_ID = "user-id";
        public static final String AGREEMENT_BE = "agreement-be";
        public static final String USER_BE = "user-be";
        public static final String ENTITY_CODE = "entity-code";
        public static final String COD_SEC_IP = HeaderKeys.COD_SEC_IP;
        public static final String INTERNAL_ID_PE = "internal-id-pe";
        public static final String HOLDER_NAME = "holder-name";
        public static final String DEVICE_ID = "device-id";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
    }

    public static class FormKey {
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "I#Cuenta Inteligente EVO")
                    .put(AccountTypes.SAVINGS, "I#Depósito")
                    .build();

    public static final TypeMapper<String> CURRENCY_TYPE_MAPPER =
            TypeMapper.<String>builder().put("978", "EUR").build();

    public static class LogTags {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE =
                LogTag.from("evobanco_unknown-accountype");
        public static final LogTag UNEXPECTED_SESSION_EXCEPTION =
                LogTag.from("evobanco_unexpected-session-exception");
        public static final LogTag NO_TRANSACTIONS_FOUND =
                LogTag.from("evobanco_no-transactions-found");
    }

    public static class Constants {
        public static final String FIRST_SEQUENTIAL_NUMBER = "0000001";
        public static final String MADRID_ZONE_ID = "Europe/Madrid";
        public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
        public static final DateTimeFormatter DATE_TIME_FORMATTER =
                DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        public static final int OTP_VALUE_LENGTH = 7;
        public static final int DEVICE_ID_LENGTH = 25;
        public static final String CREDIT_TRANSACTION_TYPE = "1";
        public static final String ACCOUNT_TRANSACTION_PLUS_SIGN = "H";
    }

    public static class Tags {
        public static final LogTag INVESTMENTS_ERROR = LogTag.from("es_evobanco_investments_error");
        public static final LogTag INVESTMENTS = LogTag.from("es_evobanco_investments");
    }
}
