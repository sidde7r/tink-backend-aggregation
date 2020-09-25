package se.tink.backend.aggregation.nxgen.agents.demo.data;

import java.time.LocalDate;

public interface DemoLoanAccount {
    String getMortgageId();

    String getBlancoId();

    String getMortgageLoanName();

    String getBlancoLoanName();

    double getMortgageInterestName();

    double getBlancoInterestName();

    double getMortgageBalance();

    double getBlancoBalance();

    LocalDate getInitialDate();
}
