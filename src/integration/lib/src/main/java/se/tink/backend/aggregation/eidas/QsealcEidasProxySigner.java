package se.tink.backend.aggregation.eidas;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import se.tink.backend.aggregation.eidas.EidasProxyConstants.Eidas;
import se.tink.backend.aggregation.eidas.EidasProxyConstants.Url;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

/**
 * Requests the eIDAS proxy to sign a string using its eIDAS private key (RSA with SHA256).
 *
 * @return The signature
 */
public final class QsealcEidasProxySigner implements Signer {

    private final TinkHttpClient httpClient;

    public QsealcEidasProxySigner(final TinkHttpClient httpClient) {
        this.httpClient = httpClient;
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
        final String signatureString =
                httpClient
                        .request(Url.EIDAS_SIGN)
                        .header("X-Tink-Eidas-Sign-Certificate-Id", "Tink-qsealc")
                        .type("application/octet-stream")
                        .body(signingString)
                        .post(String.class);

        return Base64.getDecoder().decode(signatureString);
    }
}
