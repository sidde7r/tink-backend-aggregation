package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

public class Fields {

    static class Account {
        static final String IBAN = "IBAN";
        static final String ACCOUNT_NAME = "accountDescription";
        static final String ACCOUNT_NUMBER = "accountNumber";
        static final String BIC = "BIC";
        static final String AVAILABLE_BALANCE = "numAvailableBalance";
        static final String CURRENCY_NUMERIC_CODE = "currency";
        static final String PRODUCT_NAME = "number";
        static final String BRANCH_CODE = "branchCode";
    }

    static class Transaction {
        static final String AMOUNT = "amount";
        static final String OPERATION_DATE = "rawOperationDate";
        static final String DESCRIPTION = "description";
    }

    public static class Session {
        public static final String SESSION_TOKEN = "id";
    }
}
