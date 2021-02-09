package se.tink.backend.aggregation.eidassigner;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public class QsealcSignerImpl implements QsealcSigner {

    private static final Logger log = LoggerFactory.getLogger(QsealcSignerImpl.class);

    private static final String TINK_QSEALC_APPID = "X-Tink-QSealC-AppId";
    private static final String TINK_QSEALC_CERTID = "X-Tink-QSealC-CertId";
    private static final String TINK_QSEALC_CLUSTERID = "X-Tink-QSealC-ClusterId";
    private static final String TINK_REQUESTER = "X-SignRequester";

    private final QsealcSignerHttpClient qsealcSignerHttpClient;
    private final QsealcAlg alg;
    private final String host;
    private final EidasIdentity eidasIdentity;

    private QsealcSignerImpl(
            QsealcSignerHttpClient qsealcSignerHttpClient,
            QsealcAlg alg,
            String host,
            EidasIdentity eidasIdentity) {
        this.qsealcSignerHttpClient = qsealcSignerHttpClient;
        this.alg = alg;
        this.host = host;
        this.eidasIdentity = eidasIdentity;
    }

    /**
     * This is the preferred builder. This will send the cluster and app ID's chosen in the
     * EidasIdentity object.
     */
    public static QsealcSigner build(
            InternalEidasProxyConfiguration conf, QsealcAlg alg, EidasIdentity eidasIdentity) {
        try {
            log.info("Return a singleton httpclient");
            return new QsealcSignerImpl(
                    QsealcSignerHttpClient.create(conf), alg, conf.getHost(), eidasIdentity);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] callSecretsService(byte[] signingData) {

        try {
            HttpPost post =
                    new HttpPost(StringUtils.stripEnd(this.host, "/") + alg.getSigningType());
            post.setHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            if (!Strings.isNullOrEmpty(eidasIdentity.getAppId())) {
                post.setHeader(TINK_QSEALC_APPID, eidasIdentity.getAppId());
            }
            post.setHeader(TINK_QSEALC_CERTID, eidasIdentity.getCertId());
            post.setHeader(TINK_QSEALC_CLUSTERID, eidasIdentity.getClusterId());
            post.setHeader(TINK_REQUESTER, eidasIdentity.getRequester());
            post.setEntity(new ByteArrayEntity(Base64.getEncoder().encode(signingData)));
            long start = System.nanoTime();
            try (CloseableHttpResponse response = qsealcSignerHttpClient.execute(post)) {
                long total = System.nanoTime() - start;
                long eidasSigningRoundtrip = TimeUnit.SECONDS.convert(total, TimeUnit.NANOSECONDS);
                if (eidasSigningRoundtrip > 0) {
                    log.info("Eidas signing time: {} seconds", eidasSigningRoundtrip);
                }

                byte[] responseBytes = EntityUtils.toByteArray(response.getEntity());
                final int statusCode = response.getStatusLine().getStatusCode();
                response.close();

                if (statusCode != HttpStatus.SC_OK) {
                    throw new QsealcSignerException(
                            "Unexpected status code "
                                    + response.getStatusLine()
                                    + " requesting QSealC signature: "
                                    + new String(responseBytes));
                }
                return responseBytes;
            }
        } catch (IOException ex) {
            throw new QsealcSignerException("IOException when requesting QSealC signature", ex);
        }
    }

    @Override
    public String getSignatureBase64(byte[] signingData) {
        return new String(callSecretsService(signingData), Charsets.US_ASCII);
    }

    @Override
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
     * Signature privateSignature = Signature.getInsta<nce("SHA256withRSA");
     * privateSignature.initSign(privateKey);
     * privateSignature.update(signingData);
     * byte[] sig = privateSignature.sign();
     * }</pre>
     *
     * @param signingData the data to be signed
     * @return the signature in a 512-byte array
     * @throws QsealcSignerException if the signature could not be retrieved
     */
    @Override
    public byte[] getSignature(byte[] signingData) {
        return Base64.getDecoder().decode(callSecretsService(signingData));
    }
}
