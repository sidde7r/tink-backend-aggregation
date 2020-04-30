package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import java.net.URI;
import java.util.Base64;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public class BoursoramaSignatureHeaderGenerator {

    private final EidasProxyConfiguration eidasProxyConf;
    private final EidasIdentity eidasIdentity;
    private final String qsealKeyUrl;

    public BoursoramaSignatureHeaderGenerator(
            EidasProxyConfiguration eidasProxyConf,
            EidasIdentity eidasIdentity,
            String qsealKeyUrl) {
        this.eidasProxyConf = eidasProxyConf;
        this.eidasIdentity = eidasIdentity;
        this.qsealKeyUrl = qsealKeyUrl;
    }

    String getDigestHeaderValue(String requestBody) {
        return "SHA-256=" + Base64.getEncoder().encodeToString(Hash.sha256(requestBody));
    }

    String getSignatureHeaderValue(
            String httpMethod,
            URI uri,
            String digest,
            String xRequestId,
            String date,
            String contentType) {

        String signatureEntity =
                getMandatoryHeadersSignature(
                        httpMethod, uri, digest, xRequestId, date, contentType);
        String signature = signAndEncode(signatureEntity, eidasProxyConf, eidasIdentity);

        return String.join(
                ",",
                "keyId=" + '"' + qsealKeyUrl + '"',
                "algorithm=\"rsa-sha256\"",
                "headers=\"(request-target) Digest X-Request-ID Date Content-Type\"",
                "signature=" + '"' + signature + '"');
    }

    private String getMandatoryHeadersSignature(
            String httpMethod,
            URI uri,
            String digest,
            String xRequestId,
            String date,
            String contentType) {

        String fullPath = httpMethod.toLowerCase() + " " + uri.getPath();
        if (uri.getQuery() != null) {
            fullPath += "?" + uri.getQuery();
        }

        return String.join(
                "\n",
                "(request-target): " + fullPath,
                "digest: " + digest,
                "x-request-id: " + xRequestId,
                "date: " + date,
                "content-type: " + contentType);
    }

    private String signAndEncode(
            String signatureEntity,
            EidasProxyConfiguration eidasProxyConf,
            EidasIdentity eidasIdentity) {

        return QsealcSignerImpl.build(
                        eidasProxyConf.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity,
                        "Tink")
                .getSignatureBase64(signatureEntity.getBytes());
    }
}
