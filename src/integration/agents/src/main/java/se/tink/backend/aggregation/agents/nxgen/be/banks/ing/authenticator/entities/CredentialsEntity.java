package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@JsonObject
public class CredentialsEntity {

    private String means;

    private String verifier;

    private String salt;
}
