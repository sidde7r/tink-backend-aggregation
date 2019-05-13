package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    private List<AccessInfoEntity> accounts;
    private List<AccessInfoEntity> balances;
    private List<AccessInfoEntity> transactions;

    public AccessEntity(
            List<AccessInfoEntity> accounts,
            List<AccessInfoEntity> balances,
            List<AccessInfoEntity> transactions) {
        this.accounts = accounts;
        this.balances = balances;
        this.transactions = transactions;
    }
}
