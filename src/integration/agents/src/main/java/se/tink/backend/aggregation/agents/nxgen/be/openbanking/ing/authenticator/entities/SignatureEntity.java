package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;

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
        return IngConstants.Signature.SIGNING_STRING
                + httpMethod
                + " "
                + reqPath
                + System.lineSeparator()
                + IngConstants.Signature.DATE
                + date
                + System.lineSeparator()
                + IngConstants.Signature.DIGEST
                + digest
                + System.lineSeparator()
                + IngConstants.Signature.X_ING_REQUEST_ID
                + xIngRequestId;
    }
}
