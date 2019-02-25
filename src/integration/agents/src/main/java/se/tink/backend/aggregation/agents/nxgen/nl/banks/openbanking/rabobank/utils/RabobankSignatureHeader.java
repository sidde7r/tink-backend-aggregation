package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.utils;

import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants;

public class RabobankSignatureHeader {
    private final String keyId;
    private final String algorithm;
    private final String headers;
    private final String signature;

    public RabobankSignatureHeader(
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
        return RabobankConstants.Signature.KEY_ID + "=\"" + keyId + "\"";
    }

    private String buildAlgorithm() {
        return RabobankConstants.Signature.ALGORITHM + "=\"" + algorithm + "\"";
    }

    private String buildHeaders() {
        return RabobankConstants.Signature.HEADERS + "=\"" + headers + "\"";
    }

    private String buildSignature() {
        return RabobankConstants.Signature.SIGNATURE + "=\"" + signature + "\"";
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
