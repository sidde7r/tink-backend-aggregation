package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.IbanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SwedbankBalticsAccessEntity {
    private List<IbanEntity> accounts = new ArrayList<>();

    // for Baltics transactionsOver90Days is already included to transactions. So we must
    // not use transactionsOver90Days in Baltics cases but must add it for SE.
    private List<IbanEntity> transactions = new ArrayList<>();
    private List<IbanEntity> balances = new ArrayList<>();

    @JsonIgnore
    public SwedbankBalticsAccessEntity addIbans(List<String> ibansList) {
        for (String iban : ibansList) {
            IbanEntity ibanEntity = new IbanEntity(iban);
            accounts.add(ibanEntity);
            transactions.add(ibanEntity);
            balances.add(ibanEntity);
        }
        return this;
    }
}
