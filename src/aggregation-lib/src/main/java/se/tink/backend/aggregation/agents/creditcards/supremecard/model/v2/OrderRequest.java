package se.tink.backend.aggregation.agents.creditcards.supremecard.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderRequest {
    @JsonProperty("subject")
    private String subject = "";
    @JsonProperty("useAnotherDevice")
    private boolean useAnotherDevice = false;
}
