package se.tink.backend.aggregation.eidassigner;

import com.google.common.base.Charsets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Base64;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.http.truststrategy.TrustRootCaStrategy;

public class QsealcSigner {

    private static final String TINK_QSEALC_APPID = "X-Tink-Eidas-Sign-Certificate-Id";
    private static final String TINK_QSEALC_CLUSTERID = "X-Tink-QSealC-ClusterId";

    private final HttpClient httpClient;
    private final QsealcAlg alg;
    private final String host;
    private final String appId;
    private final String clusterId;

    private QsealcSigner(
            HttpClient httpClient, QsealcAlg alg, String host, String appId, String clusterId) {
        this.httpClient = httpClient;
        this.alg = alg;
        this.host = host;
        this.appId = appId;
        this.clusterId = clusterId;
    }

    public static QsealcSigner build(
            InternalEidasProxyConfiguration conf, QsealcAlg alg, String appId, String clusterId) {
        try {

            KeyStore trustStore = conf.getRootCaTrustStore();
            KeyStore keyStore = conf.getClientCertKeystore();
            SSLContext sslContext =
                    new SSLContextBuilder()
                            .loadTrustMaterial(
                                    trustStore,
                                    TrustRootCaStrategy.createWithoutFallbackTrust(trustStore))
                            .loadKeyMaterial(keyStore, "changeme".toCharArray())
                            .build();

            HttpClient httpClient =
                    HttpClients.custom()
                            .setHostnameVerifier(new AllowAllHostnameVerifier())
                            .setSslcontext(sslContext)
                            .build();
            return new QsealcSigner(httpClient, alg, conf.getHost(), appId, clusterId);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] getSignatureBase64Bytes(byte[] signingData) {
        try {
            HttpPost post =
                    new HttpPost(StringUtils.stripEnd(this.host, "/") + alg.getSigningType());
            post.setHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            post.setHeader(TINK_QSEALC_APPID, appId);
            post.setHeader(TINK_QSEALC_CLUSTERID, clusterId);
            post.setEntity(new ByteArrayEntity(Base64.getEncoder().encode(signingData)));
            HttpResponse response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new QsealcSignerException(
                        "Unexpected status code "
                                + response.getStatusLine()
                                + " requesting QSealC signature");
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            response.getEntity().writeTo(outputStream);
            return outputStream.toByteArray();

        } catch (IOException ex) {
            throw new QsealcSignerException("IOException when requesting QSealC signature", ex);
        }
    }

    public String getSignatureBase64(byte[] signingData) {
        return new String(getSignatureBase64Bytes(signingData), Charsets.US_ASCII);
    }

    /**
     * Ask the proxy for a signature.
     *
     * <pre>{@code byte[] sig QsealcSigner.getSignature(signingData); }
     * </pre>
     *
     * <p>is equivalent to:
     *
     * <pre>{@code
     * Signature privateSignature = Signature.getInstance("SHA256withRSA");
     * privateSignature.initSign(privateKey);
     * privateSignature.update(signingData);
     * byte[] sig = privateSignature.sign();
     * }</pre>
     *
     * @param signingData the data to be signed
     * @return the signature in a 512-byte array
     * @throws QsealcSignerException if the signature could not be retrieved
     */
    public byte[] getSignature(byte[] signingData) {
        return Base64.getDecoder().decode(getSignatureBase64Bytes(signingData));
    }
}
