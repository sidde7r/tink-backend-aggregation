package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

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
public class KeyAgreementRequest {

    private String clientNonce;

    private String clientId;

    private String clientPublicKey;

    private String serverSigningKeyId;
}
