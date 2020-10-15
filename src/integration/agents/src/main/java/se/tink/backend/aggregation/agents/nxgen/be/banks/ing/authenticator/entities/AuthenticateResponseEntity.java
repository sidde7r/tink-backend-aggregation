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
public class AuthenticateResponseEntity {

    private String status;

    private AuthenticateTokenResultEntity accessToken;

    private String keyId;

    private String encryptedId;
}
