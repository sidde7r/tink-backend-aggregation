package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaTransactionLinksEntity {

    private Href self;

    @JsonProperty("parent-list")
    private Href parentList;

    private Href balances;
    private Href last;
    private Href next;
}
