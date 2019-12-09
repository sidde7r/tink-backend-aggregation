package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;

public class AuthorizationEntity {

    private final String clientId;
    private final String signature;

    public AuthorizationEntity(String clientId, String signature) {
        this.clientId = clientId;
        this.signature = signature;
    }

    public String toString() {
        return IngBaseConstants.Signature.KEY_ID_NAME
                + "\""
                + clientId
                + "\","
                + IngBaseConstants.Signature.ALGORITHM
                + ","
                + IngBaseConstants.Signature.HEADERS
                + ","
                + IngBaseConstants.Signature.SIGNATURE_NAME
                + "\""
                + signature
                + "\"";
    }
}
