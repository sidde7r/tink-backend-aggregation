package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class LinksEntity {

    private List<AuthorizationItem> authorization;

    public List<AuthorizationItem> getAuthorization() {
        return authorization;
    }
}
