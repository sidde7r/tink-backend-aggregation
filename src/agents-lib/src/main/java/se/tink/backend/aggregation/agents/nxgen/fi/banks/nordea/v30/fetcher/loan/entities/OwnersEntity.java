package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class OwnersEntity {

    private String name;

    @Override
    public String toString() {
        return name;
    }

    public HolderName getName() {
        return new HolderName(name);
    }
}
