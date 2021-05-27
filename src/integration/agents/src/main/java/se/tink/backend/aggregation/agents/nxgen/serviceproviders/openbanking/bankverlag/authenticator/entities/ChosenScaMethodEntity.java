package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChosenScaMethodEntity {

    private String authenticationType;
    private String authenticationMethodId;
    private String authenticationVersion;
    private String name;
    private String explanation;
}
