package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.util;

import java.net.URI;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Signature;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public final class SignatureUtil {

    private SignatureUtil() {}

    public static String generateDigest(String data) {
        return Signature.DIGEST_PREFIX + Base64.getEncoder().encodeToString(Hash.sha256(data));
    }

    public static String getSignatureHeaderValue(
            final String keyId,
            final String httpMethod,
            final URI uri,
            final String date,
            final String requestId,
            final EidasProxyConfiguration eidasProxyConf,
            final EidasIdentity eidasIdentity) {

        String signatureEntity = getMandatoryHeadersSignature(httpMethod, uri, date, requestId);

        String signature = signAndEncode(signatureEntity, eidasProxyConf, eidasIdentity);

        return String.join(
                ",",
                "keyId=" + keyId,
                "algorithm=\"rsa-sha256\"",
                "headers=\"(request-target) host date x-request-id\"",
                "signature=\"" + signature + "\"");
    }

    public static String getSignatureHeaderValue(
            final String keyId,
            final String httpMethod,
            final URI uri,
            final String date,
            final String digest,
            final String contentType,
            final String requestId,
            final EidasProxyConfiguration eidasProxyConf,
            final EidasIdentity eidasIdentity) {

        String signatureEntity =
                String.join(
                        "\n",
                        getMandatoryHeadersSignature(httpMethod, uri, date, requestId),
                        "digest: " + digest,
                        "content-type: " + contentType);

        String signature = signAndEncode(signatureEntity, eidasProxyConf, eidasIdentity);

        return String.join(
                ",",
                "keyId=" + keyId,
                "algorithm=\"rsa-sha256\"",
                "headers=\"(request-target) host date x-request-id digest content-type\"",
                "signature=\"" + signature + "\"");
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

    private static String signAndEncode(
            String signatureEntity,
            EidasProxyConfiguration eidasProxyConf,
            EidasIdentity eidasIdentity) {
        return QsealcSignerImpl.build(
                        eidasProxyConf.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity)
                .getSignatureBase64(signatureEntity.getBytes());
    }
}
