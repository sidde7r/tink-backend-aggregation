package se.tink.backend.aggregation.eidassigner;

public final class FakeQsealcSigner implements QsealcSigner {

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
    public String getSignatureBase64(QsealcAlg algorithm, byte[] dataToSign) {
        return SIGNATURE;
    }

    @Override
    public String getJWSToken(QsealcAlg algorithm, byte[] jwsTokenData) {
        return SIGNATURE;
    }

    @Override
    public byte[] getSignature(QsealcAlg algorithm, byte[] dataToSign) {
        return SIGNATURE.getBytes();
    }
}
