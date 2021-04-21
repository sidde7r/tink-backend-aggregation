package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.Signature;

public class PaymentSignatureEntity {

    private final String httpMethod;
    private final String reqPath;
    private final String date;
    private final String digest;
    private final String xIngRequestId;

    public PaymentSignatureEntity(
            String httpMethod, String reqPath, String date, String digest, String xIngRequestId) {
        this.httpMethod = httpMethod;
        this.reqPath = reqPath;
        this.date = date;
        this.digest = digest;
        this.xIngRequestId = xIngRequestId;
    }

    @Override
    public String toString() {
        return Signature.SIGNING_STRING
                + httpMethod
                + " "
                + reqPath
                + System.lineSeparator()
                + Signature.DATE
                + date
                + System.lineSeparator()
                + Signature.DIGEST
                + digest
                + System.lineSeparator()
                + Signature.X_REQUEST_ID
                + xIngRequestId;
    }
}
