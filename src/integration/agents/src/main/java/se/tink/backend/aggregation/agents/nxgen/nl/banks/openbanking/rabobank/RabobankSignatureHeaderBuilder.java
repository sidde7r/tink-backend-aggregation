package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import java.util.Base64;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.utils.RabobankUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.libraries.cryptography.Certificate;

@RequiredArgsConstructor
class RabobankSignatureHeaderBuilder {

    @Getter private final String qsealcPem;
    private final QsealcSigner qsealcSigner;

    public String buildSignatureHeader(
            final String digest, final String requestId, final String date) {
        final String signingString = RabobankUtils.createSignatureString(date, digest, requestId);
        final byte[] signatureBytes = qsealcSigner.getSignature(signingString.getBytes());

        final String b64Signature = Base64.getEncoder().encodeToString(signatureBytes);
        final String clientCertSerial = extractQsealcSerial(qsealcPem);

        return RabobankUtils.createSignatureHeader(
                clientCertSerial, Signature.RSA_SHA_256, b64Signature, Signature.HEADERS_VALUE);
    }

    private String extractQsealcSerial(final String qsealc) {
        return Certificate.getX509SerialNumber(qsealc);
    }
}
