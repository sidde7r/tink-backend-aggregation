package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.utils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.SwedbankFallbackConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.SwedbankFallbackConstants.HeadersToSign;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SignatureUtils {

    public static String getDigestHeaderValue(HttpRequest request) {
        final String serializedBody = serializeBodyIfNecessary(request);

        return Headers.DIGEST_PREFIX
                + Base64.getEncoder().encodeToString(Hash.sha256(serializedBody));
    }

    public static String generateSignatureHeader(
            Map<String, Object> headers,
            EidasProxyConfiguration eidasProxyConf,
            EidasIdentity eidasIdentity,
            String qSealc) {
        QsealcSigner signer =
                QsealcSignerImpl.build(
                        eidasProxyConf.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity);

        String signedHeaders =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .collect(Collectors.joining(" "));

        String signedHeadersWithValues =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .map(header -> String.format("%s: %s", header, headers.get(header)))
                        .collect(Collectors.joining("\n"));

        String signature = signer.getSignatureBase64(signedHeadersWithValues.getBytes());

        return String.format(
                Headers.SIGNATURE_HEADER,
                Psd2Headers.getTppCertificateKeyId(getX509Certificate(qSealc)),
                signedHeaders,
                signature);
    }

    private static String serializeBodyIfNecessary(HttpRequest request) {
        Object requestBody = request.getBody();

        return requestBody instanceof String
                ? (String) requestBody
                : SerializationUtils.serializeToString(requestBody);
    }

    private static X509Certificate getX509Certificate(String qSealc) {
        try {
            return CertificateUtils.getX509CertificatesFromBase64EncodedCert(qSealc).stream()
                    .findFirst()
                    .get();
        } catch (CertificateException ce) {
            throw new SecurityException("Certificate error", ce);
        }
    }
}
