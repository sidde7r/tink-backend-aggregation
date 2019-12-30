package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import java.net.URI;
import java.util.Base64;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class BoursoramaSignatureHeaderGenerator {

    private EidasProxyConfiguration eidasProxyConf;
    private EidasIdentity eidasIdentity;
    private String qwacKeyUrl;

    public void setConfiguration(
            EidasProxyConfiguration eidasProxyConf,
            EidasIdentity eidasIdentity,
            String qwacKeyUrl) {
        this.eidasProxyConf = eidasProxyConf;
        this.eidasIdentity = eidasIdentity;
        this.qwacKeyUrl = qwacKeyUrl;
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
                "keyId=" + '"' + qwacKeyUrl + '"',
                "algorithm=\"rsa-sha256\"",
                "headers=\"(request-target) Digest X-Request-Id date content-type\"",
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

        return QsealcSigner.build(
                        eidasProxyConf.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity,
                        "Tink")
                .getSignatureBase64(signatureEntity.getBytes());
    }
}
