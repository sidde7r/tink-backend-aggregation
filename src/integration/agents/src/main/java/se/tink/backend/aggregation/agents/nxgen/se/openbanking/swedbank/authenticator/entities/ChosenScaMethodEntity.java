package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChosenScaMethodEntity {
    private String authenticationMethodId;
    private String authenticationType;
}
