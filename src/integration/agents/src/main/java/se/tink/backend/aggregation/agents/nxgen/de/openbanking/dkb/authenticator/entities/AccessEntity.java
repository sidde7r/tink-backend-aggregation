package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {
    private List<AccessItemEntity> accounts;
    private String allPsd2;
    private String availableAccounts;
    private List<AccessItemEntity> balances;
    private List<AccessItemEntity> transactions;

    public AccessEntity(
            List<AccessItemEntity> accounts,
            List<AccessItemEntity> balances,
            List<AccessItemEntity> transactions,
            String allPsd2,
            String availableAccounts) {
        this.accounts = accounts;
        this.allPsd2 = allPsd2;
        this.availableAccounts = availableAccounts;
        this.balances = balances;
        this.transactions = transactions;
    }
}
