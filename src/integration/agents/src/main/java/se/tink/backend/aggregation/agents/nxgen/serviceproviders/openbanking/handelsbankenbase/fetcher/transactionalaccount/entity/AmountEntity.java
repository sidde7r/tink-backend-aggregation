package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountEntity {

    private String currency;
    private Double content;

    public String getCurrency() {
        return currency;
    }

    public Double getContent() {
        return content;
    }
}
