package se.tink.backend.aggregation.nxgen.agents.demo;

public class DemoConstants {

    public static String BASE_PATH = "data/demo";
    public static String GENERATION_BASE_FILE = "generationbase.json";

    public static class SavingsAccountInformation {
        public static final String ACCOUNT_ID = "9999-222222222222";
        public static final double ACCOUNT_BALANCE = 385245.33;
        public static final String ACCOUNT_NAME = "Savings Account";
    }

    public static class CheckingAccountInformation {
        public static final String ACCOUNT_ID = "9999-111111111111";
        public static final double ACCOUNT_BALANCE = 26245.33;
        public static final String ACCOUNT_NAME = "Debt Account";
    }

    public static class LoanAccountInformation {
        public static final String MORTGAGE_ID = "9999-333333333333";
        public static final String BLANCO_ID = "9999-333334444444";
        public static final String MORTGAGE_LOAN_NAME = "Bolån";
        public static final String BLANCO_LOAN_NAME = "Santander";
        public static final double MORTGAGE_INTEREST_RATE = 0.19;
        public static final double BLANCO_INTEREST_RATE = 1.45;
        public static final double MORTGAGE_BALANCE = -2300000D;
        public static final double BLANCO_BALANCE = -50000D;
    }

    public static class InvestmentAccountInformation {
        public static final String INVESTMENT_ACCOUNT_ID = "9999-444444444444";
        public static final double INVESTMENT_BALANCE = 123456;
    }

    //Change to take amount and return the conversion
    public static double getSekToCurrencyConverter(String currency, double amountInSek) {
        double conversionAmount;
        switch (currency) {
        case "EUR" :
            conversionAmount = 10.22;
        case "USD" :
            conversionAmount = 9.06;
        case "GBP":
            conversionAmount = 11.77;
        default:
            conversionAmount = 1;
        }

        return amountInSek/conversionAmount;
    }
}
