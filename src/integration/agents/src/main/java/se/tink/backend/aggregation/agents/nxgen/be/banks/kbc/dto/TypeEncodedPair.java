package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TypeEncodedPair {
    @JsonProperty("T")
    private String type;
    @JsonProperty("E")
    private String encoded;

    public String getType() {
        return type;
    }

    public String getEncoded() {
        return encoded;
    }

    // Dummy constructor to satisfy Jackson
    public TypeEncodedPair() {
    }

    private TypeEncodedPair(String type, String encoded) {
        this.type = type;
        this.encoded = encoded;
    }

    @JsonIgnore
    public static TypeEncodedPair create(String type, String value) {
        return new TypeEncodedPair(type, value);
    }

    @JsonIgnore
    public static TypeEncodedPair createHidden(String value) {
        return new TypeEncodedPair(KbcConstants.PairTypeTypes.HIDDEN, value);
    }
}
