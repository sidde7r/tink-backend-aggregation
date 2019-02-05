package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CommonEntity {

    @JsonProperty("statut")
    private String status;
    @JsonProperty("raison")
    private String reason;
    private String action;
    @JsonProperty("origine")
    private String origin;

    public String getStatus() {
        return status;
    }

}
