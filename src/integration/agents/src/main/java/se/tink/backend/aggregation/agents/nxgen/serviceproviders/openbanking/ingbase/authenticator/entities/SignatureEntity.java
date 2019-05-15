package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;

public class SignatureEntity {

    private final String httpMethod;
    private final String reqPath;
    private final String date;
    private final String digest;
    private final String xIngRequestId;

    public SignatureEntity(
            String httpMethod, String reqPath, String date, String digest, String xIngRequestId) {
        this.httpMethod = httpMethod;
        this.reqPath = reqPath;
        this.date = date;
        this.digest = digest;
        this.xIngRequestId = xIngRequestId;
    }

    @Override
    public String toString() {
        return IngBaseConstants.Signature.SIGNING_STRING
                + httpMethod
                + " "
                + reqPath
                + System.lineSeparator()
                + IngBaseConstants.Signature.DATE
                + date
                + System.lineSeparator()
                + IngBaseConstants.Signature.DIGEST
                + digest
                + System.lineSeparator()
                + IngBaseConstants.Signature.X_ING_REQUEST_ID
                + xIngRequestId;
    }
}
