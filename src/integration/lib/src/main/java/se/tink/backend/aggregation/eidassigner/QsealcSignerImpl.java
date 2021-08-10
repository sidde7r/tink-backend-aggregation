package se.tink.backend.aggregation.eidassigner;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map.Entry;
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
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.libraries.tracing.lib.api.Tracing;

public class QsealcSignerImpl implements QsealcSigner {

    private static final Logger log = LoggerFactory.getLogger(QsealcSignerImpl.class);

    private static final String TINK_QSEALC_APPID = "X-Tink-QSealC-AppId";
    private static final String TINK_QSEALC_CERTID = "X-Tink-QSealC-CertId";
    private static final String TINK_QSEALC_PROVIDERID = "X-Tink-QSealC-ProviderId";
    private static final String TINK_QSEALC_CLUSTERID = "X-Tink-QSealC-ClusterId";
    private static final String TINK_REQUESTER = "X-SignRequester";

    private final QsealcSignerHttpClient qsealcSignerHttpClient;
    private final QsealcAlg alg;
    private final String host;
    private final EidasIdentity eidasIdentity;
    private Span span;
    private Scope scope;

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
            post.setHeader(TINK_QSEALC_PROVIDERID, eidasIdentity.getProviderId());
            post.setHeader(TINK_QSEALC_CLUSTERID, eidasIdentity.getClusterId());
            post.setHeader(TINK_REQUESTER, eidasIdentity.getRequester());
            post.setEntity(new ByteArrayEntity(Base64.getEncoder().encode(signingData)));
            log.info("Sign data with EidasIdentity setting: {}", eidasIdentity);
            createClientTraceSpan(post);
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
        } finally {
            endTraceSpan();
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
    @Override
    public byte[] getSignature(byte[] signingData) {
        return Base64.getDecoder().decode(callSecretsService(signingData));
    }

    private void createClientTraceSpan(HttpPost request) {
        try {
            Tracer tracer = Tracing.getTracer();
            String spanName = getSpanName(request);
            span = tracer.buildSpan(spanName).start();
            scope = tracer.activateSpan(span);
            Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);
            Tags.HTTP_METHOD.set(span, request.getMethod());
            Tags.HTTP_URL.set(span, request.getURI().toString());

            tracer.inject(
                    span.context(),
                    Format.Builtin.HTTP_HEADERS,
                    new QsealcSignerImpl.RequestBuilderCarrier(request));

        } catch (Exception e) {
            log.warn("Failed to start trace: {}", e.getMessage());
        }
    }

    private void endTraceSpan() {
        try {
            scope.close();
            span.finish();
        } catch (Exception e) {
            log.info("Failed to end trace: {}", e.getMessage());
        }
    }

    private String getSpanName(HttpPost request) {
        try {
            String path = request.getURI().getPath();
            return "Outgoing HTTP call: AggregationService"
                    + path.replaceAll("/[a-fA-F0-9]{32}]", "/<nbr>");
        } catch (Exception e) {
            return "Outgoing HTTP call: unable to parse path";
        }
    }

    private static class RequestBuilderCarrier implements TextMap {
        private final HttpPost request;

        RequestBuilderCarrier(HttpPost request) {
            this.request = request;
        }

        @Override
        public Iterator<Entry<String, String>> iterator() {
            throw new UnsupportedOperationException("carrier is write-only");
        }

        @Override
        public void put(String key, String value) {
            request.addHeader(key, value);
        }
    }
}
