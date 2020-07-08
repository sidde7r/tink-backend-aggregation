package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class FiduciaSignatureHeaderGenerator {

    private final String signatureHeaderFormat;
    private final List<String> headersToSign;
    private final QsealcSigner qsealcSigner;

    public FiduciaSignatureHeaderGenerator(
            String signatureHeaderFormat, List<String> headersToSign, QsealcSigner qsealcSigner) {
        this.signatureHeaderFormat = requireNonNull(signatureHeaderFormat);
        this.headersToSign = requireNonNull(headersToSign);
        this.qsealcSigner = requireNonNull(qsealcSigner);
    }

    public String generateSignatureHeader(Map<String, Object> headers) {
        String signedHeaders = getSignedHeaders(headers);
        String signedHeadersWithValues = getSignedHeadersWithValues(headers);
        String signature = qsealcSigner.getSignatureBase64(signedHeadersWithValues.getBytes());

        return String.format(signatureHeaderFormat, signedHeaders, signature);
    }

    private String getSignedHeaders(Map<String, Object> headers) {
        return headersToSign.stream()
                .filter(headers::containsKey)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
    }

    private String getSignedHeadersWithValues(Map<String, Object> headers) {
        return headersToSign.stream()
                .filter(headers::containsKey)
                .map(header -> String.format("%s: %s", header.toLowerCase(), headers.get(header)))
                .collect(Collectors.joining("\n"));
    }
}
