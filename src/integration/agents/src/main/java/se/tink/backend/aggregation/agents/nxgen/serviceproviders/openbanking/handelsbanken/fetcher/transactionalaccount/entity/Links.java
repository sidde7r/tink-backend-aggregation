package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Links {

    @JsonProperty("transactions")
    private Transactions transactions;

    public Transactions getTransactions() {
        return transactions;
    }
}
