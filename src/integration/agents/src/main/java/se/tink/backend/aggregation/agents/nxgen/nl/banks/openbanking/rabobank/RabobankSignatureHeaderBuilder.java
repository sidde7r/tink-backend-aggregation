package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import java.util.Base64;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.utils.RabobankSignatureHeader;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.libraries.cryptography.Certificate;

@RequiredArgsConstructor
class RabobankSignatureHeaderBuilder {

    @Getter private final String qsealcPem;
    private final QsealcSigner qsealcSigner;

    public String buildSignatureHeader(
            final String digest, final String requestId, final String date) {
        final String signingString = createSignatureString(date, digest, requestId);
        final byte[] signatureBytes = qsealcSigner.getSignature(signingString.getBytes());

        final String b64Signature = Base64.getEncoder().encodeToString(signatureBytes);
        final String clientCertSerial = extractQsealcSerial(qsealcPem);

        return new RabobankSignatureHeader(
                        clientCertSerial,
                        Signature.RSA_SHA_256,
                        b64Signature,
                        Signature.HEADERS_VALUE)
                .toString();
    }

    private String createSignatureString(
            final String date, final String digest, final String requestId) {
        String result = Signature.SIGNING_STRING_DATE + date + "\n";
        result +=
                Signature.SIGNING_STRING_DIGEST + Signature.SIGNING_STRING_SHA_512 + digest + "\n";
        result += Signature.SIGNING_STRING_REQUEST_ID + requestId;
        return result;
    }

    private String extractQsealcSerial(final String qsealc) {
        return Certificate.getX509SerialNumber(qsealc);
    }
}
