package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountEntity {

    private String currency;

    private int content;

    public String getCurrency() {
        return currency;
    }

    public int getContent() {
        return content;
    }
}
