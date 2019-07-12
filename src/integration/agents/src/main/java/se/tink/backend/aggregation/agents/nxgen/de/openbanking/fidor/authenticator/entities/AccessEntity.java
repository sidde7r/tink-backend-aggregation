package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.entities;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {
    private List<AccountEntity> accounts;
    private List<AccountEntity> balances;
    private List<AccountEntity> transactions;

    public AccessEntity(String iban, String bban) {
        accounts = new ArrayList<>();
        balances = new ArrayList<>();
        transactions = new ArrayList<>();
        accounts.add(new AccountEntity(iban, bban));
        balances.add(new AccountEntity(iban, bban));
        transactions.add(new AccountEntity(iban, bban));
    }
}
