package se.tink.backend.aggregation.nxgen.agents.demo.definitions;

public interface DemoLoanAccountDefinition {
    String getMortgageId();
    String getBlancoId();
    String getMortgageLoanName();
    String getBlancoLoanName();
    double getMortgageInterestName();
    double getBlancoInterestName();
    double getMortgageBalance();
    double getBlancoBalance();
}
