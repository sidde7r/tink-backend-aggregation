package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProcessDate {

    @JsonProperty("rawValue")
    private int rawValue;

    @JsonProperty("formattedDate")
    private String formattedDate;

    public int getRawValue() {
        return rawValue;
    }

    public String getFormattedDate() {
        return formattedDate;
    }
}
