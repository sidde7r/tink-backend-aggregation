package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank;

import java.time.LocalDate;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;

public class ImaginBankConstants {

    public static final String CURRENCY = "EUR";

    public static class ApiService {
        static final String LOGIN_INIT_PATH = "login/inicio";
        static final String LOGIN_SUBMIT_PATH = "login/login";
        static final String LOGOUT_PATH = "login/logout";
        static final String ACCOUNTS_PATH = "cuentas/lista";
        static final String ACCOUNT_TRANSACTION_PATH = "cuentas/extracto";

        static final String CHECK_FOTO_PATH = "smartContent/consultaFoto";

        public static final String INITIATE_CARD_FETCHING_PATH = "finanbox/inicializarBoxes";
        static final String CARDS_PATH = "tarbox/listadoTarjetas";
        static final String CARD_TRANSACTIONS_PATH = "tarbox/listaMovimientos";

        static final String USER_DATA_PATH = "login/loginDatosUsuario";
    }

    public static class Urls {
        private static final String BASE = "https://loapp.caixabank.es/xmlapps/rest/";

        public static final URL INIT_LOGIN =
                new URL(BASE + ApiService.LOGIN_INIT_PATH); // Gets session id. Needed before login.
        public static final URL SUBMIT_LOGIN = new URL(BASE + ApiService.LOGIN_SUBMIT_PATH);
        public static final URL LOGOUT = new URL(BASE + ApiService.LOGOUT_PATH);
        public static final URL FETCH_ACCOUNTS = new URL(BASE + ApiService.ACCOUNTS_PATH);

        public static final URL KEEP_ALIVE = new URL(BASE + ApiService.CHECK_FOTO_PATH);
        public static final URL FETCH_ACCOUNT_TRANSACTION =
                new URL(BASE + ApiService.ACCOUNT_TRANSACTION_PATH);
        public static final URL FETCH_CARDS = new URL(BASE + ApiService.CARDS_PATH);
        public static final URL INITIATE_CARD_FETCHING =
                new URL(BASE + ApiService.INITIATE_CARD_FETCHING_PATH);

        public static final URL FETCH_CARD_TRANSACTIONS =
                new URL(BASE + ApiService.CARD_TRANSACTIONS_PATH);

        public static final URL USER_DATA = new URL(BASE + ApiService.USER_DATA_PATH);
    }

    public static class DefaultRequestParams {
        public static final String LANGUAGE_EN = "en";
        public static final String ORIGIN = "13190";
        public static final String CHANNEL = "3";
        public static final String INSTALLATION_ID =
                "eIAPPLPh8,1+SfeQDnFvsPWCKY4QFdUJ9ofXhc="; // App install ID
        public static final String VIRTUAL_KEYBOARD = "false";
        public static final String DEMO = "false";
        public static final String ALTA_IMAGINE = "false";
    }

    public static class QueryParams {
        public static final String FROM_BEGIN = "inicio";
        public static final String ACCOUNT_NUMBER = "numeroCuenta";

        // credit cards
        public static final String INITIALIZED_BOXES = "boxesInicializados";
        public static final String INITIALIZED_BOXES_VALUE = "S";
        public static final String CARD_STATUS = "estadoTarjeta";
        public static final String CARD_STATUS_VALUE = "A";
        public static final String MORE_DATA = "masDatos";
        public static final String MORE_DATA_VALUE = "false";
        public static final String PROFILE = "perfil";
        public static final String PROFILE_VALUE = "I";
    }

    public static class TemporaryStorage {
        public static final String ACCOUNT_REFERENCE = "accountRef";
    }

    public static class StatusCodes {
        public static final int INCORRECT_USERNAME_PASSWORD = 409; // Conflict
    }

    public static class TransactionDescriptions {
        public static final Pattern CLEAN_TRANSFER_MSG = Pattern.compile("(\\d*-)(.*)$");
        public static final String TRANSFER = "TRANSFER";
    }

    public static class CreditCard {
        public static final String PREPAID = "P";
        public static final String CREDIT = "C";
        public static final String FRACTIONAL_LIST_FILTER = "S";

        public static LocalDate START_DATE = LocalDate.of(2013, 01, 01);
    }

    public static class LogTags {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE =
                LogTag.from("imaginbank_unknown_accountype");
        public static final LogTag MULTIPLE_ACCOUNTS = LogTag.from("imaginbank_multiple_accounts");
        public static final LogTag CREDIT_CARD = LogTag.from("imaginbank_credit_card");
    }

    public static class Storage {
        public static final String LOGIN_RESPONSE = "loginResponse";
    }

    public static class IdentityData {
        public static final String DNI = "linkDNI";
    }
}
