package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserCustomizationEntity {
    private boolean favorite;
    private String alias;

    public boolean isFavorite() {
        return favorite;
    }

    public String getAlias() {
        return alias;
    }
}
