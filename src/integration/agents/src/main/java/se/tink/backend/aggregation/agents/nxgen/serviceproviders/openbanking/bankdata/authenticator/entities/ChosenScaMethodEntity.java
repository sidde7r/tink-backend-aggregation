package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChosenScaMethodEntity {
    private String authenticationMethodId;
    private String authenticationType;
    private String authenticationVersion;
    private String explanation;
    private String name;
}
