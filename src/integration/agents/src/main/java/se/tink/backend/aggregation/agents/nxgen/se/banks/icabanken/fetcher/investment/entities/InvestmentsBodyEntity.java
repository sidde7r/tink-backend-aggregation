package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class InvestmentsBodyEntity {
    @JsonProperty("Depots")
    private List<DepotEntity> depots;
    @JsonProperty("TotalBalance")
    private double totalBalance;
    @JsonProperty("TotalValue")
    private double totalValue;
    @JsonProperty("NextMonthlySavingsStartDate")
    private String nextMonthlySavingsStartDate;

    public List<DepotEntity> getDepots() {
        return depots;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public String getNextMonthlySavingsStartDate() {
        return nextMonthlySavingsStartDate;
    }
}
