package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class SignatureHeaderGenerator {

    private final String applicationId;
    private final QsealcSigner qsealcSigner;

    public SignatureHeaderGenerator(
            @NonNull String applicationId, @NonNull final QsealcSigner qsealcSigner) {
        this.applicationId = applicationId;
        this.qsealcSigner = qsealcSigner;
    }

    public String generateSignatureHeader(Map<String, Object> headers) {
        String signedHeaders = getSignedHeaders(headers);
        String signedHeadersWithValues = getSignedHeadersWithValues(headers);
        String signature = qsealcSigner.getSignatureBase64(signedHeadersWithValues.getBytes());

        return String.format(
                ChebancaConstants.HeaderValues.SIGNATURE_HEADER,
                applicationId,
                signedHeaders,
                signature);
    }

    private String getSignedHeadersWithValues(Map<String, Object> headers) {
        return Arrays.stream(ChebancaConstants.HeadersToSign.values())
                .map(ChebancaConstants.HeadersToSign::getHeader)
                .filter(headers::containsKey)
                .map(header -> String.format("%s: %s", header.toLowerCase(), headers.get(header)))
                .collect(Collectors.joining("\n"));
    }

    private String getSignedHeaders(Map<String, Object> headers) {
        return Arrays.stream(ChebancaConstants.HeadersToSign.values())
                .map(ChebancaConstants.HeadersToSign::getHeader)
                .filter(headers::containsKey)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
    }
}
