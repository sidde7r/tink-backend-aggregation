package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AccessTokenEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class KeyAgreementResponse {

    private AccessTokenEntity accessTokens;

    private String serverPublicKey;

    private String serverNonce;

    private String signature;
}
