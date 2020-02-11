package se.tink.backend.aggregation.nxgen.http.header;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class SignatureHeaderGenerator {

    private final String signatureHeader;
    private final List<String> headersToSign;
    private final String applicationId;
    private final QsealcSigner qsealcSigner;

    public SignatureHeaderGenerator(
            String signatureHeader,
            List<String> headersToSign,
            String applicationId,
            QsealcSigner qsealcSigner) {
        this.signatureHeader = signatureHeader;
        this.headersToSign = requireNonNull(headersToSign);
        this.applicationId = requireNonNull(applicationId);
        this.qsealcSigner = requireNonNull(qsealcSigner);
    }

    public String generateSignatureHeader(Map<String, Object> headers) {
        String signedHeaders = getSignedHeaders(headers);
        String signedHeadersWithValues = getSignedHeadersWithValues(headers);
        String signature = qsealcSigner.getSignatureBase64(signedHeadersWithValues.getBytes());

        return String.format(signatureHeader, applicationId, signedHeaders, signature);
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
