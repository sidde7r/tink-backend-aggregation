package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;

public class AuthorizationEntity {

    private final String clientId;
    private final String signature;

    public AuthorizationEntity(String clientId, String signature) {
        this.clientId = clientId;
        this.signature = signature;
    }

    public String toString() {
        return IngConstants.Signature.KEY_ID_NAME
                + "\""
                + clientId
                + "\","
                + IngConstants.Signature.ALGORITHM
                + ","
                + IngConstants.Signature.HEADERS
                + ","
                + IngConstants.Signature.SIGNATURE_NAME
                + "\""
                + signature
                + "\"";
    }
}
