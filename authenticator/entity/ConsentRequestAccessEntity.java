package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequestAccessEntity {

    private List<IbanEntity> accounts;
    private List<IbanEntity> balances;
    private List<IbanEntity> transactions;

    public ConsentRequestAccessEntity(List<IbanEntity> ibans) {
        this.accounts = ibans;
        this.balances = ibans;
        this.transactions = ibans;
    }
}
