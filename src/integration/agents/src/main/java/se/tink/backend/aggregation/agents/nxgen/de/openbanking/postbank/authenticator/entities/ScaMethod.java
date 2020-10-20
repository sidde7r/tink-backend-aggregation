package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ScaMethod {

    private String name;
    private String authenticationType;
    private String authenticationMethodId;
}
