package se.tink.backend.aggregation.nxgen.http.legacy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.legacy.entities.JwtBodyEntity;
import se.tink.backend.aggregation.nxgen.http.legacy.entities.JwtHeaderEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

/*
   This HttpRequestExecutor is only necessary because of bugs in the underlying libraries (jersey and apache).
   Adding cookies to single requests will lead to multiple `Cookie` headers because apache adds cookies from
   the internal cookieStore (which is populated by Set-Cookie directives).

   The work-around is to merge all `Cookie` headers into one.

   (This class also removes the header `Cookie2` which is added by apache).
*/
public class TinkApacheHttpRequestExecutor extends HttpRequestExecutor {
    private static final String RESOLVED_APPID_PLACE_HOLDER = "RESOLVEDAPPIDPLACEHOLDER";
    private static final Logger log = LoggerFactory.getLogger(TinkApacheHttpRequestExecutor.class);
    private static final String SIGNATURE_HEADER_KEY = "X-Signature";
    private static final String SIGNATURE_INFO_HEADER_KEY = "X-Signature-Info";
    private static final String EIDAS_CLUSTER_ID_HEADER = "X-Tink-QWAC-ClusterId";
    private static final String EIDAS_APPID_HEADER = "X-Tink-QWAC-AppId";
    // TODO: Make a lib for these shared headers
    private static final String EIDAS_CERTID_HEADER = "X-Tink-QWAC-CertId";
    private static final String EIDAS_PROXY_REQUESTER = "X-Tink-Debug-ProxyRequester";

    private DefaultRequestLoggingAdapter requestLoggingAdapter;

    private static final ImmutableSet<String> ALLOWED_CLUSTERIDS_FOR_QSEALCSIGN =
            ImmutableSet.of(
                    "barnsley-staging",
                    "barnsley-production",
                    "cardiff-staging",
                    "cardiff-production",
                    // To trigger the workaround in proxy for cornwall
                    "cornwall-production",
                    "oxford-preprod",
                    "oxford-production",
                    "kirkby-staging",
                    "kirkby-production",
                    "farnham-staging",
                    "farnham-production",
                    "leeds-staging",
                    "leeds-production",
                    "neston-staging",
                    "neston-preprod",
                    "neston-production");

    private SignatureKeyPair signatureKeyPair;
    private Algorithm algorithm;
    private boolean shouldAddRequestSignature = true;

    private String proxyUsername;
    private String proxyPassword;

    private boolean shouldUseEidasProxy = false;
    private EidasIdentity eidasIdentity;
    private EidasProxyConfiguration eidasProxyConfiguration;

    public void setEidasProxyConfiguration(EidasProxyConfiguration eidasProxyConfiguration) {
        this.eidasProxyConfiguration = eidasProxyConfiguration;
    }

    public void setEidasIdentity(EidasIdentity eidasIdentity) {
        this.eidasIdentity = eidasIdentity;
    }

    public TinkApacheHttpRequestExecutor(SignatureKeyPair signatureKeyPair) {
        if (signatureKeyPair == null || signatureKeyPair.getPrivateKey() == null) {
            return;
        }

        this.signatureKeyPair = signatureKeyPair;

        algorithm =
                Algorithm.RSA256(signatureKeyPair.getPublicKey(), signatureKeyPair.getPrivateKey());
    }

    public void setProxyCredentials(String username, String password) {
        this.proxyUsername = username;
        this.proxyPassword = password;
    }

    public void shouldUseEidasProxy() {
        this.shouldUseEidasProxy = true;
    }

    public void setRequestLoggingAdapter(DefaultRequestLoggingAdapter requestLoggingAdapter) {
        this.requestLoggingAdapter = requestLoggingAdapter;
    }

