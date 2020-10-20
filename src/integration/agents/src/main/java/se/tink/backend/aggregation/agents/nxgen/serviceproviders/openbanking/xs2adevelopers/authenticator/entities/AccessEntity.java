package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    private List<AccessInfoEntity> accounts;
    private String allPsd2;
    private String availableAccounts;
    private String availableAccountsWithBalances;
    private List<AccessInfoEntity> balances;
    private List<AccessInfoEntity> transactions;

    public AccessEntity(String allPsd2) {
        this.allPsd2 = allPsd2;
    }

    public AccessEntity(List<AccessInfoEntity> accessInfoEntities) {
        this.accounts = accessInfoEntities;
        this.balances = accessInfoEntities;
        this.transactions = accessInfoEntities;
    }
}
