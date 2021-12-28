package se.tink.backend.aggregation.eidassigner;

public interface QsealcSigner {
    String getSignatureBase64(byte[] signingData);

    String getJWSToken(byte[] jwsTokenData);

    byte[] getSignature(byte[] signingData);

    String getSignatureBase64(QsealcAlg algorithm, byte[] dataToSign);

    String getJWSToken(QsealcAlg algorithm, byte[] jwsTokenData);

    byte[] getSignature(QsealcAlg algorithm, byte[] dataToSign);
}
