package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

@RequiredArgsConstructor
@Slf4j
public class FiduciaSignatureHeaderGenerator {

    private static final String SIGNATURE_HEADER_FORMAT =
            "algorithm=\"SHA256withRSA\",headers=\"%s\",signature=\"%s\"";

    private static final List<String> HEADERS_TO_SIGN =
            Arrays.asList(
                    FiduciaConstants.HeaderKeys.DATE,
                    FiduciaConstants.HeaderKeys.DIGEST,
                    FiduciaConstants.HeaderKeys.X_REQUEST_ID,
                    FiduciaConstants.HeaderKeys.PSU_ID,
                    FiduciaConstants.HeaderKeys.PSU_CORPORATE_ID);

    private final QsealcSigner qsealcSigner;

    public String generateSignatureHeader(Map<String, Object> headers) {
        String signedHeaders = getSignedHeaders(headers);
        String signedHeadersWithValues = getSignedHeadersWithValues(headers);
        String signature =
                qsealcSigner.getSignatureBase64(
                        signedHeadersWithValues.getBytes(StandardCharsets.UTF_8));

        log.info(String.format("[FIDUCIA] SIGNATURE: %s", signature));

        return String.format(SIGNATURE_HEADER_FORMAT, signedHeaders, signature);
    }

    private String getSignedHeaders(Map<String, Object> headers) {
        return HEADERS_TO_SIGN.stream()
                .filter(headers::containsKey)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
    }

    private String getSignedHeadersWithValues(Map<String, Object> headers) {
        return HEADERS_TO_SIGN.stream()
                .filter(headers::containsKey)
                .map(header -> String.format("%s: %s", header.toLowerCase(), headers.get(header)))
                .collect(Collectors.joining("\n"));
    }
}
