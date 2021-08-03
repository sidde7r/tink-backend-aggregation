package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MontepioConstants {

    public static final int MAX_TRANSACTION_HISTORY_MONTHS = 6;
    public static final String TRANSACTIONS_FETCH_ERROR_FORMAT =
            "Cannot fetch transactions, bank returned error with [code=%s, message=%s]";
    public static final String DEFAULT_CURRENCY = "EUR";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class URLs {
        public static final URL LOGIN = new URL(Endpoints.LOGIN);
        public static final URL FINALIZE_LOGIN = new URL(Endpoints.FINALIZE_LOGIN);
        public static final URL FETCH_ACCOUNTS = new URL(Endpoints.FETCH_ACCOUNTS);
        public static final URL FETCH_TRANSACTIONS = new URL(Endpoints.FETCH_TRANSACTIONS);
        public static final URL FETCH_SAVINGS_ACCOUNTS = new URL(Endpoints.FETCH_SAVINGS_ACCOUNTS);
        public static final URL FETCH_SAVINGS_ACCOUNT_TRANSACTIONS =
                new URL(Endpoints.FETCH_SAVINGS_ACCOUNT_TRANSACTIONS);
        public static final URL FETCH_ACCOUNT_DETAILS = new URL(Endpoints.FETCH_ACCOUNT_DETAILS);
        public static final URL FETCH_CREDIT_CARDS = new URL(Endpoints.FETCH_CREDIT_CARDS);
        public static final URL FETCH_CREDIT_CARD_TRANSACTIONS =
                new URL(Endpoints.FETCH_CREDIT_CARD_TRANSACTIONS);
        public static final URL FETCH_CREDIT_CARD_DETAILS =
                new URL(Endpoints.FETCH_CREDIT_CARD_DETAILS);
        public static final URL FETCH_LOAN_ACCOUNTS = new URL(Endpoints.FETCH_LOAN_ACCOUNTS);
        public static final URL FETCH_LOAN_ACCOUNT_DETAILS =
                new URL(Endpoints.FETCH_LOAN_ACCOUNT_DETAILS);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Endpoints {

        public static final String BASE = "https://mobweb.montepio.pt/";
        public static final String LOGIN = BASE + "publicMG/LoginTransactionStep0";
        public static final String FINALIZE_LOGIN = BASE + "publicMG/LoginTransactionStep1";
        public static final String FETCH_ACCOUNTS =
                BASE + "privateMG/currentAccount/CurrentAccountsTransaction";
        public static final String FETCH_ACCOUNT_DETAILS =
                BASE + "privateMG/currentAccount/CurrentAccountDetailsTransaction";
        public static final String FETCH_TRANSACTIONS =
                BASE + "privateMG/currentAccount/CurrentAccountTransactionsTransaction";
        public static final String FETCH_SAVINGS_ACCOUNTS =
                BASE + "privateMG/savingAccount/SavingAccountWithdrawStep0";
        public static final String FETCH_SAVINGS_ACCOUNT_TRANSACTIONS =
                BASE + "privateMG/savingAccount/SavingAccountTransactionsTransaction";
        public static final String FETCH_CREDIT_CARDS =
                BASE + "privateMG/card/CreditCardsTransaction";
        public static final String FETCH_CREDIT_CARD_TRANSACTIONS =
                BASE + "privateMG/card/CreditCardTransactionsTransaction";
        public static final String FETCH_CREDIT_CARD_DETAILS =
                BASE + "privateMG/card/CreditCardDetailsTransaction";
        public static final String FETCH_LOAN_ACCOUNTS =
                BASE + "/privateMG/loanAccount/LoanAccountsTransaction";
        public static final String FETCH_LOAN_ACCOUNT_DETAILS =
                BASE + "/privateMG/loanAccount/LoanAccountDetailsTransaction";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Crypto {

        public static final String SALT_PATTERN = "IPHONE%s";
        public static final String PASSWORD_ENCRYPTION_KEY =
                "ZWJhbmtJVCB8IE9tbmljaGFubmVsIElubm92YXRpb24=";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderKeys {
        public static final String APP_VERSION = "ITSAPP-VER";
        public static final String APP_ID = "MGAppId";
        public static final String DEVICE = "ITSAPP-DEVICE";
        public static final String LANG = "ITSAPP-LANG";
        public static final String IOS_VERSION = "ITSAPP-SO";
        public static final String MGM_VERSION = "MGMdwVersion";
        public static final String PSU_IP = "MGIP";
        public static final String SCREEN_NAME = "MGScreen";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderValues {
        public static final String ACCEPT_ENCODING = "br, gzip, deflate";
        public static final String APP_VERSION = "2.38";
        public static final String APP_ID = "iOS-Mobile";
        public static final String DEVICE = "IPHONE";
        public static final String LANG = "pt-PT";
        public static final String IOS_VERSION = "12.4";
        public static final String MGM_VERSION = "5";
        public static final String ACCEPT = "*/*";
        public static final String ACCEPT_LANGUAGE = "en;q=1";
        public static final String PSU_IP = "0.0.0.0";
        public static final String ACCOUNTS_SCREEN_NAME = "AccountsMovementsViewController_P";
        public static final String TRANSACTIONS_SCREEN_NAME = "AccountsDocumentsViewController_P";
        public static final String SAVINGS_ACCOUNTS_SCREEN_NAME =
                "SavingsMobilizationViewController_P";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorMessages {
        public static final String INVALID_LOGIN = "MG:7";
        public static final String INVALID_PASSWORD = "MG:4";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FieldValues {
        public static final String DEVICE_MODEL = "Tink";
        public static final String PSU_IP = "0.0.0.0";
        public static final int CREDENTIAL_TYPE = 0;
        public static final String CLIENT_TYPE = "0";
        public static final String LATITUDE = "0.000000";
        public static final String LONGTITUDE = "0.000000";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PropertyKeys {
        public static final String HANDLE = "handle";
        public static final String IBAN_DETAILS_KEY = "Iban";
        public static final String LOAN_INTEREST_DETAILS_KEY = "Taxa Líquida";
        public static final String LOAN_HOLDER_NAME_DETAILS_KEY = "Titular";
        public static final String LOAN_INITIAL_BALANCE_DETAILS_KEY = "Montante contratação";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LoanTypes {
        public static final String MORTGAGE = "M HABITAÇÃO";
        public static final String INDIVUDUAL_CREDIT = "MONTEPIO CRÉDITO INDIVIDUAL";
        public static final String MORTGAGE_ADDON = "MONTEPIO LAR MAIS EUR 3M";
    }
}
