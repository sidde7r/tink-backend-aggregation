package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

@RequiredArgsConstructor
public class BpceGroupRequestSigner {

    private final QsealcSigner qsealcSigner;

    String getSignature(String signatureString) {
        return qsealcSigner.getSignatureBase64(signatureString.getBytes());
    }
}
