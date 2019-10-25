package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

public class Fields {

    static class Account {
        /*There are differences between labels in application and in the API:
        App Label          | Api field
        ------------------------------------------
        Authorized Balance | numAvailableBalance
        Available Balance  | numAuthorizedBalance
        Accounting Balance | numClosingBalance
        */

        static final String IBAN = "IBAN";
        static final String ACCOUNT_NAME = "accountDescription";
        static final String ACCOUNT_NUMBER = "accountNumber";
        static final String BIC = "BIC";
        static final String AVAILABLE_BALANCE = "numAuthorizedBalance";
        static final String CURRENCY_NUMERIC_CODE = "currency";
        static final String PRODUCT_NAME = "number";
        static final String BRANCH_CODE = "branchCode";
        static final String ACCOUNT_TYPE = "FAMILIA";
    }

    static class Transaction {
        static final String AMOUNT = "amount";
        static final String RAW_OPERATION_DATE = "rawOperationDate";
        static final String DESCRIPTION = "description";
        public static final String CURRENCY = "CTRCMOEA";
        public static final String NUMBER_OF_UNITS = "nUPs";
        public static final String OPERATION_DATE = "operationDate";
    }

    public static class Session {
        public static final String SESSION_TOKEN = "id";
        public static final String CUSTOMER_NAME = "userName";
        public static final String VARIABLES = "variables";
    }

    public static class Investment {
        static final String AVAILABLE_BALANCE = "availableBalance";
        static final String BALANCE = "balance";
        static final String CURRENCY_NUMERIC_CODE = "currency";
        static final String ACCOUNT_NUMBER = "accountNumber";
        static final String FULL_ACCOUNT_NUMBER = "account";
        static final String PRODUCT_NAME = "number";
    }

    public static class Assets {
        static final String RETIREMENT_INVESTMENTS = "PR";
        static final String INVESTMENT_ACCOUNTS = "FU";
        static final String CREDIT_CARDS = "CR";
        static final String LOANS = "EP";
    }

    public static class Identity {
        public static final String USER_NAME = "userName";
        public static final String BIRTH_DATE = "birthDate";
    }

    public static class Card {
        public static final String TYPE_CREDIT = "credit";
        public static final String PRODUCT_CARD_TYPE = "productCardType";
        public static final String CURRENCY = "currency";
        public static final String MASKED_NUMBER = "partialNumber";
        public static final String AUTHORIZED_BALANCE = "authorizedBalance";
        public static final String AVAILABLE = "available";
        public static final String ALIAS = "numAlias";
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String PRODUCT_NAME = "name";
        public static final String FULL_NUMBER = "number";
    }
}
