package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class IcaBankenConstants {

    public static final class Urls {
        public static final String ENTRY_POINT = "https://appserver.icabanken.se";
        public static final URL LOGIN_BANKID = new URL(ENTRY_POINT + "/api/session/login/bankid/{identifier}");
        public static final URL SIGN_TRANSFER_COLLECT_URL =new URL(ENTRY_POINT + "/api/bankId/sign/collect/{requestId}");
        public static final URL ACCOUNTS = new URL(ENTRY_POINT + "/api/accounts");
        public static final URL INVESTMENTS = new URL(ENTRY_POINT + "/api/depots");
        public static final URL INSTRUMENT = new URL(ENTRY_POINT + "/api/funds/{fundId}");
        public static final URL TRANSACTIONS = new URL(ENTRY_POINT + "/api/accounts/{identifier}/transactions");
        public static final URL RESERVED_TRANSACTIONS = new URL(ENTRY_POINT + "/api/accounts/{identifier}/reservedTransactions");
        public static final URL LOAN_OVERVIEW = new URL(ENTRY_POINT + "/api/engagement/loans");
        public static final URL HEARTBEAT = new URL(ENTRY_POINT + "/api/session/heartbeat");
        public static final URL GIRO_DESTINATION_NAME = new URL(ENTRY_POINT + "/api/recipients/pgBgRecipientName/{pgnumber}");
        public static final URL UNSIGNED_TRANSFERS_URL = new URL(ENTRY_POINT + "/api/events/unsigned");
        public static final URL TRANSFER_DESTINATIONS_URL = new URL(ENTRY_POINT + "/api/recipients");
        public static final URL TRANSFER_BANKS_URL = new URL(ENTRY_POINT + "/api/accounts/transferBanks");
        public static final URL INIT_TRANSFER_SIGN_URL = new URL(ENTRY_POINT + "/api/assignments/bundle/bankid/init");
        public static final URL SIGNED_ASSIGNMENTS_URL = new URL(ENTRY_POINT + "/api/assignments/bundle/bankid/submit?");
        public static final URL UNSIGNED_ASSIGNMENTS_URL = new URL(ENTRY_POINT + "/api/assignments");
        public static final URL DELETE_UNSIGNED_TRANSFER_URL = new URL(ENTRY_POINT + "/api/assignments/bundle/{transferId}");
        public static final URL UPCOMING_TRANSACTIONS_URL = new URL(ENTRY_POINT + "/api/events/future");
        public static final URL ACCEPT_EINVOICE_URL = new URL(ENTRY_POINT + "/api/egiro/invoice/accept");
        public static final URL VALIDATE_INVOICE_URL = new URL(ENTRY_POINT + "/api/egiro/invoice/validate");
        public static final URL EINVOICES_URL = new URL(ENTRY_POINT + "/api/egiro/invoices");
        public static final URL UPDATE_INVOICE_URL = new URL (ENTRY_POINT + "/api/egiro/invoice/update");
        public static final URL INIT_EINVOICE_SIGN_URL = new URL (ENTRY_POINT + "/api/egiro/recipient/bankId/init/{invoiceId}");
        public static final URL MORTGAGES_URL = new URL(ENTRY_POINT + "/api/engagement");
    }

    public static final class Headers {
        public static final String HEADER_CLIENTAPPVERSION = "ClientAppVersion";
        public static final String VALUE_CLIENTAPPVERSION = "1.34.1";
        public static final String HEADER_USERINSTALLATIONID = "UserInstallationId";
        public static final String VALUE_USERINSTALLATIONID = "be631049-fc80-4200-aff6-a1d3b2a4cb46";
        public static final String HEADER_USERAGENT = "User-Agent";
        public static final String VALUE_USERAGENT = "ICA Banken/1.34.1 (iPhone; iOS 10.1.1; Scale/2.00)";
        public static final String HEADER_APIKEY = "ApiKey";
        public static final String VALUE_APIKEY = "D520547D-05A3-4189-8139-74C41CD52965";
        public static final String HEADER_CLIENT_OS = "ClientOS";
        public static final String VALUE_CLIENT_OS = "iOS";
        public static final String HEADER_CLIENT_OS_VERSION = "ClientOSVersion";
        public static final String VALUE_CLIENT_OS_VERSION = "10.3.1";
        public static final String HEADER_CLIENT_HARDWARE = "ClientHardware";
        public static final String VALUE_CLIENT_HARDWARE = "iPhone";
    }

    public static final class IdTags{
        public static String SESSION_ID_TAG = "sessionId";
        public static String IDENTIFIER_TAG = "identifier";
        public static String FROM_DATE_TAG = "fromDate";
        public static String TO_DATE_TAG = "toDate";
        public static String FUND_ID_TAG = "fundId";
        public static String GIRO_NUMBER_TAG = "pgNumber";
        public static String REQUEST_ID_TAG = "requestId";
        public static String TRANSFER_ID_TAG = "transferId";
        public static String INVOICE_ID_TAG = "invoiceId";
        public static String NOT_AVAILABLE_TAG = "N/A";
        //Used for making a sign request for transfers
        public static String BUNDLE_TAG = "Bundle";
        public static String KEY_TAG = "Key";
        public static String VALUE_TAG = "Tag";
        public static String SWEDISH_AND_SEPARATOR = "och";
    }

    public static final class IcaMessages{
        public static String INTEREST_RATE = "Aktuell räntesats";
        public static String LOAN_NAME = "Lån";
        public static String INITIAL_DEBT = "Ursprunglig skuld";
        public static String INITIAL_DATE = "Utbetalningsdag";
        public static String APPLICANTS = "Aktuell räntesats";
        public static String NEXT_DAY_OF_TERMS_CHANGE = "Nästa villkorsändringsdag";
        public static String MONTH_BOUND = "Räntebindningstid";
        public static String SECURITY = "Säkerhet";
        public static String TYPE_OF_SECURITY = "Typ av objekt";
        public static String TYPE_OF_LOAN = "Typ av lån";
    }

    public static final class Currencies{
        public static String SEK = "SEK";
    }

    public enum AccountType {
        ICA_ACCOUNT("IcaAccount", AccountTypes.CHECKING),
        SAVINGSACCOUNT("SavingsAccount", AccountTypes.SAVINGS),
        UNKOWN("", AccountTypes.OTHER);

        AccountType(String icaAccountType, AccountTypes tinkType) {
            this.icaAccountType = icaAccountType;
            this.tinkType = tinkType;
        }

        private final String icaAccountType;
        private final AccountTypes tinkType;

        public static AccountType toAccountType(String sAccountType) {
            for (AccountType accountType : AccountType.values()) {
                if (accountType.icaAccountType.equalsIgnoreCase(sAccountType)) {
                    return accountType;
                }
            }

            return AccountType.UNKOWN;
        }

        public AccountTypes getTinkType() {
            return tinkType;
        }

        public String getIcaBankenAccountType() {
            return icaAccountType;
        }
    }

}
