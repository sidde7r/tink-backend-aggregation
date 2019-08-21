package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PayerEntity {
    private String name;

    public String getName() {
        return name;
    }
}
