package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {
    private List<IbanEntity> accounts = new ArrayList<>();
    private List<IbanEntity> transactions = new ArrayList<>();
    private List<IbanEntity> balances = new ArrayList<>();

    public AccessEntity(String iban) {
        this.accounts.add(new IbanEntity(iban));
        this.transactions.add(new IbanEntity(iban));
        this.balances.add(new IbanEntity(iban));
    }
}
