package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardEntity {
    private String id;
    private DetailsEntity details;
    // `keys: {}` has only been observed empty

    public String getId() {
        return id;
    }

    public DetailsEntity getDetails() {
        return details;
    }
}
