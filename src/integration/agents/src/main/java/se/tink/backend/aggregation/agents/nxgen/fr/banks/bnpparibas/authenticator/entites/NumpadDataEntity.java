package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.entites;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NumpadDataEntity {
    @JsonProperty("idGrille")
    private String gridId;

    @JsonProperty("grille")
    private String grid;

    private String template;

    public String getGridId() {
        return gridId;
    }

    public String getGrid() {
        return grid;
    }

    public String getTemplate() {
        return template;
    }
}
