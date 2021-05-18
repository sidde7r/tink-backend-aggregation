package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class AccountOwnerEntity {
    private String name;
    private String role;

    public boolean isOwner() {
        return "owner".equalsIgnoreCase(role);
    }

    public HolderName getHolderName() {
        return new HolderName(name);
    }
}
