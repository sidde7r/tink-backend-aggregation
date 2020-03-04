package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class SebConstants {

    public static final String DEFAULT_CURRENCY = "SEK";
    public static final int AGE_LIMIT = 18;

    public static class Urls {
        public static final String BASE = "https://mp.seb.se";

        public static final URL AUTHENTICATE = new URL(BASE.concat(Endpoints.AUTHENTICATIONS));
        public static final URL INITIATE_SESSION = new URL(BASE.concat(Endpoints.INITIATE_SESSION));
        public static final URL ACTIVATE_SESSION = new URL(BASE.concat(Endpoints.ACTIVATE_SESSION));
        public static final URL LIST_ACCOUNTS = new URL(BASE.concat(Endpoints.LIST_ACCOUNTS));
        public static final URL LIST_CARDS = new URL(BASE.concat(Endpoints.LIST_CARDS));
        public static final URL LIST_LOANS = new URL(BASE.concat(Endpoints.LIST_LOANS));
        public static final URL LIST_TRANSACTIONS =
                new URL(BASE.concat(Endpoints.LIST_TRANSACTIONS));
        public static final URL LIST_RESERVED_TRANSACTIONS =
                new URL(BASE.concat(Endpoints.LIST_RESERVED_TRANSACTIONS));
        public static final URL LIST_UPCOMING_TRANSACTIONS =
                new URL(BASE.concat(Endpoints.LIST_UPCOMING_TRANSACTIONS));
        public static final URL LIST_PENDING_CREDIT_CARD_TRANSACTIONS =
                new URL(BASE.concat(Endpoints.LIST_PENDING_CREDIT_CARD_TRANSACTIONS));
        public static final URL LIST_BOOKED_CREDIT_CARD_TRANSACTIONS =
                new URL(BASE.concat(Endpoints.LIST_BOOKED_CREDIT_CARD_TRANSACTIONS));
        public static final URL LIST_INVESTMENT_ACCOUNTS =
                new URL(BASE.concat(Endpoints.LIST_INVESTMENT_ACCOUNTS));
        public static final URL FETCH_INTESTMENT_ACCOUNT_DETAILS =
                new URL(BASE.concat(Endpoints.INVESTMENT_ACCOUNT_DETAILS));
    }

    public static class Endpoints {
        public static final String AUTHENTICATIONS = "/auth/bid/v2/authentications";
        private static final String API_BASE = "/1000/ServiceFactory/PC_BANK/";
        public static final String LIST_INVESTMENT_ACCOUNTS =
                API_BASE + "Tl_forsakringLista11Enga01.asmx/Execute";
        public static final String INVESTMENT_ACCOUNT_DETAILS =
                API_BASE + "PC_BankHamta11Savingsvarde01.asmx/Execute";
        public static final String INITIATE_SESSION =
                API_BASE + "PC_BankInit11Session01.asmx/Execute";
        public static final String ACTIVATE_SESSION =
                API_BASE + "PC_BankAktivera01Session01.asmx/Execute";
        public static final String LIST_ACCOUNTS =
                API_BASE + "PC_BankLista01Konton_privat01.asmx/Execute";
        public static final String LIST_CARDS =
                API_BASE + "PC_BankLista11Kort_privat05.asmx/Execute";
        public static final String LIST_LOANS =
                API_BASE + "PC_BankLista11Laninfo_privat03.asmx/Execute";
        public static final String LIST_TRANSACTIONS =
                API_BASE + "PC_BankLista01Rorelse_ftg03.asmx/Execute";
        public static final String LIST_RESERVED_TRANSACTIONS =
                API_BASE + "PC_BankLista01Skydd01.asmx/Execute";
        private static final String LIST_UPCOMING_TRANSACTIONS =
                API_BASE + "PC_BankLista11Komm_uppdrag02.asmx/Execute";
        private static final String LIST_PENDING_CREDIT_CARD_TRANSACTIONS =
                API_BASE + "PC_BankLista11Ofakt_korttran02.asmx/Execute";
        private static final String LIST_BOOKED_CREDIT_CARD_TRANSACTIONS =
                API_BASE + "PC_BankLista11Fakt_korttran02.asmx/Execute";
    }

    public static class HeaderKeys {
        public static final String X_SEB_UUID = "x-seb-uuid";
        public static final String X_SEB_CSRF = "x-seb-csrf";
    }

    public static class Authentication {
        public static final GenericTypeMapper<BankIdStatus, String> statusMapper =
                GenericTypeMapper.<BankIdStatus, String>genericBuilder()
                        .put(BankIdStatus.DONE, "complete")
                        .put(BankIdStatus.WAITING, "pending")
                        .put(BankIdStatus.FAILED_UNKNOWN, "failed")
                        .setDefaultTranslationValue(BankIdStatus.FAILED_UNKNOWN)
                        .build();

        public static final GenericTypeMapper<BankIdStatus, String> hintCodeMapper =
                GenericTypeMapper.<BankIdStatus, String>genericBuilder()
                        .put(BankIdStatus.TIMEOUT, "expired_transaction")
                        .put(BankIdStatus.EXPIRED_AUTOSTART_TOKEN, "start_failed")
                        .put(BankIdStatus.CANCELLED, "cancelled", "user_cancel")
                        .setDefaultTranslationValue(BankIdStatus.FAILED_UNKNOWN)
                        .build();
    }

    public static class InitResult {
        public static final String OK = "OK";
    }

    public static class ServiceInputKeys {
        public static final String CUSTOMER_TYPE = "CUSTOMERTYPE";
        public static final String CUSTOMER_ID = "KUND_ID";
        public static final String ACCOUNT_TYPE = "KONTO_TYP";
        public static final String CUSTOMER_NUMBER = "SEB_KUND_ID";
        public static final String MAX_ROWS = "MAX_ROWS";
        public static final String ACCOUNT_NUMBER = "KONTO_NR";
        public static final String CREDIT_CARD_HANDLE = "BILL_UNIT_HDL";
        public static final String PENDING_TRANSACTIONS = "RESERVE_AMT_FL";
        public static final String INVESTMENT_DETAIL_HANDLE = "DETAIL_URL";
    }

    public static class ServiceInputValues {
        public static final String PRIVATE = "P";
        public static final String YES = "Y";
        public static final String DEFAULT_ACCOUNT_TYPE = "ICKEFOND";
        public static final int MAX_ROWS = 110;
    }

    // ROR_TYP field in TransactionEntity
    public static class TransactionType {
        public static final String OTHER = "1";
        public static final String CARD_TRANSACTION = "2";
        public static final String FOREIGN_CARD_TRANSACTION = "5";
        public static final String FOREIGN_WITHDRAWAL = "7";
        // INVOICE_PAYMENT has BG, OCR
        public static final String INVOICE_PAYMENT = "8";
    }

    public static class StorageKeys {
        public static final String CUSTOMER_NAME = "customerName";
        public static final String CUSTOMER_NUMBER = "customerNumber";
        public static final String SHORT_USERID = "shortUserId";
        public static final String SSN = "ssn";
        public static final String ACCOUNT_CUSTOMER_ID = "customerId";
        public static final String CREDIT_CARD_ACCOUNT_HANDLE_PREFIX = "card_handle:";
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

    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    // 1: PRIVATKONTO
                    // 3: PERSONALLONEKONTO
                    // 17: SPECIALINLONEKONTO
                    .put(AccountTypes.CHECKING, "1", "3", "17")
                    // 16: ENKLA_SPARKONTOT
                    // 12: ENKLA_SPARKONTOT_FORETAG
                    .put(AccountTypes.SAVINGS, "16", "12")
                    // 54: ISK_KAPITALKONTO
                    // 22: FUND
                    // 27: IPS
                    // 35: PLACERINGSKONTO
                    .put(AccountTypes.INVESTMENT, "54", "22", "27", "35")
                    .build();
}
