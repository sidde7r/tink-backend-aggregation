package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TypeValuePair {
    @JsonProperty("T")
    private String type;

    @JsonProperty("V")
    private String value;

    @JsonProperty("E")
    private String identifier;

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    // Dummy constructor to satisfy Jackson
    public TypeValuePair() {}

    @JsonIgnore
    private TypeValuePair(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @JsonIgnore
    public static TypeValuePair create(String type, String value) {
        return new TypeValuePair(type, value);
    }

    @JsonIgnore
    public static TypeValuePair createBoolean(boolean value) {
        return new TypeValuePair(KbcConstants.PairTypeTypes.BOOLEAN, String.valueOf(value));
    }

    @JsonIgnore
    public static TypeValuePair createText(String value) {
        return new TypeValuePair(KbcConstants.PairTypeTypes.TEXT, String.valueOf(value));
    }
}
