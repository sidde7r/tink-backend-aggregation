package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcQuantity {
    private String localizedQuantity;
    private double quantity;

    public String getLocalizedQuantity() {
        return localizedQuantity;
    }

    public double getQuantity() {
        return quantity;
    }
}
