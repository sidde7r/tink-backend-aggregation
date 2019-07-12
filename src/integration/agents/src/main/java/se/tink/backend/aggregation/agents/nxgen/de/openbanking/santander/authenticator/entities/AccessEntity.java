package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.entities;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    private final List<AccountsEntity> accounts = new ArrayList<>();
    private final List<AccountsEntity> transactions = new ArrayList<>();
    private final List<AccountsEntity> balances = new ArrayList<>();

    public void addAccessEntity(String iban, String currency) {
        accounts.add(new AccountsEntity(iban, currency));
        transactions.add(new AccountsEntity(iban, currency));
        balances.add(new AccountsEntity(iban, currency));
    }
}
