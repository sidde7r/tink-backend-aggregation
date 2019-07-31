package se.tink.backend.aggregation.eidas;

import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

/** Requests the eIDAS proxy to sign a string using its eIDAS private key (RSA with SHA256). */
public final class QsealcEidasProxySigner implements Signer {

    private static final Logger logger = LoggerFactory.getLogger(QsealcEidasProxySigner.class);
    private static final int TIMEOUT_MS = 5000;

    private final TinkHttpClient httpClient;
    private final URL eidasProxyBaseUrl;
    private final String certificateId;
    private final String signingType;

    @Deprecated
    public QsealcEidasProxySigner(
            final EidasProxyConfiguration proxyConfig, final String certificateId) {
        this(proxyConfig, certificateId, EidasProxyConstants.Algorithm.EIDAS_RSA_SHA256);
    }

    public QsealcEidasProxySigner(
            final EidasProxyConfiguration proxyConfig,
            final String certificateId,
            final EidasProxyConstants.Algorithm algorithm) {
        this.eidasProxyBaseUrl = new URL(proxyConfig.getHost());
        this.certificateId = certificateId;
        this.signingType = algorithm.getSigningType();
        this.httpClient = new TinkHttpClient();
        this.httpClient.setEidasSign(proxyConfig);
        httpClient.setTimeout(TIMEOUT_MS);
        httpClient.setDebugOutput(true);
    }

    @Override
    public byte[] getSignature(final byte[] signingBytes) {
        final String signatureString = getSignatureFromProxy(signingBytes);
        return Base64.getDecoder().decode(signatureString);
    }

    public String getSignatureBase64(final byte[] signingBytes) {
        return getSignatureFromProxy(signingBytes);
    }

    private String getSignatureFromProxy(final byte[] signingBytes) {
        final String signingString = Base64.getEncoder().encodeToString(signingBytes);
        final URL url = eidasProxyBaseUrl.concatWithSeparator(signingType);

        logger.info("Requesting QSealC signature from {}", url);
        return httpClient
                .request(url)
                .header("X-Tink-Eidas-Sign-Certificate-Id", certificateId)
                .type("application/octet-stream")
                .body(signingString)
                .post(String.class);
    }
}
