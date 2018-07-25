package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;

public class LaCaixaConstants {

    public static class ApiService {
        public static final String LOGIN_INIT_PATH = "login/loginInicio";
        public static final String LOGIN_SUBMIT_PATH = "login/loginResultado";
        public static final String LOGOUT_PATH= "login/logout";
        public static final String MAIN_ACCOUNT_PATH = "dashboardApp/cuentaPrincipal?";
        public static final String CHECK_FOTO_PATH = "smartContent/consultaFoto"; // Used for keep alive. TODO: Evaluate
        public static final String USER_DATA_PATH = "login/loginDatosUsuario";
        public static final String ACCOUNT_TRANSACTION_PATH = "cuentas/extracto?";
        public static final String TRANSACTION_DETAILS_PATH = "cuentas/detalleMovimientoExtracto?";
        public static final String CREDIT_CARDS_PATH = "tarjetas/listadoTarjetasGenerica";
    }

    public static class Urls {
        private static final String BASE = "https://loapp.caixabank.es/xmlapps/rest/";

        public static final URL INIT_LOGIN = new URL(BASE + ApiService.LOGIN_INIT_PATH); // Gets session id. Needed before login.
        public static final URL SUBMIT_LOGIN = new URL(BASE + ApiService.LOGIN_SUBMIT_PATH);
        public static final URL LOGOUT = new URL(BASE + ApiService.LOGOUT_PATH);
        public static final URL FETCH_MAIN_ACCOUNT = new URL(BASE + ApiService.MAIN_ACCOUNT_PATH);
        public static final URL KEEP_ALIVE = new URL(BASE + ApiService.CHECK_FOTO_PATH);
        public static final URL FETCH_USER_DATA = new URL(BASE + ApiService.USER_DATA_PATH);
        public static final URL FETCH_ACCOUNT_TRANSACTION = new URL(BASE + ApiService.ACCOUNT_TRANSACTION_PATH);
        public static final URL FETCH_TRANSACTION_DETAILS = new URL(BASE + ApiService.TRANSACTION_DETAILS_PATH);
        public static final URL FETCH_CREDIT_CARDS = new URL(BASE + ApiService.CREDIT_CARDS_PATH);
    }

    public static class DefaultRequestParams {
        public static final String LANGUAGE_EN = "en"; // English TODO: Language constants already exists somewhere?
        public static final String ORIGIN = "4024"; // Can seemingly be anything as long as it exists, purpose unknown.
        public static final String CHANNEL = "O"; // Only some valid values (1, 2, O, ...), purpose unknown.
        public static final String INSTALLATION_ID = "CIAPPLPh7,2CakrHGsSyjX1nakKcEk6dOc3gHc="; // App install ID?

        public static final int NUM_CARDS = 0;
        public static final String OPTION_FILTER = "02";
        public static final String PRODUCT_TYPE_FILTER = "T";
        public static final String STATUS_FILTER = "A";
        public static final String LIQUIDATION_FILTER = "S";
    }

    public static class QueryParams {
        public static final String FROM_BEGIN = "inicio";
        public static final String ACCOUNT_NUMBER = "numeroCuenta";
        public static final String ACCOUNT_REFERENCE = "refValCuenta";
        public static final String TRANSACTION_DETAILS_CONSULTACOM = "refValConsultaCom";
        public static final String TRANSACTION_DETAILS_COMMUNICADOS = "indComunicados";
        public static final String TRANSACTION_DETAILS_ACCESODETALLEMOV = "refValAccesoDetalleMov";

    }

    public static class TemporaryStorage {
        public static final String ACCOUNT_REFERENCE = "accountRef";
    }

    public static class StatusCodes {
        public static final int INCORRECT_USERNAME_PASSWORD = 409; // Conflict
    }

    public static class UserData {
        public static final String FULL_HOLDER_NAME = "linkNombreEmp";
    }

    public static class TransactionDescriptions {
        public static final String TRANSFER = "TRANSFER";
    }

    public static class TransactionDetailsInfoKeys {
        public static final String TRANSFER_MESSAGE = "Concepto transferencia";
    }

    public static class LogTags {
        public static final LogTag CREDIT_CARDS = LogTag.from("es_lacaixa_creditcards");
    }
}
