package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RibListEntity {
    @JsonProperty("listeRib")
    private List<RibEntity> ribList;

    public List<RibEntity> getRibList() {
        return ribList;
    }
}
