package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private TransactionsEntity transactionsEntity;

    public TransactionsEntity getTransactionsEntity() {
        return transactionsEntity;
    }
}
