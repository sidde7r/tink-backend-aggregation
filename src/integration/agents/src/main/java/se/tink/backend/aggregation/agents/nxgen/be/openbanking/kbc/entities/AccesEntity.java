package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.IbanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccesEntity extends AccessEntity {

    protected List<IbanEntity> transactions = new ArrayList<>();
    protected List<IbanEntity> balances = new ArrayList<>();
    public String allPsd2;

    public AccesEntity() {}

    public AccesEntity(String allPsd2) {
        this.allPsd2 = allPsd2;
    }

    @Override
    public void addIban(String iban) {
        this.transactions.add(new IbanEntity(iban));
        this.balances.add(new IbanEntity(iban));
    }

    @Override
    public void addIbans(List<String> ibans) {
        ibans.forEach(this::addIban);
    }
}
