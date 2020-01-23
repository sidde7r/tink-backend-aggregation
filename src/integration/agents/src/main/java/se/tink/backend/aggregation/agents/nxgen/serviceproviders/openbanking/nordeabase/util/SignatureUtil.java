package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.util;

import java.net.URI;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.Signature;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public final class SignatureUtil {

    private SignatureUtil() {}

    public static String generateDigest(String data) {
        return Signature.DIGEST_PREFIX + Base64.getEncoder().encodeToString(Hash.sha256(data));
    }

    public static String createPostSignature(
            final String keyId,
            final String httpMethod,
            final URI uri,
            final String date,
            final String digest,
            final String contentType,
            final EidasProxyConfiguration eidasProxyConf,
            final EidasIdentity eidasIdentity) {
        String signatureEntity =
                String.join(
                        Signature.DELIMITER_NEXT_LINE,
                        getBaseSignatureEntity(httpMethod, uri, date),
                        Signature.CONTENT_TYPE + contentType,
                        Signature.DIGEST + digest);
        String signature = signAndEncode(signatureEntity, eidasProxyConf, eidasIdentity);

        return String.join(
                Signature.DELIMITER_COMMA,
                Signature.KEY_ID + keyId + Signature.DOUBLE_QUOTE,
                Signature.ALGORITHM,
                Signature.POST_HEADERS,
                Signature.SIGNATURE + signature + Signature.DOUBLE_QUOTE);
    }

    public static String createGetSignature(
            final String keyId,
            final String httpMethod,
            final URI uri,
            final String date,
            final EidasProxyConfiguration eidasProxyConf,
            final EidasIdentity eidasIdentity) {
        String signatureEntity = getBaseSignatureEntity(httpMethod, uri, date);
        String signature = signAndEncode(signatureEntity, eidasProxyConf, eidasIdentity);

        return String.join(
                Signature.DELIMITER_COMMA,
                Signature.KEY_ID + keyId + Signature.DOUBLE_QUOTE,
                Signature.ALGORITHM,
                Signature.GET_HEADERS,
                Signature.SIGNATURE + signature + Signature.DOUBLE_QUOTE);
    }

    private static String getBaseSignatureEntity(String httpMethod, URI uri, String date) {
        String fullPath = httpMethod.toLowerCase() + " " + uri.getPath();
        if (uri.getQuery() != null) {
            fullPath += "?" + uri.getQuery();
        }

        return String.join(
                Signature.DELIMITER_NEXT_LINE,
                Signature.REQUEST_TARGET + fullPath,
                Signature.ORIGINATING_HOST + uri.getHost(),
                Signature.ORIGINATING_DATE + date);
    }

    private static String signAndEncode(
            String signatureEntity,
            EidasProxyConfiguration eidasProxyConf,
            EidasIdentity eidasIdentity) {
        return QsealcSigner.build(
                        eidasProxyConf.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity)
                .getSignatureBase64(signatureEntity.getBytes());
    }
}
