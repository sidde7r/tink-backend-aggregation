package se.tink.backend.aggregation.eidas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.eidas.EidasProxyConstants.Eidas;
import se.tink.backend.aggregation.eidas.EidasProxyConstants.Url;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

/** Requests the eIDAS proxy to sign a string using its eIDAS private key (RSA with SHA256). */
public final class QsealcEidasProxySigner implements Signer {

    private static final Logger logger = LoggerFactory.getLogger(QsealcEidasProxySigner.class);
    private static final int TIMEOUT_MS = 5000;

    private final TinkHttpClient httpClient;
    private final URL eidasProxyBaseUrl;
    private final String certificateId;

    public QsealcEidasProxySigner(final URL eidasProxyBaseUrl, final String certificateId) {
        this.eidasProxyBaseUrl = eidasProxyBaseUrl;
        this.certificateId = certificateId;
        this.httpClient = new TinkHttpClient();
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
        httpClient.setSslClientCertificate(readFile(Eidas.CLIENT_P12), Eidas.CLIENT_PASSWORD);
        httpClient.trustRootCaCertificate(readFile(Eidas.DEV_CAS_JKS), Eidas.DEV_CAS_PASSWORD);

        final String signingString = Base64.getEncoder().encodeToString(signingBytes);
        final URL url = eidasProxyBaseUrl.concatWithSeparator(Url.EIDAS_SIGN);

        logger.info("Requesting QSealC signature from {}", url);
        return httpClient
                .request(url)
                .header("X-Tink-Eidas-Sign-Certificate-Id", this.certificateId)
                .type("application/octet-stream")
                .body(signingString)
                .post(String.class);
    }

    private static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
