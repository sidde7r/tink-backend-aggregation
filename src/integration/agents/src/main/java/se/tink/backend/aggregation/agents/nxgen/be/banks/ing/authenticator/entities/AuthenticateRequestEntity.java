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
public class AuthenticateRequestEntity {

    private String responseCode;

    private String ingId;

    private String cardId;

    private String encryptedId;

    private AuthenticationContextEntity authenticationContext;

    private String keyId;
}
