package se.tink.backend.aggregation.agents.nxgen.demo.banks.bankid;

import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoInvestmentAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoLoanAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoTransactionAccount;

public class BankIdAccountDefinition extends DemoConstants {

    @Override
    public DemoInvestmentAccount getInvestmentDefinitions() {
        return new DemoInvestmentAccount() {
            @Override
            public String getAccountId() {
                return "9999-444444444444";
            }

            @Override
            public String getName() {
                return "Investment";
            }

            @Override
            public double getAccountBalance() {
                return 123456;
            }
        };
    }

    @Override
    public DemoSavingsAccount getDemoSavingsDefinitions() {
        return new DemoSavingsAccount() {
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
    public DemoLoanAccount getDemoLoanDefinitions() {
        return new DemoLoanAccount() {
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
    public DemoTransactionAccount getTransactionalAccountDefinition() {
        return new DemoTransactionAccount() {
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
