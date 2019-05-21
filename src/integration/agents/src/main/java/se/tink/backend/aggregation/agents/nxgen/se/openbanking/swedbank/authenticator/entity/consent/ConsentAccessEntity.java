package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entity.consent;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAccessEntity {
    private List<ConsentAccessIdentifier> accounts;
    private List<ConsentAccessIdentifier> balances;
    private List<ConsentAccessIdentifier> transactions;
    private String availableAccounts;
    private String allPsd2;

    public ConsentAccessEntity(
            List<ConsentAccessIdentifier> accounts,
            List<ConsentAccessIdentifier> balances,
            List<ConsentAccessIdentifier> transactions,
            String availableAccounts,
            String allPsd2) {
        this.accounts = accounts;
        this.balances = balances;
        this.transactions = transactions;
        this.availableAccounts = availableAccounts;
        this.allPsd2 = allPsd2;
    }
}
