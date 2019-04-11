package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CharacterEntity {

    @JsonProperty("must_recheck")
    private int mustRecheck;

    @JsonProperty("delai_val")
    private int delaiVal;

    @JsonProperty("chiffrement")
    private boolean encrypted;

    @JsonProperty("compression")
    private boolean compressed;

    public boolean isEncrypted() {
        return encrypted;
    }

    public boolean isCompressed() {
        return compressed;
    }
}
