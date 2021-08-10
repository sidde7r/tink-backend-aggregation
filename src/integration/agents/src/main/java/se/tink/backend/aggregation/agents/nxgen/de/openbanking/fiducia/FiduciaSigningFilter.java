package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.MultivaluedMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
@Slf4j
public class FiduciaSigningFilter extends Filter {

    private static final String SHA_256_PREFIX = "SHA-256=";
    private static final String SIGNATURE_HEADER_FORMAT =
            "keyId=\"SN=%s,CA=%s\",algorithm=\"SHA256withRSA\",headers=\"%s\",signature=\"%s\"";

    private static final List<String> HEADERS_TO_SIGN =
            Arrays.asList(
                    FiduciaConstants.HeaderKeys.DIGEST,
                    FiduciaConstants.HeaderKeys.X_REQUEST_ID,
                    FiduciaConstants.HeaderKeys.PSU_ID);

    private final QsealcSigner qsealcSigner;
    private final String qsealcDerBase64;
    private final String qsealcSerialNumberInHex;
    private final String qsealcIssuerDN;

    @SneakyThrows
    public FiduciaSigningFilter(
            QsealcSigner qsealcSigner,
            AgentConfiguration<FiduciaConfiguration> agentConfiguration) {
        this.qsealcSigner = qsealcSigner;

        String qsealc = agentConfiguration.getQsealc();

        this.qsealcDerBase64 =
                CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(qsealc);
        this.qsealcSerialNumberInHex = CertificateUtils.getSerialNumber(qsealc, 16);
        this.qsealcIssuerDN = CertificateUtils.getCertificateIssuerDN(qsealc);
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        MultivaluedMap<String, Object> requestHeaders = httpRequest.getHeaders();

        String body =
                httpRequest.getBody() instanceof String
                        ? httpRequest.getBody().toString()
                        : getBody(httpRequest);
        String digest = createDigest(body);

        requestHeaders.add(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, qsealcDerBase64);
        requestHeaders.add(HeaderKeys.DIGEST, digest);
        requestHeaders.add(HeaderKeys.SIGNATURE, generateSignatureHeader(requestHeaders));

        return nextFilter(httpRequest);
    }

    private String getBody(HttpRequest httpRequest) {
        return Optional.ofNullable(httpRequest.getBody())
                .map(SerializationUtils::serializeToString)
                .orElse("");
    }

    private String createDigest(String body) {
        return SHA_256_PREFIX
                + Base64.getEncoder()
                        .encodeToString(Hash.sha256(body.getBytes(StandardCharsets.UTF_8)));
    }

    private String generateSignatureHeader(MultivaluedMap<String, Object> headers) {
        String signedHeaders = getSignedHeaders(headers);
        String signedHeadersWithValues = getSignedHeadersWithValues(headers);
        String signature =
                qsealcSigner.getSignatureBase64(
                        signedHeadersWithValues.getBytes(StandardCharsets.UTF_8));

        return String.format(
                SIGNATURE_HEADER_FORMAT,
                qsealcSerialNumberInHex,
                qsealcIssuerDN,
                signedHeaders,
                signature);
    }

    private String getSignedHeaders(MultivaluedMap<String, Object> headers) {
        return HEADERS_TO_SIGN.stream()
                .filter(headers::containsKey)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
    }

    private String getSignedHeadersWithValues(MultivaluedMap<String, Object> headers) {
        return HEADERS_TO_SIGN.stream()
                .filter(headers::containsKey)
                .map(header -> header.toLowerCase() + ": " + headers.getFirst(header))
                .collect(Collectors.joining("\n"));
    }
}
