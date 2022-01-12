package se.tink.backend.aggregation.eidassigner;

public interface QsealcSigner {
    /**
     * @deprecated use {@link
     *     se.tink.agent.sdk.utils.signer.QsealcSigner#sign(se.tink.agent.sdk.utils.signer.QsealcAlgorithm,
     *     byte[])} instead.
     */
    @Deprecated
    String getSignatureBase64(byte[] signingData);

    /**
     * @deprecated use {@link
     *     se.tink.agent.sdk.utils.signer.QsealcSigner#sign(se.tink.agent.sdk.utils.signer.QsealcAlgorithm,
     *     byte[])} instead.
     */
    @Deprecated
    String getJWSToken(byte[] jwsTokenData);

    /**
     * @deprecated use {@link
     *     se.tink.agent.sdk.utils.signer.QsealcSigner#sign(se.tink.agent.sdk.utils.signer.QsealcAlgorithm,
     *     byte[])} instead.
     */
    @Deprecated
    byte[] getSignature(byte[] signingData);
}
