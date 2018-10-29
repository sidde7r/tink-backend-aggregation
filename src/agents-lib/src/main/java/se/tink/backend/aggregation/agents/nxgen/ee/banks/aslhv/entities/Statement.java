package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Statement {

    @JsonProperty("transactions")
    private List<TransactionItem> transactions;

    public List<TransactionItem> getTransactions() {
        return transactions;
    }
}
