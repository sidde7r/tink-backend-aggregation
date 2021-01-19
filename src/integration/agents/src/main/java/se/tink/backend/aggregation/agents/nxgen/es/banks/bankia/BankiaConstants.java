package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia;

import java.time.ZoneId;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public abstract class BankiaConstants {

    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Madrid");
    public static final String PROVIDER_NAME = "es-bankia-password";
    public static final String MARKET = "ES";
    public static final String LANGUAGE = "en";
    public static final String URL_BASE_OIP = "https://oip.bankia.es";

    public static class Url {
        public static final String LOGIN =
                URL_BASE_OIP + "/api/1.0/escenario/escenarioaplicacion/login";
        public static final String LOGIN_KEY = URL_BASE_OIP + "/api/1.0/login/key";
        public static final String DISCONNECT = URL_BASE_OIP + "/es/desconectar";
        public static final String SERVICES_CONTRACTS =
                URL_BASE_OIP + "/api/1.0/servicios/contratos/6.0/contratos";
        public static final String SERVICES_ACCOUNT_MOVEMENT =
                URL_BASE_OIP + "/api/1.0/servicios/cuenta.movimiento/3.0/cuenta/movimiento";
        public static final String CREDIT_CARD_TRANSACTIONS =
                URL_BASE_OIP + "/api/1.0/operativas/2.0/tarjetas/movimientos";
        public static final String VALUE_ACCOUNT_POSITION_WALLET =
                URL_BASE_OIP + "/api/microservicios/1.0/valores/posicionCartera";

        public static final String IDENTITY_DATA =
                URL_BASE_OIP + "/api/1.0/servicios/cliente.datos/5.0/cliente/datos";
        public static final String CUSTOMER_SCENARIO =
                URL_BASE_OIP
                        + "/api/1.0/servicios/contexto.escenariocliente/5.0/contexto/escenariocliente";
    }

    public static class Query {
        public static final String CM_FORCED_DEVICE_TYPE = "cm-forced-device-type";
        public static final String OIGID = "oigid";
        public static final String J_GID_COD_APP = "j_gid_cod_app";
        public static final String J_GID_COD_DS = "j_gid_cod_ds";
        public static final String ORIGEN = "origen";
        public static final String X_J_GID_COD_APP = "x-j_gid_cod_app";
        public static final String ID_VISTA = "idVista";
        public static final String GROUP_BY_FAMILIA = "groupByFamilia";
    }

    public static class Form {
        public static final String J_GID_ID_DISPOSITIVO = "j_gid_id_dispositivo";
        public static final String ID_DISPOSITIVO = "idDispositivo";
        public static final String J_GID_COD_APP = "j_gid_cod_app";
        public static final String J_GID_COD_DS = "j_gid_cod_ds";
        public static final String J_GID_ACTION = "j_gid_action";
        public static final String J_GID_PASSWORD = "j_gid_password";
        public static final String CONTRASENA = "contrasena";
        public static final String EVENT_ID = "_eventId";
        public static final String ORIGEN = "origen";
        public static final String J_GID_NUM_DOCUMENTO = "j_gid_num_documento";
        public static final String IDENTIFICADOR = "identificador";
        public static final String EXECUTION = "execution";
        public static final String TIPO = "tipo";
        public static final String VERSION = "version";
    }

    public static class Default {
        public static final String JSON = "json";
        public static final String _5_0 = "5.0";
        public static final String ANDROID_PHONE = "android-phone";
        public static final String LOWER_CASE_AM = "am";
        public static final String UPPER_CASE_AM = "AM";
        public static final String LOWER_CASE_OIP = "oip";
        public static final String UPPER_CASE_OIP = "OIP";
        public static final String TRUE = "true";
        public static final String OM = "OM";
        public static final String COMPROBAR_IDENTIFICACION = "comprobarIdentificacion";
        public static final String LOGIN = "login";
        public static final String OMP = "omp";
        public static final String O3 = "o3";
        public static final String _1 = "1";
        public static final String EMPTY_RESUME_POINT = "";
        public static final String ACCEPT_LANGUAGE = "es";
        public static final String EMPTY_EXECUTION_STRING = "";
    }

    public static class StorageKey {
        public static final String DEVICE_ID_BASE_64 = "DEVICE_ID_BASE64";
        public static final String DEVICE_ID_BASE_64_URL = "DEVICE_ID_BASE64_URL";
        public static final String COUNTRY = "country";
        public static final String CONTROL_DIGITS = "control_digits";
    }

    public static final TypeMapper<Type> LOAN_TYPE_MAPPER =
            TypeMapper.<LoanDetails.Type>builder()
                    .put(LoanDetails.Type.MORTGAGE, "13157", "13135")
                    .put(LoanDetails.Type.BLANCO, "10060") // CREDITO INMEDIATO
                    .build();

    public static final TypeMapper<AccountTypes> INVESTMENT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.INVESTMENT, "31000")
                    .put(AccountTypes.OTHER, "11658")
                    .build();

    public static final TransactionalAccountTypeMapper PSD2_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "10300",
                            "10600",
                            "11186",
                            "11239",
                            "11355",
                            "11359", // Non-adult account
                            "11590",
                            "11594",
                            "11660")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "10450",
                            "11403")
                    .ignoreKeys("11658")
                    .build();

    public static class CardTypes {
        public static final String CREDIT_CARD = "C";
    }

    public static class Loans {
        public static final String DEBTOR_CODE = "010";
    }

    public static class InstrumentTypes {
        public static final String STOCK = "01"; // Type description (in Spanish): ACCIONES
    }

    public static class Errors {
        public static final int LOGIN_ERROR = 428;
        public static final String NO_DATA_FOR_ENQUIRY_CODE = "VAVC003E";
        public static final String NO_DATA_FOR_ENQUIRY_MESSAGE =
                "There is no data for the request.";
        public static final String WRONG_CREDENTIALS = "Wrong credentials";

        public static final String UNKNOWN_LOGIN_ERROR = "Unknown login error";

        public static final TypeMapper<String> ERROR_MAPPER =
                TypeMapper.<String>builder()
                        .put(WRONG_CREDENTIALS, "CM04110E", "CM04111E")
                        .put(UNKNOWN_LOGIN_ERROR, "AQ99999E", "CM04150E")
                        .build();
    }

    public static class Logging {
        public static final LogTag UNKNOWN_TRANSACTIONAL_ACCOUNT_TYPE =
                LogTag.from("bankia_unknown_transactional_account_type");
        public static final LogTag UNKNOWN_INVESTMENT_ACCOUNT_TYPE =
                LogTag.from("bankia_unknown_investment_account_type");
        public static final LogTag LOAN = LogTag.from("bankia_loan");
        public static final LogTag UNKNOWN_INSTRUMENT_TYPE =
                LogTag.from("bankia_unknown_instrument_type");
        public static final LogTag INSTRUMENT_FETCHING_ERROR =
                LogTag.from("bankia_error_instrument_fetch");
    }

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
