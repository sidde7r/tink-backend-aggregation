package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.signature;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

@RequiredArgsConstructor
public class LclSignatureProvider {

    private final QsealcSigner qsealcSigner;

    public String signRequest(String requestId, String date, String digest) {
        final String signatureString = getSignatureString(requestId, date, digest);
        return signAndEncode(signatureString);
    }

    private String getSignatureString(String requestId, String date, String digest) {
        return String.join(
                "\n", "x-request-id: " + requestId, "date: " + date, "digest: " + digest);
    }

    private String signAndEncode(String signatureString) {
        return qsealcSigner.getSignatureBase64(signatureString.getBytes());
    }
}
