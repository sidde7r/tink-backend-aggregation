package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ShopEntity {
    private String id;
    private String name;
    private String address;
    private String country;
    private BasicEntity industry;
    private BasicEntity activitySector;
    private BasicEntity activitySubSector;

    public String getName() {
        return name;
    }
}
