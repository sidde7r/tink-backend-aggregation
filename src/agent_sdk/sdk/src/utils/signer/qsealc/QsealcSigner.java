package se.tink.agent.sdk.utils.signer.qsealc;

import se.tink.agent.sdk.utils.signer.signature.Signature;

public interface QsealcSigner {
    Signature sign(QsealcAlgorithm algorithm, byte[] dataToSign);
}
