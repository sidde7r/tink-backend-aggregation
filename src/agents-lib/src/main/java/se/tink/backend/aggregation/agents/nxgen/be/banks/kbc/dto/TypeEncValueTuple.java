package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TypeEncValueTuple {
    @JsonProperty("T")
    private String type;
    @JsonProperty("E")
    private String enc;
    @JsonProperty("V")
    private String value;

    public String getType() {
        return type;
    }

    public String getEnc() {
        return enc;
    }

    public String getValue() {
        return value;
    }

}