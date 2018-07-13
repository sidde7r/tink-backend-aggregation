package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MortgageListEntity {
    @JsonProperty("Mortgages")
    private List<MortgagesEntity> mortgages;
    @JsonProperty("TotalDebt")
    private double totalDebt;

    public List<MortgagesEntity> getMortgages() {
        return mortgages;
    }

    public double getTotalDebt() {
        return totalDebt;
    }
}
