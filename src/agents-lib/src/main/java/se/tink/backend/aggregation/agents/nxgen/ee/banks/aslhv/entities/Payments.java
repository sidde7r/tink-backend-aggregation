package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Payments {

    @JsonProperty("target_portfolio_id")
    private List<String> targetPortfolioId;

    @JsonProperty("enabled")
    private boolean enabled;

    public List<String> getTargetPortfolioId() {
        return targetPortfolioId;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
