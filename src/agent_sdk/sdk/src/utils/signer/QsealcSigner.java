package se.tink.agent.sdk.utils.signer;

public interface QsealcSigner {
    Signature sign(QsealcAlgorithm algorithm, byte[] dataToSign);
}
