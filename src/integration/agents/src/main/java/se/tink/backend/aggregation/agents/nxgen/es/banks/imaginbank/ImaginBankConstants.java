package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank;

import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ImaginBankConstants {
    private ImaginBankConstants() {}

    public static final String CURRENCY = "EUR";
    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(TransactionalAccountType.CHECKING, "IMAGIN ACCOUNT", "imagin")
                    .put(TransactionalAccountType.SAVINGS, "LIBRETA ESTRELLA", "LIBRETA")
                    .build();

    private static class ApiService {
        private static final String LOGIN_INIT_PATH = "login/inicio";
        private static final String LOGIN_SUBMIT_PATH = "login/login";
        private static final String LOGOUT_PATH = "login/logout";
        private static final String ACCOUNTS_PATH = "cuentas/lista";
        private static final String ACCOUNT_TRANSACTION_PATH = "cuentas/extracto";

        private static final String CHECK_FOTO_PATH = "smartContent/consultaFoto";

        private static final String INITIATE_CARD_FETCHING_PATH = "finanbox/inicializarBoxes";
        private static final String CARDS_PATH = "tarbox/listadoTarjetas";
        private static final String CARD_TRANSACTIONS_PATH = "tarbox/listaMovimientos";

        private static final String USER_DATA_PATH = "login/loginDatosUsuario";

        private static final String HOLDERS_PATH = "cuentas/listaTitulares";
    }

    public static class Urls {
        private Urls() {}

        private static final String BASE = "https://loapp.caixabank.es/xmlapps/rest/";

        public static final URL INIT_LOGIN =
                new URL(BASE + ApiService.LOGIN_INIT_PATH); // Gets session id. Needed before login.
        public static final URL SUBMIT_LOGIN = new URL(BASE + ApiService.LOGIN_SUBMIT_PATH);
        public static final URL LOGOUT = new URL(BASE + ApiService.LOGOUT_PATH);
        public static final URL FETCH_ACCOUNTS = new URL(BASE + ApiService.ACCOUNTS_PATH);
        public static final URL HOLDERS_LIST = new URL(BASE + ApiService.HOLDERS_PATH);

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
        private DefaultRequestParams() {}

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
        private QueryParams() {}

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
        private TemporaryStorage() {}

        public static final String ACCOUNT_REFERENCE = "accountRef";
    }

    public static class ErrorCode {
        private ErrorCode() {}

        public static final List<String> ACCOUNT_BLOCKED = ImmutableList.of("0207", "0246");
        public static final String INCORRECT_CREDENTIALS = "0250";
        public static final String UNAVAILABLE = "001";
        public static final String TEMPORARY_PROBLEM = "1574";
    }

    public static class TransactionDescriptions {
        private TransactionDescriptions() {}

        public static final Pattern CLEAN_TRANSFER_MSG = Pattern.compile("(\\d*-)(.*)$");
        public static final String TRANSFER = "TRANSFER";
    }

    public static class CreditCard {
        private CreditCard() {}

        public static final String PREPAID = "P";
        public static final String CREDIT = "C";
        public static final String FRACTIONAL_LIST_FILTER = "S";

        public static final LocalDate START_DATE = LocalDate.of(2013, 01, 01);
    }

    public static class Storage {
        private Storage() {}

        public static final String LOGIN_RESPONSE = "loginResponse";
    }

    public static class IdentityData {
        private IdentityData() {}

        public static final String DNI = "linkDNI";
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
}
