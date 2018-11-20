package se.tink.backend.aggregation.agents.nxgen.demo.banks.bankid;

import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoInvestmentAccountDefinition;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoLoanAccountDefinition;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoSavingsAccountDefinition;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoTransactionAccountDefinition;

public class BankIdAccountDefinition extends DemoConstants {

    @Override
    public DemoInvestmentAccountDefinition getInvestmentDefinitions() {
        return new DemoInvestmentAccountDefinition() {
            @Override
            public String getAccountId() {
                return "9999-444444444444";
            }

            @Override
            public double getAccountBalance() {
                return 123456;
            }
        };
    }

    @Override
    public DemoSavingsAccountDefinition getDemoSavingsDefinitions() {
        return new DemoSavingsAccountDefinition() {
            @Override
            public String getAccountId() {
                return "9999-222222222222";
            }

            @Override
            public String getAccountName() {
                return "Savings Account";
            }

            @Override
            public double getAccountBalance() {
                return 385245.33;
            }
        };
    }

    @Override
    public DemoLoanAccountDefinition getDemoLoanDefinitions() {
        return new DemoLoanAccountDefinition() {
            @Override
            public String getMortgageId() {
                return "9999-333333333333";
            }

            @Override
            public String getBlancoId() {
                return "9999-333334444444";
            }

            @Override
            public String getMortgageLoanName() {
                return "Bolån";
            }

            @Override
            public String getBlancoLoanName() {
                return "Santander";
            }

            @Override
            public double getMortgageInterestName() {
                return 0.19;
            }

            @Override
            public double getBlancoInterestName() {
                return 1.45;
            }

            @Override
            public double getMortgageBalance() {
                return -2300000D;
            }

            @Override
            public double getBlancoBalance() {
                return -50000D;
            }
        };
    }

    @Override
    public DemoTransactionAccountDefinition getTransactionalAccountDefinition() {
        return new DemoTransactionAccountDefinition() {
            @Override
            public String getAccountId() {
                return "9999-111111111111";
            }

            @Override
            public String getAccountName() {
                return "Debt Account";
            }

            @Override
            public double getBalance() {
                return  26245.33;
            }
        };
    }
}
