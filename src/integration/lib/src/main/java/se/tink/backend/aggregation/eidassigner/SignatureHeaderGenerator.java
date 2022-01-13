package se.tink.backend.aggregation.eidassigner;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcAlgorithm;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;

public class SignatureHeaderGenerator {

    private final String signatureHeaderFormat;
    private final List<String> headersToSign;
    private final String applicationId;
    private final QsealcSigner qsealcSigner;
    private final QsealcAlgorithm algorithm;

    public SignatureHeaderGenerator(
            String signatureHeaderFormat,
            List<String> headersToSign,
            String applicationId,
            QsealcSigner qsealcSigner,
            QsealcAlgorithm algorithm) {
        this.signatureHeaderFormat = signatureHeaderFormat;
        this.headersToSign = requireNonNull(headersToSign);
        this.applicationId = requireNonNull(applicationId);
        this.qsealcSigner = requireNonNull(qsealcSigner);
        this.algorithm = requireNonNull(algorithm);
    }

    public String generateSignatureHeader(Map<String, Object> headers) {
        String signedHeaders = getSignedHeaders(headers);
        String signedHeadersWithValues = getSignedHeadersWithValues(headers);
        String signature =
                qsealcSigner
                        .sign(this.algorithm, signedHeadersWithValues.getBytes())
                        .getBase64Encoded();
        return String.format(signatureHeaderFormat, applicationId, signedHeaders, signature);
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
