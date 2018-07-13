package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TypeValueEncodedTriplet {
    @JsonProperty("V")
    private String value;
    @JsonProperty("E")
    private String encoded;
    @JsonProperty("T")
    private String type;

    // Dummy constructor to satisfy Jackson
    public TypeValueEncodedTriplet() {
    }

    @JsonIgnore
    private TypeValueEncodedTriplet(String value, String encoded, String type) {
        this.value = value;
        this.encoded = encoded;
        this.type = type;
    }

    @JsonIgnore
    public static TypeValueEncodedTriplet create(String value, String encoded, String type) {
        return new TypeValueEncodedTriplet(value, encoded, type);
    }

    public String getValue() {
        return value;
    }

    public String getEncoded() {
        return encoded;
    }

    public String getType() {
        return type;
    }
}
