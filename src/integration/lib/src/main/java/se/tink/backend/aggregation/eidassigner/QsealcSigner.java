package se.tink.backend.aggregation.eidassigner;

public interface QsealcSigner {
    /** @deprecated use {@link #getSignatureBase64(QsealcAlg, byte[])} instead. */
    @Deprecated
    String getSignatureBase64(byte[] signingData);

    /** @deprecated use {@link #getJWSToken(QsealcAlg, byte[])} instead. */
    @Deprecated
    String getJWSToken(byte[] jwsTokenData);

    /** @deprecated use {@link #getSignature(QsealcAlg, byte[])} instead. */
    @Deprecated
    byte[] getSignature(byte[] signingData);

    String getSignatureBase64(QsealcAlg algorithm, byte[] dataToSign);

    String getJWSToken(QsealcAlg algorithm, byte[] jwsTokenData);

    byte[] getSignature(QsealcAlg algorithm, byte[] dataToSign);
}
