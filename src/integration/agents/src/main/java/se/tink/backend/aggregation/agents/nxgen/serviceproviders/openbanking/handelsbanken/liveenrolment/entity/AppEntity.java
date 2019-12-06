package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AppEntity {

    private String name;

    private String description;

    private String oauthRedirectURI;

    public AppEntity(String name, String description, String oauthRedirectURI) {
        this.name = name;
        this.description = description;
        this.oauthRedirectURI = oauthRedirectURI;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOauthRedirectURI() {
        return oauthRedirectURI;
    }
}
