package se.tink.backend.aggregation.eidassigner;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
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

    private static final String TINK_QSEALC_OLD_CERTID = "X-Tink-Eidas-Sign-Certificate-Id";
    private static final String TINK_QSEALC_APPID = "X-Tink-QSealC-AppId";
    private static final String TINK_QSEALC_CLUSTERID = "X-Tink-QSealC-ClusterId";
    private static final String TINK_REQUESTER = "X-SignRequester";

    private final HttpClient httpClient;
    private final QsealcAlg alg;
    private final String host;
    private final String oldCertId;
    private final EidasIdentity eidasIdentity;

    private QsealcSigner(
            HttpClient httpClient,
            QsealcAlg alg,
            String host,
            String oldCertId,
            EidasIdentity eidasIdentity) {
        this.httpClient = httpClient;
        this.alg = alg;
        this.host = host;
        this.oldCertId = oldCertId;
        this.eidasIdentity = eidasIdentity;
    }

    /**
     * This is the preferred builder. This will send the cluster and app ID's chosen in the
     * EidasIdentity object.
     */
    public static QsealcSigner build(
            InternalEidasProxyConfiguration conf, QsealcAlg alg, EidasIdentity eidasIdentity) {
        return build(conf, alg, eidasIdentity, null);
    }

    /**
     * This builder should be used for 'legacy' agents in clusters where the ESS is not deployed yet
     * and a certificate override must be provided. 'oldCertId' will override the cert chosen by
     * 'appId'.
     */
    public static QsealcSigner build(
            InternalEidasProxyConfiguration conf,
            QsealcAlg alg,
            EidasIdentity eidasIdentity,
            String oldCertId) {
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
            return new QsealcSigner(httpClient, alg, conf.getHost(), oldCertId, eidasIdentity);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Deprecated
    public static QsealcSigner build(
            InternalEidasProxyConfiguration conf,
            QsealcAlg alg,
            String oldCertId,
            String clusterId) {
        StackTraceElement e = Thread.currentThread().getStackTrace()[2];
        String requester = e.getClassName() + "." + e.getMethodName() + ":" + e.getLineNumber();
        return build(conf, alg, new EidasIdentity(clusterId, "", requester), oldCertId);
    }

    private byte[] getSignatureBase64Bytes(byte[] signingData) {

        try {
            HttpPost post =
                    new HttpPost(StringUtils.stripEnd(this.host, "/") + alg.getSigningType());
            post.setHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            post.setHeader(TINK_QSEALC_OLD_CERTID, oldCertId);
            if (!Strings.isNullOrEmpty(eidasIdentity.getAppId())) {
                post.setHeader(TINK_QSEALC_APPID, eidasIdentity.getAppId());
            }
            post.setHeader(TINK_QSEALC_CLUSTERID, eidasIdentity.getClusterId());
            post.setHeader(TINK_REQUESTER, eidasIdentity.getSignRequester());
            post.setEntity(new ByteArrayEntity(Base64.getEncoder().encode(signingData)));
            HttpResponse response = httpClient.execute(post);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            response.getEntity().writeTo(outputStream);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new QsealcSignerException(
                        "Unexpected status code "
                                + response.getStatusLine()
                                + " requesting QSealC signature: "
                                + new String(outputStream.toByteArray()));
            }
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
