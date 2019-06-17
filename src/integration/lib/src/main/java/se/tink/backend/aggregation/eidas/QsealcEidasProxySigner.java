package se.tink.backend.aggregation.eidas;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.eidas.EidasProxyConstants.Eidas;
import se.tink.backend.aggregation.eidas.EidasProxyConstants.Url;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

/**
 * Requests the eIDAS proxy to sign a string using its eIDAS private key (RSA with SHA256).
 *
 * @return The signature
 */
public final class QsealcEidasProxySigner implements Signer {

    private static Logger logger = LoggerFactory.getLogger(QsealcEidasProxySigner.class);
    private static int TIMEOUT_MS = 5000;

    private final TinkHttpClient httpClient;
    private final URL eidasProxyBaseUrl;

    public QsealcEidasProxySigner(final TinkHttpClient httpClient, final URL eidasProxyBaseUrl) {
        this.httpClient = httpClient;
        this.eidasProxyBaseUrl = eidasProxyBaseUrl;
        httpClient.setTimeout(TIMEOUT_MS);
    }

    @Override
    public byte[] getSignature(final byte[] signingBytes) {
        try {
            httpClient.setSslClientCertificate(
                    Files.toByteArray(new File(Eidas.CLIENT_P12)), Eidas.CLIENT_PASSWORD);
            httpClient.trustRootCaCertificate(
                    Files.toByteArray(new File(Eidas.DEV_CAS_JKS)), Eidas.DEV_CAS_PASSWORD);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        final String signingString = Base64.getEncoder().encodeToString(signingBytes);
        final URL url = eidasProxyBaseUrl.concatWithSeparator(Url.EIDAS_SIGN);

        logger.info("Requesting QSealC signature from {}", url);
        final String signatureString =
                httpClient
                        .request(url)
                        .header("X-Tink-Eidas-Sign-Certificate-Id", "Tink-qsealc")
                        .type("application/octet-stream")
                        .body(signingString)
                        .post(String.class);

        return Base64.getDecoder().decode(signatureString);
    }
}
