package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private List<AuthorizationItem> authorization;

    public List<AuthorizationItem> getAuthorization() {
        return authorization;
    }
}
