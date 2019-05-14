package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.utils;

import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroConstants;

public class AbnAmroSignatureHeader {
    private final String keyId;
    private final String algorithm;
    private final String headers;
    private final String signature;

    public AbnAmroSignatureHeader(
            final String keyId,
            final String algorithm,
            final String signature,
            final String headers) {
        this.keyId = keyId;
        this.algorithm = algorithm;
        this.signature = signature;
        this.headers = headers;
    }

    private String buildKeyId() {
        return AbnAmroConstants.Signature.KEY_ID + "=\"" + keyId + "\"";
    }

    private String buildAlgorithm() {
        return AbnAmroConstants.Signature.ALGORITHM + "=\"" + algorithm + "\"";
    }

    private String buildHeaders() {
        return AbnAmroConstants.Signature.HEADERS + "=\"" + headers + "\"";
    }

    private String buildSignature() {
        return AbnAmroConstants.Signature.SIGNATURE + "=\"" + signature + "\"";
    }

    @Override
    public String toString() {
        return buildKeyId()
                + ","
                + buildAlgorithm()
                + ","
                + buildHeaders()
                + ","
                + buildSignature();
    }
}
