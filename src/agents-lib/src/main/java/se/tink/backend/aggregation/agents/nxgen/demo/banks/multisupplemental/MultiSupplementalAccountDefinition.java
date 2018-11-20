package se.tink.backend.aggregation.agents.nxgen.demo.banks.multisupplemental;

import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoInvestmentAccountDefinition;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoLoanAccountDefinition;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoSavingsAccountDefinition;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoTransactionAccountDefinition;

public class MultiSupplementalAccountDefinition extends DemoConstants {

    @Override
    public DemoInvestmentAccountDefinition getInvestmentDefinitions() {
        return new DemoInvestmentAccountDefinition() {
            @Override
            public String getAccountId() {
                return "8888-444444444444";
            }

            @Override
            public double getAccountBalance() {
                return 456;
            }
        };
    }

    @Override
    public DemoSavingsAccountDefinition getDemoSavingsDefinitions() {
        return new DemoSavingsAccountDefinition() {
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
    public DemoLoanAccountDefinition getDemoLoanDefinitions() {
        return new DemoLoanAccountDefinition() {
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
    public DemoTransactionAccountDefinition getTransactionalAccountDefinition() {
        return new DemoTransactionAccountDefinition() {
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
