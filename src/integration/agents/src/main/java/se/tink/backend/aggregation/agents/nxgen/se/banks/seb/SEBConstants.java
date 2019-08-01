package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class SEBConstants {

    public static class Urls {
        public static final String BASE = "https://mp.seb.se";

        public static final URL FETCH_AUTOSTART_TOKEN =
                new URL(BASE + Endpoints.FETCH_AUTOSTART_TOKEN);
        public static final URL COLLECT_BANKID = new URL(BASE + Endpoints.COLLECT_BANKID);
        public static final URL INITIATE_SESSION = new URL(BASE + Endpoints.INITIATE_SESSION);
        public static final URL ACTIVATE_SESSION = new URL(BASE + Endpoints.ACTIVATE_SESSION);
        public static final URL LIST_ACCOUNTS = new URL(BASE + Endpoints.LIST_ACCOUNTS);
        public static final URL LIST_TRANSACTIONS = new URL(BASE + Endpoints.LIST_TRANSACTIONS);
        public static final URL LIST_PENDING_TRANSACTIONS =
                new URL(BASE + Endpoints.LIST_PENDING_TRANSACTIONS);
    }

    public static class Endpoints {
        public static final String FETCH_AUTOSTART_TOKEN = "/nauth2/Authentication/api/v1/bid/auth";
        public static final String COLLECT_BANKID = "/nauth2/Authentication/api/v1/bid/";
        private static final String API_BASE = "/1000/ServiceFactory/PC_BANK/PC_Bank";
        public static final String INITIATE_SESSION = API_BASE + "Init11Session01.asmx/Execute";
        public static final String ACTIVATE_SESSION = API_BASE + "Aktivera01Session01.asmx/Execute";
        public static final String LIST_ACCOUNTS = API_BASE + "Lista01Konton_privat01.asmx/Execute";
        public static final String LIST_TRANSACTIONS =
                API_BASE + "Lista01Rorelse_ftg03.asmx/Execute";
        public static final String LIST_PENDING_TRANSACTIONS =
                API_BASE + "Lista01Skydd01.asmx/Execute";
    }

    public static class HeaderKeys {
        public static final String X_SEB_UUID = "x-seb-uuid";
    }

    public static class RequestBody {
        public static final String SEB_REFERER_VALUE = "/masp/mbid";
    }

    public static class LoginCodes {
        // Strings for status comparison when logging in with BankID.
        public static final String START_BANKID = "RFA1";
        public static final String ALREADY_IN_PROGRESS = "RFA3";
        public static final String USER_CANCELLED = "RFA6";
        public static final String NO_CLIENT = "RFA8";
        public static final String USER_SIGN = "RFA9";
        public static final String AUTHENTICATED = "RFA100";
        public static final String AUTHORIZATION_REQUIRED = "RFA101";
        public static final String WAITING_FOR_BANKID = "RFA15A";
        public static final String COLLECT_BANKID = "RFA102";
    }

    public static class InitResult {
        public static final String OK = "OK";
    }

    public static class ServiceInputKeys {
        public static final String CUSTOMER_TYPE = "CUSTOMERTYPE";
        public static final String CUSTOMER_ID = "KUND_ID";
        public static final String ACCOUNT_TYPE = "KONTO_TYP";
    }

    public static class ServiceInputValues {
        public static final String PRIVATE = "P";
        public static final String DEFAULT_ACCOUNT_TYPE = "ICKEFOND";
    }

    public static class TransactionType { // ROR_TYP
        public static final String OTHER = "1";
        public static final String CARD_TRANSACTION = "2";
        public static final String FOREIGN_CARD_TRANSACTION = "5";
        public static final String FOREIGN_WITHDRAWAL = "7";
        public static final String INVOICE_PAYMENT = "8"; // has BG, OCR
    }

    public static class StorageKeys {
        public static final String CUSTOMER_NAME = "customerName";
        public static final String CUSTOMER_NUMBER = "customerNumber";
        public static final String SHORT_USERID = "shortUserId";
        public static final String SSN = "ssn";
        public static final String ACCOUNT_CUSTOMER_ID = "customerId";
    }

    public static class SystemCode {
        public static final int BANKID_NOT_AUTHORIZED = 2;
        public static final int KYC_ERROR = 9200;
    }

    public static class ErrorMessages {
        public static final String UNKNOWN_BANKID_STATUS = "Unknown BankIdStatus (%s)";
    }

    public enum UserMessage implements LocalizableEnum {
        MUST_AUTHORIZE_BANKID(
                new LocalizableKey(
                        "The first time you use your mobile BankId you have to verify it with your Digipass. Login to the SEB-app with your mobile BankID to do this.")),
        WRONG_BANKID(
                new LocalizableKey(
                        "Wrong BankID signature. Did you log in with the wrong personnummer?")),
        DO_NOT_SUPPORT_YOUTH(
                new LocalizableKey(
                        "It looks like you have SEB Ung. Unfortunately we currently only support SEB's standard login.")),
        MUST_ANSWER_KYC(
                new LocalizableKey(
                        "To continue using this app you must answer some questions from your bank. Please log in with your bank's app or website."));

        private final LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }

    public static final GenericTypeMapper<AccountTypes, Integer> ACCOUNT_TYPE_MAPPER =
            GenericTypeMapper.<AccountTypes, Integer>genericBuilder()
                    .put(
                            AccountTypes.CHECKING,
                            1, // PRIVATKONTO
                            3, // PERSONALLONEKONTO
                            17) // SPECIALINLONEKONTO
                    .put(
                            AccountTypes.SAVINGS,
                            16, // ENKLA_SPARKONTOT
                            12) // ENKLA_SPARKONTOT_FORETAG
                    .put(
                            AccountTypes.INVESTMENT,
                            54, // ISK_KAPITALKONTO
                            22, // FUND
                            27, // IPS
                            35) // PLACERINGSKONTO
                    .put(AccountTypes.OTHER, 2) // can be NOTARIATKONTO, SKOGSKONTO, FRETAGSKONTO
                    .build();
}
