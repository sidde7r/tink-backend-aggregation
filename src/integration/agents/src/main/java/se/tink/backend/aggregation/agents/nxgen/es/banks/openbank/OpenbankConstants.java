package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank;

import java.time.ZoneId;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public abstract class OpenbankConstants {
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Madrid");
    public static final String PROVIDER_NAME = "es-openbank-password";

    public static final String USERNAME_TYPE = "username-type";
    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, "300")
                    .put(AccountTypes.SAVINGS, "301")
                    .put(AccountTypes.CREDIT_CARD, "500")
                    .ignoreKeys("506") // 506 is Debit Card
                    .build();

    public static class Codes {
        public static final String MARKET_CODE = "es";
        public static final String LANGUAGE_CODE = "E";
        public static final String CURRENCY_CODE = "EUR";
        public static final String PROCEDURE_CODE = "BE";
    }

    public static class Storage {
        public static final String AUTH_TOKEN = "auth-token";
        public static final String PRODUCT_CODE_OLD = "product-code-old";
        public static final String CONTRACT_NUMBER_OLD = "contract-number-old";
        public static final String PRODUCT_CODE_NEW = "product-code-new";
        public static final String CONTRACT_NUMBER_NEW = "contract-number-new";
        public static final String CARD_NUMBER = "card-number";
    }

    public static class ApiService {
        static final String LOGIN_PATH = "/authenticationcomposite/login";
        static final String LOGOUT_PATH = "/authenticationcomposite/logout";
        static final String KEEP_ALIVE_PATH = "/claves/cmc";

        static final String USER_DATA_PATH = "/posicion-global-total";
        static final String ACCOUNT_TRANSACTIONS_PATH = "/my-money/cuentas/movimientos";
        static final String TRANSACTION_DETAILS_PATH = "/cuentas/movimientos/detalles";
        static final String CARD_TRANSACTIONS_PATH = "/my-cards/tarjetas/movimientos-categoria";
        static final String IDENTITY_PATH = "/user/profile";
    }

    public static class Urls {
        public static final String HOST = "https://api.openbank.es";

        public static final URL LOGIN = new URL(HOST + ApiService.LOGIN_PATH);
        public static final URL LOGOUT = new URL(HOST + ApiService.LOGOUT_PATH);
        public static final URL KEEP_ALIVE = new URL(HOST + ApiService.KEEP_ALIVE_PATH);

        public static final URL USER_DATA = new URL(HOST + ApiService.USER_DATA_PATH);
        public static final URL ACCOUNT_TRANSACTIONS =
                new URL(HOST + ApiService.ACCOUNT_TRANSACTIONS_PATH);
        public static final URL TRANSACTION_DETAILS =
                new URL(HOST + ApiService.TRANSACTION_DETAILS_PATH);
        public static final URL CARD_TRANSACTIONS =
                new URL(HOST + ApiService.CARD_TRANSACTIONS_PATH);
        static final URL IDENTITY_URL = new URL(HOST + ApiService.IDENTITY_PATH);
    }

    public static class UsernameTypes {
        // These types might need to be made more granular at a later time.

        // The following formats of usernames matches (regex) NIE/NIF:
        //  [0-9]+[A-Z]
        public static final String NIE = "C";
        public static final String NIF = "N";

        public static final String PASSPORT = "P";
        public static final String OTHER_DOCUMENT = "I";
    }

    public static class AccountType {}

    public static class Headers {
        static final String AUTH_TOKEN = "openBankAuthToken";
    }

    public static class QueryParams {
        public static final String PRODUCT_CODE = "producto"; // Chars 11-13 of IBAN
        public static final String CONTRACT_NUMBER = "numeroContrato"; // Chars 14-20 of IBAN
        public static final String CONNECTING_PRODUCT_CODE = "conexionProducto";
        public static final String CONNECTING_CONTRACT_NUMBER = "conexionNumeroContrato";
        public static final String FROM_DATE = "fechaDesde";
        public static final String TO_DATE = "fechaHasta";
        public static final String LANGUAGE_CODE = "codigoIdioma";
        public static final String CURRENCY_CODE = "codigoMoneda";
        public static final String PROCEDURE_CODE = "codigoProcedimiento";
        public static final String MOVEMENT_OF_THE_DAY_INDEX = "diaMovimiento";
        public static final String DATE_NOTED = "fechaAnotacion";
        public static final String SITUATION_ID = "idSituacion";
    }

    public static class ErrorCodes {
        public static final String INVALID_LOGIN_USERNAME_TYPE = "NotNull";
        public static final String INCORRECT_CREDENTIALS = "bad.input.credentials.incorrect";
    }

    public static class LogTags {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE =
                LogTag.from(PROVIDER_NAME + "-unknown-account-type");
    }
}
