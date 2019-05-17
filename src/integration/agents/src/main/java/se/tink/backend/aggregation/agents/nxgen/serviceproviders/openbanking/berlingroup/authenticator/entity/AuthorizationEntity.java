package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;

public class AuthorizationEntity {

    private final String clientId;
    private final String signature;

    public AuthorizationEntity(String clientId, String signature) {
        this.clientId = clientId;
        this.signature = signature;
    }

    public String toString() {
        return BerlinGroupConstants.Signature.KEY_ID_NAME
                + "\""
                + clientId
                + "\","
                + "algorithm=\"SHA256withRSA\""
                + ","
                + "headers=\"digest x-request-id\""
                + ","
                + BerlinGroupConstants.Signature.SIGNATURE_NAME
                + "\""
                + signature
                + "\"";
    }
}
