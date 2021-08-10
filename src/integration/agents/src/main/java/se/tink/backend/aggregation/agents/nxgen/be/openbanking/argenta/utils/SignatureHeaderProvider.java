package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils;

import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.*;
import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.HeaderValues.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

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

        String signature = qsealcSigner.getSignatureBase64(signedHeadersWithValues.getBytes());

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
