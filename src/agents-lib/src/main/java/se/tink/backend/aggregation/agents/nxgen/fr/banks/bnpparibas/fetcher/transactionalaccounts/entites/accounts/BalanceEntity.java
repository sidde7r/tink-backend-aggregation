package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {
    @JsonDouble
    @JsonProperty("solde")
    private double balance;

    public double getBalance() {
        return balance;
    }

    @JsonIgnore
    public Amount getTinkAmount() {
        return Amount.inEUR(balance);
    }
}
