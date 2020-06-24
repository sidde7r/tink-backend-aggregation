package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IbanAccessEntity implements AccessEntity {

    private List<AccountsEntity> accounts;
    private List<AccountsEntity> balances;
    private List<AccountsEntity> transactions;

    public IbanAccessEntity(
            List<AccountsEntity> accounts,
            List<AccountsEntity> balances,
            List<AccountsEntity> transactions) {
        this.accounts = accounts;
        this.balances = balances;
        this.transactions = transactions;
    }
}
