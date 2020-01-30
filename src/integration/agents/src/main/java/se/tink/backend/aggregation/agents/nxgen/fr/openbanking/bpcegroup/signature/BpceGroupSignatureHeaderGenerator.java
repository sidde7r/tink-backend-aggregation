package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature;

import java.net.URI;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupHttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class BpceGroupSignatureHeaderGenerator {

    private static final String ALGORITHM = "algorithm=\"rsa-sha256\"";

    private final BpceGroupConfiguration bpceGroupConfiguration;
    private final BpceGroupRequestSigner bpceGroupRequestSigner;

    public String buildSignatureHeader(HttpMethod httpMethod, URL url, String requestId) {
        final String targetRequest = buildTargetRequest(httpMethod, getPathForSignature(url));

        return String.format(
                "%s,%s,%s,%s",
                getKeyId(), ALGORITHM, getHeaders(), getSignature(targetRequest, requestId));
    }

    private String getKeyId() {
        return String.format("keyId=\"%s\"", bpceGroupConfiguration.getKeyId());
    }

    private String getHeaders() {
        return String.format(
                "headers=\"(request-target) %s\"",
                BpceGroupHttpHeaders.X_REQUEST_ID.getName().toLowerCase());
    }

    private String getSignature(String targetRequest, String requestId) {
        final String signatureString =
                String.format(
                        "(request-target): %s\n%s: %s",
                        targetRequest,
                        BpceGroupHttpHeaders.X_REQUEST_ID.getName().toLowerCase(),
                        requestId);

        return String.format(
                "signature=\"%s\"", bpceGroupRequestSigner.getSignature(signatureString));
    }

    private static String buildTargetRequest(HttpMethod httpMethod, String path) {
        return String.format("%s %s", httpMethod.name().toLowerCase(), path);
    }

    private static String getPathForSignature(URL url) {
        final URI uri = url.toUri();
        return Objects.nonNull(uri.getQuery())
                ? String.format("%s?%s", uri.getPath(), uri.getQuery())
                : uri.getPath();
    }
}
