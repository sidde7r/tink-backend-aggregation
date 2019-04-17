package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {
    @JsonProperty private List<AccountInfoEntity> accounts;

    @JsonProperty private List<AccountInfoEntity> balances;

    @JsonProperty private List<AccountInfoEntity> transactions;

    public AccessEntity(
            List<AccountInfoEntity> accounts,
            List<AccountInfoEntity> balances,
            List<AccountInfoEntity> transactions) {
        this.accounts = accounts;
        this.balances = balances;
        this.transactions = transactions;
    }
}