    @Override
    public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context)
            throws IOException, HttpException {
        // Remove the default "Cookie2" header that ApacheHttp adds
        request.removeHeaders("Cookie2");
        mergeCookieHeaders(request);

        // Authentication towards the EIDAS proxy is with TLS-MA, we don't need to add an auth
        // header for EIDAS requests.
        if (isHttpProxyRequest(request) && (!shouldUseEidasProxy)) {
            addProxyAuthorizationHeader(request);
        } else if (shouldUseEidasProxy) {
            addEidasProxyHeaders(request);
        } else if (shouldAddRequestSignature) {
            addQsealcOrRequestSignature(request);
        }

        if (!isHttpProxyRequest(request)) {
            log(request);
        }
        return super.execute(request, conn, context);
    }

    private void addEidasProxyHeaders(HttpRequest request) {
        request.addHeader(EIDAS_CLUSTER_ID_HEADER, eidasIdentity.getClusterId());
        request.addHeader(EIDAS_APPID_HEADER, eidasIdentity.getAppId());
        request.addHeader(EIDAS_CERTID_HEADER, eidasIdentity.getCertId());
        request.addHeader(EIDAS_PROXY_REQUESTER, eidasIdentity.getRequester());
    }

    private boolean isHttpProxyRequest(HttpRequest request) {
        return "connect".equalsIgnoreCase(request.getRequestLine().getMethod());
    }

    private void addProxyAuthorizationHeader(HttpRequest request) {
        if (Strings.isNullOrEmpty(proxyUsername) || Strings.isNullOrEmpty(proxyPassword)) {
            return;
        }

        // Note: The apache version we use cannot automatically add the `Proxy-Authorization` via
        // proxy authentication
        // configuration.
        // Remove this code once Apache has been updated to a new version where that functionality
        // works.
        request.addHeader(
                "Proxy-Authorization",
                String.format(
                        "Basic %s",
                        Base64.getUrlEncoder()
                                .encodeToString(
                                        String.format("%s:%s", proxyUsername, proxyPassword)
                                                .getBytes())));
    }

    public void disableSignatureRequestHeader() {
        this.shouldAddRequestSignature = false;
    }

    private void mergeCookieHeaders(HttpRequest request) {
        List<Header> cookieHeaders = Arrays.asList(request.getHeaders("Cookie"));
        if (cookieHeaders.size() <= 1) {
            return;
        }

        // Remove them from the request before adding the merged value
        request.removeHeaders("Cookie");

        String cookieValue =
                cookieHeaders.stream().map(Header::getValue).collect(Collectors.joining("; "));

        request.addHeader("Cookie", cookieValue);
    }

    private void addQsealcOrRequestSignature(HttpRequest request) {
        // For RE request, try to add authentication header with corresponding QSealC cert.
        // * If eidasIdentity is null (for legacy agent), fallback to use self signed cert.
        // * If QSealC cert can't be found or other exceptions, fallback as well and log the
        // error.
        if (eidasIdentity != null
                && eidasIdentity.getAppId() != null
                && ALLOWED_CLUSTERIDS_FOR_QSEALCSIGN.contains(eidasIdentity.getClusterId())) {
            try {
                addQsealcSignatureByGetingWholeJwsToken(request);
            } catch (Exception e) {
                log.warn(
                        "QSealC signing failed for appId {} certId {} clusterId {}. Using self signed cert instead.",
                        eidasIdentity.getAppId(),
                        eidasIdentity.getCertId(),
                        eidasIdentity.getClusterId());
                addRequestSignature(request);
            }
        } else {
            addRequestSignature(request);
        }
    }

    private void addQsealcSignatureByGetingWholeJwsToken(HttpRequest request) {
        // TODO: adding the info header once verified
        JwtBodyEntity jwtBody = new JwtBodyEntity();
        RequestLine requestLine = request.getRequestLine();
        jwtBody.setMethod(requestLine.getMethod());
        jwtBody.setUri(requestLine.getUri());
        getHttpHeadersHashAsBase64(request).ifPresent(jwtBody::setHeaders);

        getHttpBodyHashAsBase64(request).ifPresent(jwtBody::setBody);
        jwtBody.setNonce(UUID.randomUUID().toString());
        jwtBody.setIat(OffsetDateTime.now().toEpochSecond());

        String tokenBodyJson = SerializationUtils.serializeToString(jwtBody);
        // TODO, idea is to use appId as keyId and upload cert to corresponding path on CDN
        String tokenHeadJson =
                SerializationUtils.serializeToString(
                        new JwtHeaderEntity(RESOLVED_APPID_PLACE_HOLDER));

        String baseTokenString =
                Base64.getUrlEncoder()
                                .encodeToString(
                                        tokenHeadJson != null
                                                ? tokenHeadJson.getBytes()
                                                : new byte[0])
                        + "."
                        + Base64.getUrlEncoder()
                                .encodeToString(
                                        tokenBodyJson != null
                                                ? tokenBodyJson.getBytes()
                                                : new byte[0]);

        QsealcSigner signer =
                QsealcSignerImpl.build(
                        eidasProxyConfiguration.toInternalConfig(),
                        QsealcAlg.EIDAS_JWT_RSA_SHA256,
                        eidasIdentity);
        String jwt = signer.getJWSToken(baseTokenString.getBytes());
        request.addHeader(SIGNATURE_HEADER_KEY, jwt);
    }

    private void addRequestSignature(HttpRequest request) {
        // TODO remove the signatureKeyPair and algorithm

        if (signatureKeyPair == null || algorithm == null) {
            return;
        }

        // This header needs to be added before we fetch the headers to create the signature.
        // Note: This header can be removed if this URL is added to the JWT.
        request.addHeader(
                SIGNATURE_INFO_HEADER_KEY,
                "Visit https://cdn.tink.se/aggregation-signature/how-to-verify.txt for more info.");

        RequestLine requestLine = request.getRequestLine();

        JWTCreator.Builder jwtBuilder =
                JWT.create()
                        .withIssuedAt(new Date())
                        .withClaim("method", requestLine.getMethod())
                        .withClaim("uri", requestLine.getUri());

        // Only add keyId for request where we use signatureKeyPair
        if (signatureKeyPair != null) {
            jwtBuilder.withKeyId(signatureKeyPair.getKeyId());
        }

        getHttpHeadersHashAsBase64(request)
                .ifPresent(hash -> jwtBuilder.withClaim("headers", hash));

        getHttpBodyHashAsBase64(request).ifPresent(hash -> jwtBuilder.withClaim("body", hash));

        request.addHeader(SIGNATURE_HEADER_KEY, jwtBuilder.sign(algorithm));
    }

    private Optional<String> getHttpBodyHashAsBase64(HttpRequest request) {
        if (!(request instanceof HttpEntityEnclosingRequest)) {
            return Optional.empty();
        }

        HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
        if (entity == null) {
            // Handle the case of an empty post request
            return Optional.empty();
        }

        try {
            byte[] bodyBytes = IOUtils.toByteArray(entity.getContent());
            byte[] digest = Hash.sha256(bodyBytes);

            return Optional.of(EncodingUtils.encodeAsBase64String(digest));
        } catch (IOException e) {
            log.error("Could not get the request body from the entity content", e);
            return Optional.empty();
        }
    }

    private Optional<String> getHttpHeadersHashAsBase64(HttpRequest request) {
        Header[] allHeaders = request.getAllHeaders();

        String sortedHeaders =
                Arrays.stream(allHeaders)
                        .filter(Objects::nonNull)
                        .map(header -> String.format("%s: %s", header.getName(), header.getValue()))
                        .sorted(String::compareTo)
                        .collect(Collectors.joining("\n"));

        if (Strings.isNullOrEmpty(sortedHeaders)) {
            return Optional.empty();
        }

        byte[] headerBytes = sortedHeaders.getBytes(StandardCharsets.UTF_8);
        byte[] digest = Hash.sha256(headerBytes);
        return Optional.of(EncodingUtils.encodeAsBase64String(digest));
    }

    /**
     * Request is logged in executor instead of filter to print all outgoing headers
     *
     * @param httpRequest
     */
    private void log(HttpRequest httpRequest) {
        if (requestLoggingAdapter != null) {
            requestLoggingAdapter.logRequest(httpRequest);
        }
    }
}
