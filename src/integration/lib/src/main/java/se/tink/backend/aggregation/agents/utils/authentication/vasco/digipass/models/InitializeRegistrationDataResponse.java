package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.models;

import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class InitializeRegistrationDataResponse {
    private final String clientInitialVector;
    private final String encryptedClientPublicKeyAndNonce;

    public InitializeRegistrationDataResponse(byte[] clientInitialVector, byte[] encryptedClientPublicKeyAndNonce) {
        this.clientInitialVector = EncodingUtils.encodeHexAsString(clientInitialVector);
        this.encryptedClientPublicKeyAndNonce = EncodingUtils.encodeHexAsString(encryptedClientPublicKeyAndNonce);
    }

    public String getClientInitialVector() {
        return clientInitialVector;
    }

    public String getEncryptedClientPublicKeyAndNonce() {
        return encryptedClientPublicKeyAndNonce;
    }
}
