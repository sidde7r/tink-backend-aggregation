package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerResponse {
    @JsonProperty("namn")
    private String name;
    @JsonProperty("kort")
    private boolean card;
    @JsonProperty("spar")
    private boolean savings;
    @JsonProperty("finansiering")
    private boolean finance;

    public String getName() {
        return name;
    }

    public boolean hasCard() {
        return card;
    }

    public boolean hasSavings() {
        return savings;
    }

    public boolean hasFinance() {
        return finance;
    }
}
