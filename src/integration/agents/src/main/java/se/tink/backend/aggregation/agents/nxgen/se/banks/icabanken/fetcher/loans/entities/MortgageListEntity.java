package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MortgageListEntity {
    @JsonProperty("Mortgages")
    private List<MortgageEntity> mortgages;
    @JsonProperty("TotalDebt")
    private double totalDebt;

    public List<MortgageEntity> getMortgages() {
        return mortgages;
    }

    public double getTotalDebt() {
        return totalDebt;
    }
}
