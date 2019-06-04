package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    private final List<IbanEntity> accounts = new ArrayList<>();
    private final List<IbanEntity> transactions = new ArrayList<>();
    private final List<IbanEntity> balances = new ArrayList<>();

    public void addIban(final String iban) {
        accounts.add(new IbanEntity(iban));
        transactions.add(new IbanEntity(iban));
        balances.add(new IbanEntity(iban));
    }

    public void addIbans(final List<String> ibans) {
        ibans.forEach(
                iban -> {
                    accounts.add(new IbanEntity(iban));
                    transactions.add(new IbanEntity(iban));
                    balances.add(new IbanEntity(iban));
                });
    }
}
