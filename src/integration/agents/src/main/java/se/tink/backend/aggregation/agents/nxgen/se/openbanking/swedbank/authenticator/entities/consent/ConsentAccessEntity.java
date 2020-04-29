package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entities.consent;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAccessEntity {
    private List<ConsentAccessIdentifier> accounts;
    private List<ConsentAccessIdentifier> balances;
    private List<ConsentAccessIdentifier> transactions;

    public ConsentAccessEntity(
            List<ConsentAccessIdentifier> accounts,
            List<ConsentAccessIdentifier> balances,
            List<ConsentAccessIdentifier> transactions) {
        this.accounts = accounts;
        this.balances = balances;
        this.transactions = transactions;
    }
}
