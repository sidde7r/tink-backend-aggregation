package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class BalancesLinksEntity {
    private Href self;
    private Href transactions;

    @JsonProperty("parent-list")
    private Href parentList;
}
