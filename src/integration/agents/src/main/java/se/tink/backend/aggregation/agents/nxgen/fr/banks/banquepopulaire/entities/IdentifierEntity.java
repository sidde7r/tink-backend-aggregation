package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentifierEntity {
    @JsonProperty("codeBanque")
    private String bankCode;

    @JsonProperty("identifiant")
    private String identifier;

    public String getBankCode() {
        return bankCode;
    }

    public String getIdentifier() {
        return identifier;
    }
}
