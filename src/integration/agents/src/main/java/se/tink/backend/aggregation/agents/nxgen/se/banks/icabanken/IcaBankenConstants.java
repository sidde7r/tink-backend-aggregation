package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import java.time.ZoneId;
import java.util.Locale;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class IcaBankenConstants {

    public static final String CURRENCY = "SEK";

    public static class Date {
        public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
        public static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    }

    public static final class Urls {
        public static final String HOST = "https://appserver.icabanken.se";

        public static final URL BANKID_CREATE =
                new URL(HOST + "/api/authentication/mobilebankid/create");
        public static final URL BANKID_COLLECT =
                new URL(HOST + "/api/authentication/mobilebankid/collect/{identifier}");
        public static final URL BANKID_AUTH =
                new URL(HOST + "/api/authentication/mobilebankid/auth");

        public static final URL KEEP_ALIVE = new URL(HOST + "/api/session/heartbeat");
        public static final URL SIGN_TRANSFER_COLLECT_URL =
                new URL(HOST + "/api/bankId/sign/collect/{requestId}");
        public static final URL ACCOUNTS = new URL(HOST + "/api/accounts");
        public static final URL DEPOTS = new URL(HOST + "/api/depots");
        public static final URL FUND_DETAILS = new URL(HOST + "/api/funds/{fundId}");
        public static final URL TRANSACTIONS =
                new URL(HOST + "/api/accounts/{identifier}/transactions");
        public static final URL RESERVED_TRANSACTIONS =
                new URL(HOST + "/api/accounts/{identifier}/reservedTransactions");
        public static final URL UPCOMING_TRANSACTIONS = new URL(HOST + "/api/events/future");
        public static final URL LOAN_OVERVIEW = new URL(HOST + "/api/engagement/loans");
        public static final URL GIRO_DESTINATION_NAME =
                new URL(HOST + "/api/recipients/pgBgRecipientName/{pgnumber}");
        public static final URL UNSIGNED_TRANSFERS = new URL(HOST + "/api/events/unsigned");
        public static final URL TRANSFER_DESTINATIONS = new URL(HOST + "/api/recipients");
        public static final URL TRANSFER_BANKS = new URL(HOST + "/api/accounts/transferBanks");
        public static final URL INIT_TRANSFER_SIGN =
                new URL(HOST + "/api/assignments/bundle/bankid/init");
        public static final URL SIGNED_ASSIGNMENTS =
                new URL(HOST + "/api/assignments/bundle/bankid/submit");
        public static final URL UNSIGNED_ASSIGNMENTS = new URL(HOST + "/api/assignments");
        public static final URL DELETE_UNSIGNED_TRANSFER =
                new URL(HOST + "/api/assignments/bundle/{transferId}");
        public static final URL EINVOICES = new URL(HOST + "/api/egiro/invoices");
        public static final URL INIT_EINVOICE_SIGN =
                new URL(HOST + "/api/egiro/recipient/bankId/init/{invoiceId}");
        public static final URL EVALUATED_POLICIES =
                new URL(HOST + "/api/authorization/evaluatedpolicies");
        public static final URL CUSTOMER = new URL(HOST + "/api/customer");
    }

    public static final class Headers {
        public static final String ACCEPT = "Accept";
        public static final String HEADER_CLIENTAPPVERSION = "ClientAppVersion";
        public static final String VALUE_CLIENTAPPVERSION = "1.65.1";
        public static final String HEADER_APIKEY = "ApiKey";
        public static final String VALUE_APIKEY = "7E5540EE-9903-4272-8401-6AD9ACA455AD";
        public static final String HEADER_API_VERSION = "ApiVersion";
        public static final String VALUE_API_VERSION = "12";
        public static final String HEADER_CLIENT_OS = "ClientOS";
        public static final String VALUE_CLIENT_OS = "iOS";
        public static final String HEADER_CLIENT_OS_VERSION = "ClientOSVersion";
        public static final String VALUE_CLIENT_OS_VERSION = "13.3.1";
        public static final String HEADER_CLIENT_HARDWARE = "ClientHardware";
        public static final String VALUE_CLIENT_HARDWARE = "iPhone";
        public static final String VALUE_USER_AGENT =
                "ICA%20Banken/1.65.1.5 CFNetwork/1121.2.2 Darwin/19.3.0";
    }

    public static final class BankIdErrors {
        public static final String STATUS_FAILED = "failed";
        public static final String NOT_A_CUSTOMER = "no active accounts";
        public static final String INTERRUPTED = "signing not found";
        public static final String NOT_VERIFIED = "konto har ännu inte blivit verifierat";
        public static final String SOMETHING_WENT_WRONG = "något har blivit fel";
        public static final String INVALID_CUSTOMER_ID = "invalid customerid";
    }

    public static final GenericTypeMapper<BankIdStatus, String> BANKID_STATUS_MAPPER =
            GenericTypeMapper.<BankIdStatus, String>genericBuilder()
                    .put(BankIdStatus.DONE, "ok")
                    .put(BankIdStatus.WAITING, "pending")
                    .put(BankIdStatus.CANCELLED, "aborted")
                    .put(BankIdStatus.EXPIRED_AUTOSTART_TOKEN, "timedout")
                    .put(BankIdStatus.FAILED_UNKNOWN, BankIdErrors.STATUS_FAILED)
                    .setDefaultTranslationValue(BankIdStatus.FAILED_UNKNOWN)
                    .build();

    public static final class StatusCodes {
        public static final int OK_RESPONSE = 0;
    }

    public static final class IdTags {
        public static final String DEVICE_APPLICATION_ID = "deviceApplicationId";
        public static final String USER_INSTALLATION_ID = "userInstallationId";
        public static final String SESSION_ID_TAG = "sessionId";
        public static final String IDENTIFIER_TAG = "identifier";
        public static final String POLICIES_TAG = "policies";
        public static final String TO_DATE_TAG = "toDate";
        public static final String FUND_ID_TAG = "fundId";
        public static final String GIRO_NUMBER_TAG = "pgnumber";
        public static final String REQUEST_ID_TAG = "requestId";
        public static final String TRANSFER_ID_TAG = "transferId";
        public static final String INVOICE_ID_TAG = "invoiceId";
        public static final String NOT_AVAILABLE_TAG = "N/A";
        // Used for making a sign request for transfers
        public static final String BUNDLE_TAG = "Bundle";
        public static final String KEY_TAG = "Key";
        public static final String VALUE_TAG = "Value";
        public static final String SWEDISH_AND_SEPARATOR = "och";
    }

    public static final class LoanDetailsKeys {
        public static final String INTEREST_RATE = "aktuell räntesats";
        public static final String LOAN_NAME = "lån";
        public static final String CURRENT_DEBT = "aktuell skuld";
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

        public static final String RECIPIENT_NAME_FIELD_NAME = "name";
        public static final String RECIPIENT_NAME_FIELD_DESCRIPTION = "Mottagarnamn";

        public static final int SOURCE_MSG_MAX_LENGTH = 25;
        public static final int DEST_MSG_MAX_LENGTH = 12;
        public static final int DEST_MSG_MAX_LENGTH_BETWEEN_OWN_ACCOUNTS = 25;
        public static final String WHITELISTED_MSG_CHARS = ",.-?!/+";
        public static final String ERROR_SAVING_RECIPIENT = "Error when saving recipient.";

        public static final String INVALID_REFERENCE_TYPE =
                "du måste ange ett meddelande som referens till mottagaren";
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

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }

    public static final class LogMessage {
        public static final String NO_RECIPIENT_NAME = "Could not get recipient name from user";
        public static final String NO_SAVE_NEW_RECIPIENT_MESSAGE = "Could not save new recipient";
    }

    public static final class Error {
        public static final int MULTIPLE_LOGIN_ERROR_CODE = 1001;
        public static final int GENERIC_ERROR_CODE = 1000;
    }

    public static final class Policies {
        // access accounts, events/future, events/unsigned
        public static final String ACCOUNTS = "AccountOverview";
        // access cards
        public static final String CARDS = "MyCards";
        // access engagement/loans
        public static final String LOANS = "MyLoans";
        // access depots
        public static final String DEPOTS = "MyFundSavingsView";
        // access recipients, accounts/transferBanks, egiro/invoices
        public static final String PAYMENTS = "PaymentAndExternalTransfer";
        public static final String RESULT_OK = "Ok";
    }

    public enum UserMessage implements LocalizableEnum {
        KNOW_YOUR_CUSTOMER(
                new LocalizableKey(
                        "To be able to refresh your accounts you need to update your customer info in your bank app.")),
        EINVOICE_MODIFIED_IN_BANK_APP(
                new LocalizableKey(
                        "If the e-invoice has been modified in the ICA Banken app, please refresh you credentials.")),
        BANKID_TRANSFER_INTERRUPTED(
                new LocalizableKey(
                        "Another BankId authentication was initiated while signing the transfer. Please try again."));

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
