package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanListEntity {
    @JsonProperty("Loans")
    private List<LoanEntity> loans;
    @JsonProperty("TotalDebt")
    private double totalDebt;

    public List<LoanEntity> getLoans() {
        return loans;
    }

    public double getTotalDebt() {
        return totalDebt;
    }
}
