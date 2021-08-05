package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;

@RequiredArgsConstructor
public class BoursoramaSignatureHeaderGenerator {

    private final QsealcSigner qsealcSigner;
    private final String qsealKeyUrl;

    String getDigestHeaderValue(String requestBody) {
        return "SHA-256="
                + Base64.getEncoder()
                        .encodeToString(Hash.sha256(requestBody.getBytes(StandardCharsets.UTF_8)));
    }

    String getSignatureHeaderValueForGet(URI uri, String digest, String xRequestId, String date) {

        final String signatureEntity =
                getMandatoryHeadersSignatureForGet(uri, digest, xRequestId, date);
        final String signature = signAndEncode(signatureEntity);

        return String.join(
                ",",
                "keyId=" + '"' + qsealKeyUrl + '"',
                "algorithm=\"rsa-sha256\"",
                "headers=\"(request-target) x-request-id date digest\"",
                "signature=" + '"' + signature + '"');
    }

    String getSignatureHeaderValueForPost(URI uri, String digest, String xRequestId, String date) {

        final String signatureEntity =
                getMandatoryHeadersSignatureForPost(uri, digest, xRequestId, date);
        final String signature = signAndEncode(signatureEntity);

        return String.join(
                ",",
                "keyId=" + '"' + qsealKeyUrl + '"',
                "algorithm=\"rsa-sha256\"",
                "headers=\"(request-target) content-type x-request-id date digest\"",
                "signature=" + '"' + signature + '"');
    }

    private String getMandatoryHeadersSignatureForGet(
            URI uri, String digest, String xRequestId, String date) {

        String fullPath = HttpMethod.GET.name().toLowerCase() + " " + uri.getPath();
        if (uri.getQuery() != null) {
            fullPath += "?" + uri.getQuery();
        }

        return String.join(
                "\n",
                "(request-target): " + fullPath,
                "x-request-id: " + xRequestId,
                "date: " + date,
                "digest: " + digest);
    }

    private String getMandatoryHeadersSignatureForPost(
            URI uri, String digest, String xRequestId, String date) {

        final String fullPath = HttpMethod.POST.name().toLowerCase() + " " + uri.getPath();

        return String.join(
                "\n",
                "(request-target): " + fullPath,
                "content-type: " + MediaType.APPLICATION_JSON,
                "x-request-id: " + xRequestId,
                "date: " + date,
                "digest: " + digest);
    }

    private String signAndEncode(String signatureEntity) {
        return qsealcSigner.getSignatureBase64(signatureEntity.getBytes(StandardCharsets.UTF_8));
    }
}
