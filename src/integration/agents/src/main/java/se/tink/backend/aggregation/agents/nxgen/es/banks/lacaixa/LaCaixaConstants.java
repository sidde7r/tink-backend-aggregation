package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountFlag;

public final class LaCaixaConstants {
    private LaCaixaConstants() {}

    public static final String CURRENCY = "EUR";
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_REGEX_DDMMYYYY =
            "^([0-2][0-9]||3[0-1])/(0[0-9]||1[0-2])/([0-9][0-9])?[0-9][0-9]$";
    public static final String OTPSMS_AUTH = "n por OTPSMS";
    public static final TypeMapper<Instrument.Type> INSTRUMENT_TYPE_MAPPER =
            TypeMapper.<Instrument.Type>builder()
                    .put(Instrument.Type.STOCK, "10160", "15888", "15890")
                    .put(Instrument.Type.FUND, "10060")
                    .build();

    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.SAVINGS, AccountFlag.PSD2_PAYMENT_ACCOUNT, "LIBRETA")
                    .put(AccountTypes.SAVINGS, "LIBRETA ESTRELLA")
                    .put(
                            AccountTypes.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "CUENTA CORRIENTE",
                            "SERVICIO FAMILIAR",
                            "CUENTA NO RESIDENTE",
                            "")
                    .build();

    public static final TypeMapper<LoanDetails.Type> LOAN_TYPE_MAPPER =
            TypeMapper.<LoanDetails.Type>builder()
                    .put(LoanDetails.Type.MORTGAGE, "10045", "10043")
                    .put(LoanDetails.Type.VEHICLE, "10179")
                    .put(LoanDetails.Type.BLANCO, BlancoLoanTypes.getAllKeysOfBlancoLoanType())
                    .build();

    public enum BlancoLoanTypes {
        CONSUMER_LOAN("11407", "10097", "17240", "21919"),
        EXPRESS_LOAN("12805"),
        ICO_LOANS("24634"),
        MICROCREDITS("14090", "12739", "18873", "14091"),
        ONLINE("11410"),
        PERSONAL("10052", "12142", "22896");

        private final List<String> keys;

        BlancoLoanTypes(String... keys) {
            this.keys = Arrays.asList(keys);
        }

        public List<String> getKeys() {
            return keys;
        }

        public static String[] getAllKeysOfBlancoLoanType() {
            return Arrays.stream(values())
                    .flatMap(blancoLoanTypes -> blancoLoanTypes.getKeys().stream())
                    .toArray(String[]::new);
        }
    }

    public static class LoanTypeName {
        private LoanTypeName() {}

        public static final String MORTGAGE = "PRS";
        public static final String CONSUMER_LOAN = "CRV";
    }

    public static class ErrorCode {
        private ErrorCode() {}

        public static final String EMPTY_LIST = "ERR_TRXM01_007";
        public static final String NO_SECURITIES = "131";
        public static final String NO_ACCOUNTS = "1575";
        public static final String NO_OWN_CARDS = "LIT_NO_PERMITE_CONSULTA_TARJETAS";
        public static final String UNAVAILABLE = "0001";
        public static final String NO_ASSOCIATED_ACCOUNTS = "0007";
        public static final List<String> ACCOUNT_BLOCKED = ImmutableList.of("0207", "0246");
        public static final String INCORRECT_CREDENTIALS = "0250";
        public static final List<String> TEMPORARY_PROBLEM = ImmutableList.of("1574", "1575");
    }

    public static class Urls {
        private Urls() {}

        private static final String BASE = "https://loapp.caixabank.es/xmlapps/rest/";

        public static final URL INIT_LOGIN =
                new URL(BASE + "login/inicio"); // Gets session id. Needed before login.
        public static final URL SUBMIT_LOGIN = new URL(BASE + "login/login");
        public static final URL LOGOUT = new URL(BASE + "login/logout");
        public static final URL KEEP_ALIVE = new URL(BASE + "smartContent/consultaFoto");

        public static final URL MAIN_ACCOUNT = new URL(BASE + "dashboardApp/cuentaPrincipal?");
        public static final URL HOLDERS_LIST = new URL(BASE + "cuentas/listaTitulares?");
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
        public static final URL INIT_ENROLMENT =
                new URL(BASE + "login/enrolmentDispositivo/autorizacion/inicio");
        public static final URL INIT_AUTH_SIGNATURE =
                new URL(BASE + "autorizacion/pushAutDiferido");
        public static final URL VERIFY_AUTH_SIGNATURE =
                new URL(BASE + "autorizacion/verificaFirmaOperacion");
        public static final URL AUTHORIZE_SCA =
                new URL(BASE + "login/enrolmentDispositivo/autorizacion/resultado");
        public static final URL FINALIZE_ENROLMENT =
                new URL(BASE + "login/enrolmentDispositivo/autorizacion/resultado");
        public static final URL CHECK_LOGIN_RESULT = new URL(BASE + "login/loginResultado");
    }

    public static class DefaultRequestParams {
        private DefaultRequestParams() {}

        public static final String LANGUAGE_ES = "es";
        public static final String ORIGIN =
                "51402"; // Can seemingly be anything as long as it exists, purpose unknown.
        public static final String CHANNEL =
                "O"; // Only some valid values (1, 2, O, ...), purpose unknown.

        public static final int NUM_CARDS = 0;
        public static final String OPTION_FILTER = "02";
        public static final String PRODUCT_TYPE_FILTER = "T";
        public static final String STATUS_FILTER = "A";
        public static final String LIQUIDATION_FILTER = "S";
        public static final String ZERO_BALANCE_CONTRACTS = "N";
        public static final String GLOBAL_POSITION_TYPE_P = "P"; // only la caixa engagements
    }

    public static class AuthenticationParams {
        private AuthenticationParams() {}

        public static final String INSTALLATION_ID_PREFIX = "cIAPPLPh9,3";
        public static final String DEVICE_NAME = "Tink";
        public static final String SCA_TYPE_APP = "MOTP";
        public static final String SCA_TYPE_PIN = "PIN1_SCA";
        public static final String SCA_TYPE_PIN_BANKIA = "PIN2_BANKIA";
        public static final String SCA_TYPE_SMS = "OTPSMS";
        public static final String SCA_TYPE_CODECARD = "TCO";
        public static final String SIGNING_URL = "caixabanksign://app2app";
    }

    public static class QueryParams {
        private QueryParams() {}

        public static final String FROM_BEGIN = "inicio";
        public static final String ACCOUNT_NUMBER = "numeroCuenta";
        public static final String DEPOSIT_ID = "idExpediente";
        public static final String DEPOSIT_CONTENT_ID = "idDeposito";
        public static final String ZERO_BALANCE_CONTRACTS = "contratosSaldoCero";
        public static final String GLOBAL_POSITION_TYPE = "tipoPosGlobal";
        public static final String MORE_DATA = "masDatos";
    }

    public static class TemporaryStorage {
        private TemporaryStorage() {}

        public static final String ACCOUNT_REFERENCE = "accountRef";
        public static final String SCA_SMS = "scaSms";
        public static final String CODE_CARD = "codeCard";
        public static final String ENROLMENT_CODE = "enrolmentCode";
        public static final String PIN_BANKIA = "pinBankia";
    }

    public static class PermStorage {
        private PermStorage() {}

        public static final String USER_AGENT = "userAgent";
        public static final String APP_INSTALLATION_ID = "appInstallationId";
    }

    public static class UserData {
        private UserData() {}

        public static final String FULL_HOLDER_NAME = "linkNombreEmp";
        public static final String DNI = "linkDNI";
        public static final String DATE_OF_BIRTH = "linkDatnac";
        public static final String FIRST_NAME = "linkNomper";
        public static final String FIRST_SUR_NAME = "linkPriape";
        public static final String SECOND_SUR_NAME = "linkSegape";
        public static final String IS_CAIXA_MANUAL_DONE = "isCaixaAuthManualDone";
        public static final String IS_ENROLLMENT_NEEDED = "flagEnrolamientoDisp";
    }

    public static class LiquidationSimulation {
        private LiquidationSimulation() {}

        public static final String TRUE = "S";
    }

    public static class RetryFilterValues {
        private RetryFilterValues() {}

        public static final int MAX_ATTEMPTS = 3;
        public static final int RETRY_SLEEP_MILLISECONDS = 1000;
    }

    public static class HolderTypes {
        private HolderTypes() {}

        public static final String OWNER = "Titular";
        public static final String AUTHORIZED_USER = "Autorizado";
    }

    public static class CaixaPayloadValues {
        private CaixaPayloadValues() {}

        public static final String ANDROID_PACKAGE_NAME = "es.caixabank.caixabanksign";
        public static final String IOS_APP_STORE_URL =
                "https://apps.apple.com/es/app/caixabank-sign/id1328811481";
    }

    public static class ErrorCodes {
        public static final String INCORRECT_CREDENTIALS = "0250";
        public static final String ACCESS_DEACTIVATED = "0207";
        public static final String ATTEMPS_EXCEEDED = "0246";
        public static final String UNKNOWN_ISSUE = "1575";
    }

    public static final class HeaderKeys {
        public static final String USER_AGENT = "User-Agent";
        public static final String X_REQUEST_ID = "X-Request-Id";
    }

    public static final class SupplementalInformationKeys {
        public static final String BANKIA_SIGNATURE = "bankiaSig";
    }

    public static final class StepIdentifiers {
        public static final String INITIALIZE_ENROLMENT = "initiateEnrolment";
        public static final String FINALIZE_ENROLMENT = "finalizeEnrolment";
        public static final String APP_SIGN = "caixabankSign";
    }
}
