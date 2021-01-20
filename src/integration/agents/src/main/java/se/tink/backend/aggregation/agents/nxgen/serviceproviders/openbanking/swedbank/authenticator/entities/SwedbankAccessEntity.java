package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.IbanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SwedbankAccessEntity {
    private List<IbanEntity> accounts = new ArrayList<>();
    private List<IbanEntity> transactions = new ArrayList<>();
    private List<IbanEntity> balances = new ArrayList<>();
    private List<IbanEntity> transactionsOver90Days = new ArrayList<>();

    @JsonIgnore
    public SwedbankAccessEntity addIbans(List<String> ibans) {
        for (String iban : ibans) {
            IbanEntity ibanEntity = new IbanEntity(iban);
            accounts.add(ibanEntity);
            transactions.add(ibanEntity);
            balances.add(ibanEntity);
            transactionsOver90Days.add(ibanEntity);
        }
        return this;
    }
}
