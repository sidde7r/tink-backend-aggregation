package se.tink.backend.aggregation.eidassigner;

import se.tink.agent.sdk.utils.signer.qsealc.QsealcAlgorithm;

public interface QsealcSigner {
    /**
     * @deprecated use {@link
     *     se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner#sign(QsealcAlgorithm, byte[])}
     *     instead.
     */
    @Deprecated
    String getSignatureBase64(byte[] signingData);

    /**
     * @deprecated use {@link
     *     se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner#sign(QsealcAlgorithm, byte[])}
     *     instead.
     */
    @Deprecated
    String getJWSToken(byte[] jwsTokenData);

    /**
     * @deprecated use {@link
     *     se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner#sign(QsealcAlgorithm, byte[])}
     *     instead.
     */
    @Deprecated
    byte[] getSignature(byte[] signingData);
}
