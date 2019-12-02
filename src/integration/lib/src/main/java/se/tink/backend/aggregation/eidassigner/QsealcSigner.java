package se.tink.backend.aggregation.eidassigner;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;

public class QsealcSigner {

    private static final Logger log = LoggerFactory.getLogger(QsealcSigner.class);

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
            return new QsealcSigner(
                    QsealcSignerHttpClient.getHttpClient(conf),
                    alg,
                    conf.getHost(),
                    oldCertId,
                    eidasIdentity);
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

    private byte[] callSecretsService(byte[] signingData) {

        try {
            HttpPost post =
                    new HttpPost(StringUtils.stripEnd(this.host, "/") + alg.getSigningType());
            post.setHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            if (!Strings.isNullOrEmpty(eidasIdentity.getAppId())) {
                post.setHeader(TINK_QSEALC_APPID, eidasIdentity.getAppId());
            }
            post.setHeader(TINK_QSEALC_CLUSTERID, eidasIdentity.getClusterId());
            post.setHeader(TINK_REQUESTER, eidasIdentity.getRequester());
            post.setEntity(new ByteArrayEntity(Base64.getEncoder().encode(signingData)));
            long start = System.nanoTime();
            HttpResponse response = httpClient.execute(post);
            long total = System.nanoTime() - start;
            long eidasSigningRoundtrip = TimeUnit.SECONDS.convert(total, TimeUnit.NANOSECONDS);
            if (eidasSigningRoundtrip > 0) {
                log.info("Eidas signing time: {} seconds", eidasSigningRoundtrip);
            }

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
        return new String(callSecretsService(signingData), Charsets.US_ASCII);
    }

    public String getJWSToken(byte[] jwsTokenData) {
        return new String(Base64.getDecoder().decode(callSecretsService(jwsTokenData)));
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
        return Base64.getDecoder().decode(callSecretsService(signingData));
    }
}
