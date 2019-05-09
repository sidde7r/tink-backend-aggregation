package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardMembers {

    @JsonProperty("values")
    private List<ValuesItem> values;

    @JsonProperty("header")
    private String header;

    public List<ValuesItem> getValues() {
        return values;
    }

    public String getHeader() {
        return header;
    }
}
