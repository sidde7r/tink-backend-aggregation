package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class AuthenticationContextEntity {

    private int requiredLevelOfAssurance;

    private String clientId;

    private String identifyeeType;

    private String[] scopes;
}
