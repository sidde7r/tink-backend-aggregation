package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class AuthenticationMeanEntity {

    private String authenticationMeanId;
    private String name;
    private String dacLevel;
}
