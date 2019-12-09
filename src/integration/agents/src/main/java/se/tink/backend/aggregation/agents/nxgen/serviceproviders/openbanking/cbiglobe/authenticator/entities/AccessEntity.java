package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {
    private List<AccountDetailsEntity> balances;
    private List<AccountDetailsEntity> transactions;
    private String availableAccounts;

    public AccessEntity(String availableAccounts) {
        this.availableAccounts = availableAccounts;
    }

    public AccessEntity(
            List<AccountDetailsEntity> balances, List<AccountDetailsEntity> transactions) {
        this.balances = balances;
        this.transactions = transactions;
    }
}
