package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;

public class PaymentAuthorizationEntity {

    private final String clientId;
    private final String signature;

    public PaymentAuthorizationEntity(String clientId, String signature) {
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
                + IngBaseConstants.Signature.PAYMENT_HEADERS
                + ","
                + IngBaseConstants.Signature.SIGNATURE_NAME
                + "\""
                + signature
                + "\"";
    }
}
