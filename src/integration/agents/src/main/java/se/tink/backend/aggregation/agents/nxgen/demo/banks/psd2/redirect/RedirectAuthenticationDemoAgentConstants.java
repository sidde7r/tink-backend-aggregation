package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect;

public class RedirectAuthenticationDemoAgentConstants {

    public static class CreditCard {
        public static final String ACCOUNTID = "1122 3344 - 1234";
        public static final String CREDITCARDNUMBER = "1234 5678 9101 1121";
        public static final String NAMEONCREDITCARD = "Tink Tinkerton";
        public static final String ACCOUNTNAME = "Basic Credit Card";
        public static final double BALANCE = -1456D;
        public static final double AVAILABLECREDIT = 8543D;
    }

    public static class LoanAccount {
        public static final String MORTGAGEID = "7777-333333333333";
        public static final String BLANCOID = "7777-333334444444";
        public static final String MORTGAGELOANNAME = "Loan";
        public static final String BLANCOLOANNAME = "Santander";
        public static final double MORTGAGEINTERESTNAME = 0.53;
        public static final double BLANCOINTERESTNAME = 1.73;
        public static final double MORTGAGEBALANCE = -2300D;
        public static final double BLANCOBALANCE = -5D;
    }

    public static class IdentityData {
        public static final String FIRST_NAME = "Jane";
        public static final String SURNAME = "Doe";
    }

    public static class InvestmentAccounts {
        public static final String ACCOUNTID = "7777-444444444444";
        public static final String ACCOUNTNAME = "SmallInvestment";
        public static final double ACCOUNTBALANCE = 4563;
    }
}
