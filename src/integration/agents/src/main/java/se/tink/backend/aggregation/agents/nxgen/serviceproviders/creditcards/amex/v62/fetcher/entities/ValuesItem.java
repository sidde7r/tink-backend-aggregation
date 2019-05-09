package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValuesItem {

    @JsonProperty("label")
    private String label;

    @JsonProperty("type")
    private String type;

    @JsonProperty("defaultOption")
    private boolean defaultOption;

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public boolean isDefaultOption() {
        return defaultOption;
    }
}
