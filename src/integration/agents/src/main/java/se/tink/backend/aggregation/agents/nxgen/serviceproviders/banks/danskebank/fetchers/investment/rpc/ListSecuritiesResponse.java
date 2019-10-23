package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class ListSecuritiesResponse extends AbstractResponse {
    private double marketValue;
    private String marketValueCurrency;
    private double performance;
    private double performancePct;
    private List<SecurityEntity> securities;

    public double getMarketValue() {
        return marketValue;
    }

    public String getMarketValueCurrency() {
        return marketValueCurrency;
    }

    public double getPerformance() {
        return performance;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }

    public double getPerformancePct() {
        return performancePct;
    }

    public void setPerformancePct(double performancePct) {
        this.performancePct = performancePct;
    }

    public List<SecurityEntity> getSecurities() {
        return securities;
    }

    @JsonIgnore
    public Amount getTinkAmount() {
        return new Amount(marketValueCurrency, marketValue);
    }
}
