package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.util;

import java.net.URI;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Signature;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public final class SignatureUtil {

    private SignatureUtil() {}

    public static String generateDigest(String data) {
        return Signature.DIGEST_PREFIX + Base64.getEncoder().encodeToString(Hash.sha256(data));
    }

    public static String getSignatureHeaderValue(
            final String keyId,
            final String httpMethod,
            URI uri,
            final String date,
            final String requestId,
            final EidasProxyConfiguration eidasProxyConf,
            EidasIdentity eidasIdentity) {

        String signatureEntity =
                String.join(
                        "\n",
                        "(request-target): " + httpMethod.toLowerCase() + " " + uri.getPath(),
                        "host: " + uri.getHost(),
                        "date: " + date,
                        "x-request-id: " + requestId);

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
            EidasIdentity eidasIdentity) {

        String signatureEntity =
                String.join(
                        "\n",
                        "(request-target): " + httpMethod.toLowerCase() + " " + uri.getPath(),
                        "host: " + uri.getHost(),
                        "date: " + date,
                        "x-request-id: " + requestId,
                        "digest: " + digest,
                        "content-type: " + contentType);

        String signature = signAndEncode(signatureEntity, eidasProxyConf, eidasIdentity);

        return String.join(
                ",",
                "keyId=" + keyId,
                "algorithm=\"rsa-sha256\"",
                "headers=\"(request-target) host date x-request-id\"",
                "signature=\"" + signature + "\"");
    }

    private static String signAndEncode(
            String signatureEntity,
            EidasProxyConfiguration eidasProxyConf,
            EidasIdentity eidasIdentity) {
        return QsealcSigner.build(
                        eidasProxyConf.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity,
                        "Tink")
                .getSignatureBase64(signatureEntity.getBytes());
    }
}
