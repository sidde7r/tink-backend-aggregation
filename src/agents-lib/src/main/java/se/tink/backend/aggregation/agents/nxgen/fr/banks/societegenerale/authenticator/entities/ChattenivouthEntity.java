package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChattenivouthEntity {
    @JsonProperty("list_proc")
    private List<ListProcEntity> listProc;
    @JsonProperty("cible")
    private String target;
}
