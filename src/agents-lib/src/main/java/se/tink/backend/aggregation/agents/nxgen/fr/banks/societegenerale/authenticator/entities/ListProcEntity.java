package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListProcEntity {
    @JsonProperty("type_proc")
    private String typeProc;
    @JsonProperty("priorite")
    private String priority;
}
