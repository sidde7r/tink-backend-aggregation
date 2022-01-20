package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils;

import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.HeaderValues.SIGNATURE_HEADER;
import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.HeadersToSign;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcAlgorithm;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants;

@RequiredArgsConstructor
public final class SignatureHeaderProvider {

    private final QsealcSigner qsealcSigner;
    private final CertificateValues certificateValues;

    public String generateSignatureHeader(Map<String, Object> headers) {

        String signedHeaders =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .map(String::toLowerCase)
                        .collect(Collectors.joining(" "));

        String signedHeadersWithValues =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .map(header -> String.format("%s: %s", header, headers.get(header)))
                        .collect(Collectors.joining("\n"));

        String signature =
                qsealcSigner
                        .sign(QsealcAlgorithm.RSA_SHA256, signedHeadersWithValues.getBytes())
                        .getBase64Encoded();

        return String.format(
                SIGNATURE_HEADER,
                String.format(
                        ArgentaConstants.SignatureFormat.KEY_ID_FORMAT,
                        certificateValues.getSerialNumber(),
                        certificateValues.getCertificateAuthority()),
                signedHeaders,
                signature);
    }
}
