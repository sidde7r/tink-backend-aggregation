package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.math.BigDecimal;

@JsonObject
public class TransactionAmountEntity {

    private String currency;

    private BigDecimal content;

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getContent() {
        return content;
    }
}
