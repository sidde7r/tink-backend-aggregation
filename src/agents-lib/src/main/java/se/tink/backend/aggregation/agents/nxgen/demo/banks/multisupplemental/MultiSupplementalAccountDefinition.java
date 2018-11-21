package se.tink.backend.aggregation.agents.nxgen.demo.banks.multisupplemental;

import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoInvestmentAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoLoanAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoTransactionAccount;

public class MultiSupplementalAccountDefinition extends DemoConstants {

    @Override
    public DemoInvestmentAccount getInvestmentDefinitions() {
        return new DemoInvestmentAccount() {
            @Override
            public String getAccountId() {
                return "8888-444444444444";
            }

            @Override
            public String getName() {
                return "Small Investment";
            }

            @Override
            public double getAccountBalance() {
                return 456;
            }
        };
    }

    @Override
    public DemoSavingsAccount getDemoSavingsDefinitions() {
        return new DemoSavingsAccount() {
            @Override
            public String getAccountId() {
                return "8888-222222222222";
            }

            @Override
            public String getAccountName() {
                return "Savings Account";
            }

            @Override
            public double getAccountBalance() {
                return 245.33;
            }
        };
    }

    @Override
    public DemoLoanAccount getDemoLoanDefinitions() {
        return new DemoLoanAccount() {
            @Override
            public String getMortgageId() {
                return "8888-333333333333";
            }

            @Override
            public String getBlancoId() {
                return "8888-333334444444";
            }

            @Override
            public String getMortgageLoanName() {
                return "Loan";
            }

            @Override
            public String getBlancoLoanName() {
                return "Tesco Invest";
            }

            @Override
            public double getMortgageInterestName() {
                return 0.53;
            }

            @Override
            public double getBlancoInterestName() {
                return 1.73;
            }

            @Override
            public double getMortgageBalance() {
                return -2300D;
            }

            @Override
            public double getBlancoBalance() {
                return -5D;
            }
        };
    }

    @Override
    public DemoTransactionAccount getTransactionalAccountDefinition() {
        return new DemoTransactionAccount() {
            @Override
            public String getAccountId() {
                return "8888-111111111111";
            }

            @Override
            public String getAccountName() {
                return "Checking Account";
            }

            @Override
            public double getBalance() {
                return  25.33;
            }
        };
    }
}
