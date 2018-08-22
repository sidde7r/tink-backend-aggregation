package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class AdvisorProfileEntity {
    private String customerName;

    public String getCustomerName() {
        return customerName;
    }
}
