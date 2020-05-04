package se.tink.backend.aggregation.eidassigner;

public interface QsealcSigner {

    String getSignatureBase64(byte[] signingData);

    String getJWSToken(byte[] jwsTokenData);

    byte[] getSignature(byte[] signingData);
}
