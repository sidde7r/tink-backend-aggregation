package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HolderEntity {

    private String name;
    private Boolean isAlias;

    public String getName() {
        return name;
    }

    public Boolean getAlias() {
        return isAlias;
    }
}
