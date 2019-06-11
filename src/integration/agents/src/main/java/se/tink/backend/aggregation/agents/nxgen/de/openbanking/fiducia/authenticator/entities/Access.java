package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Access {

    private List<AccessDetails> balances;
    private List<AccessDetails> transactions;

    public Access(List<AccessDetails> balances, List<AccessDetails> transactions) {
        this.balances = balances;
        this.transactions = transactions;
    }
}
