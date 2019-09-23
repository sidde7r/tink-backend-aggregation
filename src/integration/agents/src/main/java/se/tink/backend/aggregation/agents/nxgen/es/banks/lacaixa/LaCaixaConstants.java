package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.account.enums.AccountFlag;

public class LaCaixaConstants {

    public static final String CURRENCY = "EUR";
    public static final TypeMapper<Instrument.Type> INSTRUMENT_TYPE_MAPPER =
            TypeMapper.<Instrument.Type>builder()
                    .put(Instrument.Type.STOCK, "10160", "15888", "15890")
                    .put(Instrument.Type.FUND, "10060")
                    .build();

    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.SAVINGS, AccountFlag.PSD2_PAYMENT_ACCOUNT, "LIBRETA")
                    .put(
                            AccountTypes.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "CUENTA CORRIENTE")
                    .build();

    public static final TypeMapper<LoanDetails.Type> LOAN_TYPE_MAPPER =
            TypeMapper.<LoanDetails.Type>builder()
                    .put(LoanDetails.Type.MORTGAGE, "10045")
                    .put(
                            LoanDetails.Type.BLANCO,
                            "12142", // personal
                            "11410", // online
                            "21919", // "canal"
                            "14090", // micro personal
                            "11407", // consumer loan
                            "10097") // consumer loan
                    .build();

    public static class LoanTypeName {
        public static final String MORTGAGE = "PRS";
        public static final String CONSUMER_LOAN = "CRV";
    }

    public static class ErrorCode {
        public static String EMPTY_LIST = "ERR_TRXM01_007";
        public static String NO_SECURITIES = "131";
        public static final String NO_OWN_CARDS = "LIT_NO_PERMITE_CONSULTA_TARJETAS";
    }

    public static class ErrorMessage {
        public static final String NO_ASSOCIATED_ACCOUNT = "WITHOUT ASSOCIATED CONTRACTS/ACCOUNTS";
    }

    public static class Urls {
        private static final String BASE = "https://loapp.caixabank.es/xmlapps/rest/";

        public static final URL INIT_LOGIN =
                new URL(BASE + "login/loginInicio"); // Gets session id. Needed before login.
        public static final URL SUBMIT_LOGIN = new URL(BASE + "login/loginResultado");
        public static final URL LOGOUT = new URL(BASE + "login/logout");
        public static final URL KEEP_ALIVE = new URL(BASE + "smartContent/consultaFoto");

        public static final URL MAIN_ACCOUNT = new URL(BASE + "dashboardApp/cuentaPrincipal?");
        public static final URL ACCOUNT_TRANSACTIONS = new URL(BASE + "cuentas/extracto?");

        public static final URL CARDS = new URL(BASE + "tarjetas/listadoTarjetasGenerica");
        public static final URL CARD_TRANSACTIONS =
                new URL(BASE + "tarjetasHCE/listaMovimientosGenerica");
        public static final URL CARD_LIQUIDATIONS = new URL(BASE + "tarjetas/listaLiquidaciones");
        public static final URL CARD_LIQUIDATION_DETAILS =
                new URL(BASE + "tarjetas/detalleLiquidacion");

        public static final URL LOAN_LIST = new URL(BASE + "posGlobal/listaPrestamos");
        public static final URL MORTGAGE_DETAILS = new URL(BASE + "prestamos/prestamodetalle");
        public static final URL CONSUMER_LOAN_DETAILS = new URL(BASE + "prestamos/creditodetalle");

        public static final URL ENGAGEMENTS =
                new URL(BASE + "posGlobal/posicionGlobalProductosAplicacion");
        public static final URL DEPOSIT_LIST = new URL(BASE + "valores/posicionValores/lista");
        public static final URL DEPOSIT_DETAILS =
                new URL(BASE + "valores/depositosValores/detalle");
        public static final URL FUND_LIST = new URL(BASE + "fondos/posicionGlobalLista");
        public static final URL FUND_DETAILS = new URL(BASE + "fondos/detalleFondos");

        public static final URL USER_DATA = new URL(BASE + "login/loginDatosUsuario");
    }

    public static class DefaultRequestParams {
        public static final String LANGUAGE_EN =
                "en"; // English TODO: Language constants already exists somewhere?
        public static final String ORIGIN =
                "51402"; // Can seemingly be anything as long as it exists, purpose unknown.
        public static final String CHANNEL =
                "O"; // Only some valid values (1, 2, O, ...), purpose unknown.
        public static final String INSTALLATION_ID =
                "CIAPPLPh8,1XAEvy+IW9P82Pl+fvzwnfiAzzxs="; // App install ID?

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
        public static final String DNI = "linkDNI";
        public static final String DATE_OF_BIRTH = "linkDatnac";
        public static final String FIRST_NAME = "linkNomper";
        public static final String FIRST_SUR_NAME = "linkPriape";
        public static final String SECOND_SUR_NAME = "linkSegape";
    }

    public static class TransactionDescriptions {
        public static final String TRANSFER = "TRANSFER";
    }

    public static class LogTags {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("lacaixa_unknown-accountype");
        public static final LogTag UNKNOWN_LOAN_CATEGORY =
                LogTag.from("lacaixa_unknown_loan_category");
    }

    public static class Sign {
        public static final String PLUS = "+";
        public static final String MINUS = "-";
    }

    public static class LiquidationSimulation {
        public static final String TRUE = "S";
        public static final String FALSE = "N";
    }
}
