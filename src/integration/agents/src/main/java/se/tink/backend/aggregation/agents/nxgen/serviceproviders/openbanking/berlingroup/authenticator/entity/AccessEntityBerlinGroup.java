package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntityBerlinGroup extends AccessEntity {

    @Override
    public void addIban(String iban) {
        this.accounts.add(new IbanEntity(iban));
        this.transactions.add(new IbanEntity(iban));
        this.balances.add(new IbanEntity(iban));
    }

    @Override
    public void addIbans(List<String> ibans) {
        ibans.forEach(this::addIban);
    }
}
