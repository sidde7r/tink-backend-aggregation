package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProfileEntity {
    private String profileType;

    public ProfileEntity(String profileType) {
        this.profileType = profileType;
    }
}
