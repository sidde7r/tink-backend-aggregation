package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAmountEntity {

    private String currency;

    private double content;

    public String getCurrency() {
        return currency;
    }

    public double getContent() {
        return content;
    }
}
