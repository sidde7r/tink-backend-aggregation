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
        static final String OPERATION_DATE = "rawOperationDate";
        static final String DESCRIPTION = "description";
    }

    public static class Session {
        public static final String SESSION_TOKEN = "id";
        public static final String CUSTOMER_NAME = "userName";
        public static final String VARIABLES = "variables";
    }

    public static class Investment {
        static final String AVAILABLE_BALANCE = "availableBalance";
        static final String CURRENCY_NUMERIC_CODE = "currency";
        static final String FULL_ACCOUNT_NUMBER = "account";
        static final String PRODUCT_NAME = "number";
    }

    public static class Assets {
        static final String RETIREMENT_INVESTMENTS = "PR";
        static final String INVESTMENT_ACCOUNTS = "FU";
        static final String CREDIT_CARDS = "CR";
        static final String LOANS = "EP";
    }
}
