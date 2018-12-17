package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class IcaBankenConstants {

    public static final class Urls {
        public static final String HOST = "https://appserver.icabanken.se";

        public static final URL LOGIN_BANKID = new URL(HOST + "/api/session/login/bankid/{identifier}");
        public static final URL KEEP_ALIVE = new URL(HOST + "/api/session/heartbeat");
        public static final URL SIGN_TRANSFER_COLLECT_URL =new URL(HOST + "/api/bankId/sign/collect/{requestId}");
        public static final URL ACCOUNTS = new URL(HOST + "/api/accounts");
        public static final URL DEPOTS = new URL(HOST + "/api/depots");
        public static final URL FUND_DETAILS = new URL(HOST + "/api/funds/{fundId}");
        public static final URL TRANSACTIONS = new URL(HOST + "/api/accounts/{identifier}/transactions");
        public static final URL RESERVED_TRANSACTIONS = new URL(HOST + "/api/accounts/{identifier}/reservedTransactions");
        public static final URL UPCOMING_TRANSACTIONS = new URL(HOST + "/api/events/future");
        public static final URL LOAN_OVERVIEW = new URL(HOST + "/api/engagement/loans");
        public static final URL GIRO_DESTINATION_NAME = new URL(HOST + "/api/recipients/pgBgRecipientName/{pgnumber}");
        public static final URL UNSIGNED_TRANSFERS = new URL(HOST + "/api/events/unsigned");
        public static final URL TRANSFER_DESTINATIONS = new URL(HOST + "/api/recipients");
        public static final URL TRANSFER_BANKS = new URL(HOST + "/api/accounts/transferBanks");
        public static final URL INIT_TRANSFER_SIGN = new URL(HOST + "/api/assignments/bundle/bankid/init");
        public static final URL SIGNED_ASSIGNMENTS = new URL(HOST + "/api/assignments/bundle/bankid/submit");
        public static final URL UNSIGNED_ASSIGNMENTS = new URL(HOST + "/api/assignments");
        public static final URL DELETE_UNSIGNED_TRANSFER = new URL(HOST + "/api/assignments/bundle/{transferId}");
        public static final URL ACCEPT_EINVOICE = new URL(HOST + "/api/egiro/invoice/accept");
        public static final URL VALIDATE_INVOICE = new URL(HOST + "/api/egiro/invoice/validate");
        public static final URL EINVOICES = new URL(HOST + "/api/egiro/invoices");
        public static final URL UPDATE_INVOICE = new URL (HOST + "/api/egiro/invoice/update");
        public static final URL INIT_EINVOICE_SIGN = new URL (HOST + "/api/egiro/recipient/bankId/init/{invoiceId}");
        public static final URL FINISH_EINVOICE_SIGN = new URL(HOST + "/api/egiro/recipient/bankId");
    }

    public static final class Headers {
        public static final String ACCEPT = "Accept";
        public static final String HEADER_CLIENTAPPVERSION = "ClientAppVersion";
        public static final String VALUE_CLIENTAPPVERSION = "1.44.2";
        public static final String HEADER_USERAGENT = "User-Agent";
        public static final String VALUE_USERAGENT = "ICA Banken/1.42.1.3 (iPhone; iOS 10.1.1; Scale/2.00)";
        public static final String HEADER_APIKEY = "ApiKey";
        public static final String VALUE_APIKEY = "B4D6E1AC-527A-4BBC-AF04-1F2A6D8B38BA";
        public static final String HEADER_API_VERSION = "ApiVersion";
        public static final String VALUE_API_VERSION = "8";
        public static final String HEADER_CLIENT_OS = "ClientOS";
        public static final String VALUE_CLIENT_OS = "iOS";
        public static final String HEADER_CLIENT_OS_VERSION = "ClientOSVersion";
        public static final String VALUE_CLIENT_OS_VERSION = "10.3.3";
        public static final String HEADER_CLIENT_HARDWARE = "ClientHardware";
        public static final String VALUE_CLIENT_HARDWARE = "iPhone";
    }

    public static final class BankIdStatus {
        public static final String PENDING = "pending";
        public static final String OK = "ok";
        public static final String ABORTED = "aborted";
    }

    public static final class StatusCodes {
        public static final int OK_RESPONSE = 0;
    }

    public static final class IdTags {
        public static final String SESSION_ID_TAG = "sessionId";
        public static final String IDENTIFIER_TAG = "identifier";
        public static final String TO_DATE_TAG = "toDate";
        public static final String FUND_ID_TAG = "fundId";
        public static final String GIRO_NUMBER_TAG = "pgnumber";
        public static final String REQUEST_ID_TAG = "requestId";
        public static final String TRANSFER_ID_TAG = "transferId";
        public static final String INVOICE_ID_TAG = "invoiceId";
        public static final String NOT_AVAILABLE_TAG = "N/A";
        //Used for making a sign request for transfers
        public static final String BUNDLE_TAG = "Bundle";
        public static final String KEY_TAG = "Key";
        public static final String VALUE_TAG = "Value";
        public static final String SWEDISH_AND_SEPARATOR = "och";
    }

    public static final class LoanDetailsKeys {
        public static final String INTEREST_RATE = "aktuell räntesats";
        public static final String LOAN_NAME = "lån";
        public static final String INITIAL_DEBT = "ursprunglig skuld";
        public static final String INITIAL_DATE = "utbetalningsdag";
        public static final String APPLICANTS = "låntagare";
        public static final String NEXT_DAY_OF_TERMS_CHANGE = "nästa villkorsändringsdag";
        public static final String MONTH_BOUND = "räntebindningstid";
        public static final String SECURITY = "säkerhet";
        public static final String TYPE_OF_LOAN = "typ av lån";
    }

    public static final class Transfers {
        public static final int MAX_POLL_ATTEMPTS = 90;

        public static final String BANK_TRANSFER = "Transfer";
        public static final String PAYMENT = "Payment";
        public static final String PAYMENT_BG = "PaymentBg";
        public static final String PAYMENT_PG = "PaymentPg";

        public static final String OCR = "1";
        public static final String MESSAGE = "2";

        public static final int EINVOICE_VALIDATE_SUCCESS_CODE = 0;
        public static final int EINVOICE_VALIDATE_ERROR_CODE = 1000;
        public static final String EINVOICE_VALIDATE_ERROR_MSG = "Error validating invoice.";
        public static final String EINVOICE_DATE_CHANGED_MSG = "Angivet datum har ändrats till närmast möjliga dag.";

        public static final String RECIPIENT_NAME_FIELD_NAME = "name";
        public static final String RECIPIENT_NAME_FIELD_DESCRIPTION = "Mottagarnamn";

        public static final int SOURCE_MSG_MAX_LENGTH = 25;
        public static final int DEST_MSG_MAX_LENGTH = 12;
        public static final int DEST_MSG_MAX_LENGTH_BETWEEN_OWN_ACCOUNTS = 25;
        public static final String WHITELISTED_MSG_CHARS = ",.-?!/+";
    }

    public static final class AccountTypes {
        public static final String ICA_ACCOUNT = "icaaccount";
        public static final String SAVINGS_ACCOUNT = "savingsaccount";
        public static final String CREDIT_CARD_ACCOUNT = "creditaccount";
        public static final String ISK_ACCOUNT = "isk";
        public static final String DEPOT_ACCOUNT = "depot";
        public static final String PAYMENT_BG = "paymentbg";
        public static final String PAYMENT_PG = "paymentpg";
        public static final String PAYMENT = "payment";
    }

    public static final class LogMessage {
        public static final String PROVIDER_UNIQUE_ID_NOT_FOUND = "Missing PROVIDER_UNIQUE_ID on transfer payload";
        public static final String NO_ORIGINAL_TRANSFER = "No original transfer on payload to compare with.";
        public static final String NO_RECIPIENT_NAME = "Could not get recipient name from user";
        public static final String EINVOICE_MODIFIED_IN_BANK_APP = "The e-invoice has been changed in the bank app," +
                " user have to refresh so that we update according to most recent data";
        public static final String EINVOICE_NOT_FOUND = "Could not find the selected invoice in user's bank.";
        public static final String EINVOICE_DESTINATION_MODIFIED = "Destination account cannot be changed.";
        public static final String EINVOICE_SRC_MSG_MODIFIED = "Source message cannot be changed.";
        public static final String EINVOICE_DEST_MSG_MODIFIED = "Destination message cannot be changed.";
        public static final String EINVOICE_UPDATE_ERROR = "Could not update invoice";
        public static final String EINVOICE_VALIDATE_ERROR = "Could not validate invoice";
    }

    public static final class Log {
        public static final String LOAN_TYPE = "icabanken_unknown_loan_type";
    }

    public enum UserMessage implements LocalizableEnum {
        KNOW_YOUR_CUSTOMER(new LocalizableKey("To be able to refresh your accounts you need to update your customer info in the ICA bank app.")),
        EINVOICE_MODIFIED_IN_BANK_APP(new LocalizableKey("If the e-invoice has been modified in the ICA Banken app, please refresh you credentials.")),
        BANKID_TRANSFER_INTERRUPTED(new LocalizableKey("Another BankId authentication was initiated while signing the transfer. Please try again."));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }
        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }

}
