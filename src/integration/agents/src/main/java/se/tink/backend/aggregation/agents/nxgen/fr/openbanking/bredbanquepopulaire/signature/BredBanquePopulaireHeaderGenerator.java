package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.signature;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcAlgorithm;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireConstants;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public class BredBanquePopulaireHeaderGenerator {

    private final QsealcSigner qsealcSigner;
    private final QsealcAlgorithm qsealcAlgorithm;
    private final SessionStorage sessionStorage;
    private final String keyId;

    public String getDigestHeaderValue(Object requestBody) {
        final String serializedBody =
                Objects.isNull(requestBody)
                        ? ""
                        : SerializationUtils.serializeToString(requestBody);
        final String digest =
                "SHA-256="
                        + Base64.getEncoder()
                                .encodeToString(
                                        Hash.sha256(
                                                Objects.requireNonNull(serializedBody)
                                                        .getBytes(StandardCharsets.UTF_8)));
        sessionStorage.put(BredBanquePopulaireConstants.StorageKeys.DIGEST, digest);
        return digest;
    }

    public String buildSignatureHeader(HttpMethod httpMethod, URL url, String requestId) {
        final String targetRequest = buildTargetRequest(httpMethod, getPathForSignature(url));
        final boolean isPutMethod = HttpMethod.PUT.equals(httpMethod);

        return String.format(
                "%s,%s,%s,%s",
                getKeyId(),
                getAlgorithm(),
                getHeaders(isPutMethod),
                getSignature(targetRequest, requestId, isPutMethod));
    }

    private String buildTargetRequest(HttpMethod httpMethod, String path) {
        return String.format("%s %s", httpMethod.name().toLowerCase(), path);
    }

    private String getKeyId() {
        return String.format("keyId=\"%s\"", keyId);
    }

    private String getAlgorithm() {
        return String.format(
                "algorithm=\"%s\"", BredBanquePopulaireConstants.HeaderGeneratorKeys.ALGORITHM);
    }

    private String getHeaders(boolean isPutMethod) {
        return isPutMethod
                ? String.format(
                        "headers=\"(request-target) %s\"",
                        BredBanquePopulaireConstants.HeaderGeneratorKeys.PUT_HEADERS_KEYS)
                : String.format(
                        "headers=\"(request-target) %s\"",
                        Psd2Headers.Keys.X_REQUEST_ID.toLowerCase());
    }

    private String getSignature(String targetRequest, String requestId, boolean isPutMethod) {
        final String signatureString =
                isPutMethod
                        ? String.join(
                                "\n",
                                "(request-target): " + targetRequest,
                                "x-request-id: " + requestId,
                                "digest: " + getDigest(),
                                "content-type: " + MediaType.APPLICATION_JSON_TYPE)
                        : String.join(
                                "\n",
                                "(request-target): " + targetRequest,
                                "x-request-id: " + requestId);

        return String.format(
                "signature=\"%s\"",
                qsealcSigner.sign(qsealcAlgorithm, signatureString.getBytes()).getBase64Encoded());
    }

    private String getPathForSignature(URL url) {
        final URI uri = url.toUri();
        return Objects.nonNull(uri.getQuery())
                ? String.format("%s?%s", uri.getPath(), uri.getQuery())
                : uri.getPath();
    }

    private String getDigest() {
        return sessionStorage
                .get(BredBanquePopulaireConstants.StorageKeys.DIGEST, String.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find digest."));
    }
}
