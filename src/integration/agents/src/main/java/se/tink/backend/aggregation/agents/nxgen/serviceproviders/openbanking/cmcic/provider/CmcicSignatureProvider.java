package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider;

import java.net.URI;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;

@RequiredArgsConstructor
public class CmcicSignatureProvider {

    private final QsealcSigner qsealcSigner;

    public String getSignatureHeaderValueForGet(
            final String keyId, final URI uri, final String date, final String requestId) {

        final String signatureEntity =
                getMandatoryHeadersSignature(HttpMethod.GET.name(), uri, date, requestId);
        final String signature = signAndEncode(signatureEntity);

        return String.join(
                ",",
                "keyId=" + keyId,
                "algorithm=\"rsa-sha256\"",
                "headers=\"(request-target) host date x-request-id\"",
                "signature=\"" + signature + "\"");
    }

    public String getSignatureHeaderValueForPost(
            final String keyId,
            final URI uri,
            final String date,
            final String digest,
            final String requestId) {

        String signatureEntity =
                String.join(
                        "\n",
                        getMandatoryHeadersSignature(HttpMethod.POST.name(), uri, date, requestId),
                        "digest: " + digest,
                        "content-type: " + MediaType.APPLICATION_JSON);

        final String signature = signAndEncode(signatureEntity);

        return String.join(
                ",",
                "keyId=" + keyId,
                "algorithm=\"rsa-sha256\"",
                "headers=\"(request-target) host date x-request-id digest content-type\"",
                "signature=\"" + signature + "\"");
    }

    private String signAndEncode(String signatureEntity) {
        return qsealcSigner.getSignatureBase64(signatureEntity.getBytes());
    }

    private static String getMandatoryHeadersSignature(
            String httpMethod, URI uri, String date, String requestId) {

        String fullPath = httpMethod.toLowerCase() + " " + uri.getPath();
        if (uri.getQuery() != null) {
            fullPath += "?" + uri.getQuery();
        }

        return String.join(
                "\n",
                "(request-target): " + fullPath,
                "host: " + uri.getHost(),
                "date: " + date,
                "x-request-id: " + requestId);
    }
}
