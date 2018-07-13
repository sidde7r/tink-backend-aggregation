package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractEntity {
    private String id;

    public String getId() {
        return id;
    }

    public ContractEntity setId(String id) {
        this.id = id;
        return this;
    }
}
