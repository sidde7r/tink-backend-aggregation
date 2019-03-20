package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SantanderEsConstants {

    public static final class Urls {
        public static final String HOST = "https://www.bsan.mobi";

        public static final URL AUTHENTICATION_ENDPOINT =
                new URL(HOST + "/SANMOV_IPAD_NSeg_ENS/ws/SANMOV_Def_Listener");
        public static final URL WEB_SERVICE_ENDPOINT =
                new URL(HOST + "/SCH_BAMOBI_WS_ENS/ws/BAMOBI_WS_Def_Listener");
        public static final URL FUNDS_ENDPOINT =
                new URL(HOST + "/SCH_BAMOBI_FONDOS_ENS/ws/BAMOBI_WS_Def_Listener");
        public static final URL STOCKS_ENDPOINT =
                new URL(HOST + "/SCH_BAMOBI_VALORES_ENS/ws/BAMOBI_WS_Def_Listener");
        public static final URL LOANS_ENDPOINT =
                new URL(HOST + "/SCH_BAMOBI_PRESTAMOS_ENS/ws/BAMOBI_WS_Def_Listener");
    }

    public static final class Headers {
        public static final String SOAP_ACTION = "SOAPAction";
        public static final String TEXT_XML_UTF8 = "text/xml; charset=utf-8";
    }

    public static final class NodeTags {
        public static final String METHOD_RESULT = "methodResult";
        public static final String TOKEN_CREDENTIAL = "tokenCredential";
        public static final String CODIGO_ERROR = "codigoError";
        public static final String FAULT_ERROR = "error";
    }

    public static final class ErrorCodes {
        public static final String INCORRECT_CREDENTIALS = "SBAMOV_00002";
    }

    public static final class Storage {
        public static final String LOGIN_RESPONSE = "loginResponse";
        public static final String USER_DATA_XML = "userDataXml";
        public static final String CONTRACT_ID_XML = "contractIdXml";
        public static final String BALANCE_XML = "balanceXml";
        public static final String CARD_ENTITY = "cardEntity";
    }

    public static final class DataHeader {
        public static final String VERSION = "5.1.2";
        public static final String TERMINAL_ID = "iPhone";
        public static final String IDIOMA = "es-ES";
    }

    public static final class AccountTypes {
        public static final String PROD_NR_300 = "300";
        public static final String PROD_NR_301 = "301";
        public static final String DEBIT_CARD_TYPE = "débito";
    }

    public static final class Tags {
        public static final LogTag UNKNOWN_ENGAGEMENT_TYPE =
                LogTag.from("es_santander_unknown_engagement_type");
        public static final LogTag UNKNOWN_ACCOUNT_TYPE =
                LogTag.from("es_santander_unknown_account_type");
        public static final LogTag CREDIT_CARD_ACCOUNT =
                LogTag.from("es_santander_credit_card_account_log");
        public static final LogTag LOAN_ACCOUNT = LogTag.from("es_santander_loan");
        public static final LogTag INVESTMENT_ACCOUNT = LogTag.from("es_santander_investment");
        public static final LogTag CREDIT_CARD_TRANSACTION =
                LogTag.from("es_santander_credit_card_transaction");
        public static final LogTag ES_SANTANDER_ALL = LogTag.from("es_santander_all");
    }

    public static final class LogMessages {
        public static final String LOGIN_RESPONSE_NOT_FOUND =
                "Login response not found in session storage";
        // We have problems with parsing some data that we used to consider KNOWN_NODES
        // So we temporary want to log all the data
        public static final Set<String> KNOWN_NODES = Collections.emptySet();
        /*Sets.newHashSet(
        "info",
        "datosUsuario",
        "cuentas",
        "fondos",
        "prestamos",
        "tarjetas",
        "nombreCliente",
        "indMigradoSEPA",
        "indMigradoDeBanesto",
        "indActividadEstadoFirma",
        "indFaseEstadoFirma",
        "indOperatividadUsuario",
        "indTipoConsultivo",
        "fechaNacimientoCliente",
        "nombrePersonaSinApellidos",
        "apellidoUno",
        "apellidoDos",
        "estados");*/

    }

    public static final class Indicators {
        public static final String ERROR_CODE_OK = "0";
        public static final String YES = "S";
        public static final String NO = "N";
    }

    public static final class SoapErrorMessages {
        public static final String NO_MORE_TRANSACTIONS =
                "no existen movimientos asociados a esta consulta";
        public static final String NO_MOVEMENTS_IN_INTERVAL =
                "no hay movimientos en el intervalo indicado";
        public static final String NO_PERMISSION_TO_PERFORM_OPERATION =
                "no dispone de los permisos necesarios para realizar esta operación";
        public static final String PROVIDE_VALID_INITIAL_DATE =
                "por favor, introduzca una fecha inicial válida";
    }
}
