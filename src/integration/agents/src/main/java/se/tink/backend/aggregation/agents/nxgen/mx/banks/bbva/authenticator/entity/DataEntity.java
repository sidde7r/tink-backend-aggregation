package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DataEntity {
    private String customerId;

    public String getCustomerId() {
        return customerId;
    }
}
