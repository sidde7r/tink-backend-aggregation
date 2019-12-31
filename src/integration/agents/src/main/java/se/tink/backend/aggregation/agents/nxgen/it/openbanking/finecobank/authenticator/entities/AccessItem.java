package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessItem {

    @JsonProperty("balances")
    private List<AccountConsent> balancesConsents;

    @JsonProperty("transactions")
    private List<AccountConsent> transactionsConsents;

    // Used in serialization
    private AccessItem() {}

    public AccessItem(
            List<AccountConsent> balancesConsents, List<AccountConsent> transactionsConsents) {
        this.balancesConsents = balancesConsents;
        this.transactionsConsents = transactionsConsents;
    }

    public List<AccountConsent> getBalancesConsents() {
        return balancesConsents;
    }

    public List<AccountConsent> getTransactionsConsents() {
        return transactionsConsents;
    }
}
