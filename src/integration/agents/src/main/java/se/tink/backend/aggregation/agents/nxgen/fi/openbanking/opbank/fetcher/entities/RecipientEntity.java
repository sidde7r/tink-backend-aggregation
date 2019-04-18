package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;


import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RecipientEntity {
    private String name;
    private String identifier;
    private String servicer;

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getServicer() {
        return servicer;
    }
}
