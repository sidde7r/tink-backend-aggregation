package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("transactions")
    private TransactionsEntity transactionsEntity;

    public TransactionsEntity getTransactionsEntity() {
        return transactionsEntity;
    }
}
