package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConesntAuthorizationDataEntity {
    private String chosenScaApproach;
    @Getter private String scaRedirectURL;
}
