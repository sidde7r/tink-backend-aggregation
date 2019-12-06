package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaMethodEntity {
    private String authenticationMethodId;
    private String authenticationType;
    private String authenticationVersion;
    private String explanation;
    private String name;
}
