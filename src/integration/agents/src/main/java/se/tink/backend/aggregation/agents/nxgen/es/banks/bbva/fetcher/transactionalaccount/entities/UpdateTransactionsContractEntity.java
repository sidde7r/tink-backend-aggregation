package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class UpdateTransactionsContractEntity {

    private String id;

    public String getId() {
        return id;
    }

    public UpdateTransactionsContractEntity setId(String id) {
        this.id = id;
        return this;
    }
}
