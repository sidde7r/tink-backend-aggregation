package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {
    @JsonProperty private List<AccountInfoEntity> accounts;

    @JsonProperty private List<AccountInfoEntity> balances;

    @JsonProperty private List<AccountInfoEntity> transactions;

    @JsonProperty private String availableAccounts;

    @JsonProperty private String allPsd2;

    public AccessEntity(
            List<AccountInfoEntity> accounts,
            List<AccountInfoEntity> balances,
            List<AccountInfoEntity> transactions,
            String availableAccounts,
            String allPsd2) {
        this.accounts = accounts;
        this.balances = balances;
        this.transactions = transactions;
        this.availableAccounts = availableAccounts;
        this.allPsd2 = allPsd2;
    }
}
