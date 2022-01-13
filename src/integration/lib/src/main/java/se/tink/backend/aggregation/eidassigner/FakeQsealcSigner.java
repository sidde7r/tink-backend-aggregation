package se.tink.backend.aggregation.eidassigner;

import se.tink.agent.sdk.utils.signer.qsealc.QsealcAlgorithm;
import se.tink.agent.sdk.utils.signer.signature.Signature;

public final class FakeQsealcSigner
        implements QsealcSigner, se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner {

    private static final byte[] RAW_SIGNATURE = "FAKE_SIGNATURE\n".getBytes();
    private static final String SIGNATURE = "RkFLRV9TSUdOQVRVUkUK"; // b64(FAKE_SIGNATURE)

    @Override
    public String getSignatureBase64(byte[] signingData) {
        return SIGNATURE;
    }

    @Override
    public String getJWSToken(byte[] jwsTokenData) {
        return SIGNATURE;
    }

    @Override
    public byte[] getSignature(byte[] signingData) {
        return SIGNATURE.getBytes();
    }

    @Override
    public Signature sign(QsealcAlgorithm algorithm, byte[] dataToSign) {
        return Signature.create(RAW_SIGNATURE);
    }
}
