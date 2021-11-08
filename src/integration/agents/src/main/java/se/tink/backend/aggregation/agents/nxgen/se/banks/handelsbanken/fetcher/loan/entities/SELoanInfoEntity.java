package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.loan.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SELoanInfoEntity {
    private String title;
    private List<HandelsbankenSELoan> loans;

    public List<HandelsbankenSELoan> getLoans() {
        return loans != null ? loans : Collections.emptyList();
    }
}
