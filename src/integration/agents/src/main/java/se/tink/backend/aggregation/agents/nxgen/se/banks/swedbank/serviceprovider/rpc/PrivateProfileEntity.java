package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrivateProfileEntity extends ProfileEntity {
    private String id;
    private LinksEntity links;
    private String activeProfileName;

    public String getActiveProfileName() {
        return activeProfileName;
    }

    public String getId() {
        return id;
    }

    public LinksEntity getLinks() {
        return links;
    }
}
