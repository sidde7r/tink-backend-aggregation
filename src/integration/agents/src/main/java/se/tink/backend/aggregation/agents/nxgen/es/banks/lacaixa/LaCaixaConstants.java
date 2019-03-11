package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public class LaCaixaConstants {

    public static final String CURRENCY = "EUR";
    public static final TypeMapper<Instrument.Type> INSTRUMENT_TYPE_MAPPER =
            TypeMapper.<Instrument.Type>builder()
                    .put(Instrument.Type.STOCK, "10160", "15888", "15890")
                    .put(Instrument.Type.FUND, "10060")
                    .build();

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.SAVINGS, "LIBRETA")
                    .put(AccountTypes.CHECKING, "CUENTA CORRIENTE")
                    .build();

    public static class ApiService {
        static final String LOGIN_INIT_PATH = "login/loginInicio";
        static final String LOGIN_SUBMIT_PATH = "login/loginResultado";
        static final String LOGOUT_PATH = "login/logout";
        static final String MAIN_ACCOUNT_PATH = "dashboardApp/cuentaPrincipal?";
        static final String CHECK_FOTO_PATH = "smartContent/consultaFoto"; // Used for keep alive. TODO: Evaluate
        static final String USER_DATA_PATH = "login/loginDatosUsuario";
        static final String ACCOUNT_TRANSACTION_PATH = "cuentas/extracto?";
        static final String GENERIC_CARDS_PATH = "tarjetas/listadoTarjetasGenerica";
        static final String CARD_TRANSACTIONS_PATH = "tarjetasHCE/listaMovimientosGenerica";

        // engagements
        static final String ENGAGEMENTS_PATH = "posGlobal/posicionGlobalProductosAplicacion";
        // deposits, portfolio for stocks with list of instruments
        static final String DEPOSITS_LIST_PATH = "valores/posicionValores/lista";
        // instruments
        static final String DEPOSIT_DETAILS_PATH = "valores/depositosValores/detalle";
        // funds list
        static final String FUNDS_LIST_PATH = "fondos/posicionGlobalLista";
        // fund details
        static final String FUND_DETAILS_PATH = "fondos/detalleFondos";
        // loans
        static final String LOAN_LIST_PATH = "posGlobal/listaPrestamos";
        static final String LOAN_DETAILS_PATH = "prestamos/prestamodetalle";
    }

    public static class ErrorCode {
        public static String EMPTY_LIST = "ERR_TRXM01_007";
        public static String NO_SECURITIES = "131";
    }

    public static class ErrorMessage {
        public static final String NO_ASSOCIATED_ACCOUNT = "WITHOUT ASSOCIATED CONTRACTS/ACCOUNTS";
    }

    public static class Urls {
        private static final String BASE = "https://loapp.caixabank.es/xmlapps/rest/";

        public static final URL INIT_LOGIN = new URL(
                BASE + ApiService.LOGIN_INIT_PATH); // Gets session id. Needed before login.
        public static final URL SUBMIT_LOGIN = new URL(BASE + ApiService.LOGIN_SUBMIT_PATH);
        public static final URL LOGOUT = new URL(BASE + ApiService.LOGOUT_PATH);
        public static final URL FETCH_MAIN_ACCOUNT = new URL(BASE + ApiService.MAIN_ACCOUNT_PATH);
        public static final URL KEEP_ALIVE = new URL(BASE + ApiService.CHECK_FOTO_PATH);
        public static final URL FETCH_USER_DATA = new URL(BASE + ApiService.USER_DATA_PATH);
        public static final URL FETCH_ACCOUNT_TRANSACTION = new URL(BASE + ApiService.ACCOUNT_TRANSACTION_PATH);
        public static final URL FETCH_CARDS = new URL(BASE + ApiService.GENERIC_CARDS_PATH);
        public static final URL FETCH_CARD_TRANSACTIONS = new URL(BASE + ApiService.CARD_TRANSACTIONS_PATH);
        public static final URL FETCH_LOANS_LIST = new URL(BASE + ApiService.LOAN_LIST_PATH);
        public static final URL FETCH_LOANS_DETAILS = new URL(BASE + ApiService.LOAN_DETAILS_PATH);

        public static final URL FETCH_ENGAGEMENTS = new URL(BASE + ApiService.ENGAGEMENTS_PATH);
        public static final URL FETCH_DEPOSITS_LIST = new URL(BASE + ApiService.DEPOSITS_LIST_PATH);
        public static final URL FETCH_DEPOSIT_DETAILS = new URL(BASE + ApiService.DEPOSIT_DETAILS_PATH);
        public static final URL FETCH_FUNDS_LIST = new URL(BASE + ApiService.FUNDS_LIST_PATH);
        public static final URL FETCH_FUND_DETAILS = new URL(BASE + ApiService.FUND_DETAILS_PATH);
    }

    public static class DefaultRequestParams {
        public static final String LANGUAGE_EN = "en"; // English TODO: Language constants already exists somewhere?
        public static final String ORIGIN = "51402"; // Can seemingly be anything as long as it exists, purpose unknown.
        public static final String CHANNEL = "O"; // Only some valid values (1, 2, O, ...), purpose unknown.
        public static final String INSTALLATION_ID = "CIAPPLPh8,1XAEvy+IW9P82Pl+fvzwnfiAzzxs="; // App install ID?

        public static final int NUM_CARDS = 0;
        public static final String OPTION_FILTER = "02";
        public static final String PRODUCT_TYPE_FILTER = "T";
        public static final String STATUS_FILTER = "A";
        public static final String LIQUIDATION_FILTER = "S";
        public static final String ZERO_BALANCE_CONTRACTS = "N";
        public static final String GLOBAL_POSITION_TYPE_P = "P"; // only la caixa engagements
        public static final String GLOBAL_POSITION_TYPE_A = "A"; // la caixa and imagin engagemants
    }

    public static class QueryParams {
        public static final String FROM_BEGIN = "inicio";
        public static final String ACCOUNT_NUMBER = "numeroCuenta";
        public static final String DEPOSIT_ID = "idExpediente";
        public static final String DEPOSIT_CONTENT_ID = "idDeposito";
        public static final String ZERO_BALANCE_CONTRACTS = "contratosSaldoCero";
        public static final String GLOBAL_POSITION_TYPE = "tipoPosGlobal";
        public static final String MORE_DATA = "masDatos";
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

    public static class LogTags {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("lacaixa_unknown-accountype");
    }
}
