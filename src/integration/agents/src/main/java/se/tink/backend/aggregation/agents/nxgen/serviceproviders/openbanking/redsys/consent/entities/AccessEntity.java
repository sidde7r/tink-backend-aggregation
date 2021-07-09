package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AccessEntity {
    @JsonProperty private List<AccountInfoEntity> accounts;

    @JsonProperty private List<AccountInfoEntity> balances;

    @JsonProperty private List<AccountInfoEntity> transactions;

    @JsonProperty private String availableAccounts;

    @JsonProperty private String allPsd2;

    public AccessEntity setAccounts(List<AccountInfoEntity> accounts) {
        this.accounts = accounts;
        return this;
    }

    public AccessEntity setBalances(List<AccountInfoEntity> balances) {
        this.balances = balances;
        return this;
    }

    public AccessEntity setTransactions(List<AccountInfoEntity> transactions) {
        this.transactions = transactions;
        return this;
    }

    public AccessEntity setAvailableAccounts(String availableAccounts) {
        this.availableAccounts = availableAccounts;
        return this;
    }

    public AccessEntity setAllPsd2(String allPsd2) {
        this.allPsd2 = allPsd2;
        return this;
    }
}
