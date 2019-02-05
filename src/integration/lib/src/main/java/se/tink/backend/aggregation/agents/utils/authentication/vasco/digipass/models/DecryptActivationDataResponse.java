package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.models;

import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class DecryptActivationDataResponse {
    private final String clientInitialVector;
    private final String encryptedServerNonce;
    private final String derivationCode;

    public DecryptActivationDataResponse(byte[] clientInitialVector, byte[] encryptedServerNonce,
            String derivationCode) {
        this.clientInitialVector = EncodingUtils.encodeHexAsString(clientInitialVector);
        this.encryptedServerNonce = EncodingUtils.encodeHexAsString(encryptedServerNonce);
        this.derivationCode = derivationCode;
    }

    public String getClientInitialVector() {
        return clientInitialVector;
    }

    public String getEncryptedServerNonce() {
        return encryptedServerNonce;
    }

    public String getDerivationCode() {
        return derivationCode;
    }
}
