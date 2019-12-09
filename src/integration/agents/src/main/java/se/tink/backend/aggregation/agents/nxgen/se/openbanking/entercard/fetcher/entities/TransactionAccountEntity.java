package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAccountEntity {

    List<TransactionEntity> movements;
    private String accountNumber;

    @JsonProperty("_links")
    private LinksEntity links;

    public List<TransactionEntity> getMovements() {
        return movements;
    }
}
