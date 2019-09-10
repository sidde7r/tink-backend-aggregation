package se.tink.backend.aggregation.nxgen.http.legacy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.charset.Charset;
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
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
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
    private static final Logger log = LoggerFactory.getLogger(TinkApacheHttpRequestExecutor.class);
    private static final String SIGNATURE_HEADER_KEY = "X-Signature";
    private static final String EIDAS_CERTIFICATE_ID_HEADER = "X-Tink-Eidas-Proxy-Certificate-Id";
    private static final String EIDAS_CLUSTER_ID_HEADER = "X-Tink-QWAC-ClusterId";
    private static final String EIDAS_APPID_HEADER = "X-Tink-QWAC-AppId";
    private static final String EIDAS_PROXY_REQUESTER = "X-Tink-Debug-ProxyRequester";

    private SignatureKeyPair signatureKeyPair;
    private Algorithm algorithm;
    private boolean shouldAddRequestSignature = true;

    private String proxyUsername;
    private String proxyPassword;

    private boolean shouldUseEidasProxy = false;
    private String legacyCertId;
    private EidasIdentity eidasIdentity;
    private boolean shouldQsealcJwt = false;
    private EidasProxyConfiguration eidasProxyConfiguration;

    public void setShouldQsealcJwt(boolean shouldQsealcJwt) {
        this.shouldQsealcJwt = shouldQsealcJwt;
    }

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

    public void setLegacyCertId(String legacyCertId) {
        this.shouldUseEidasProxy = true;
        this.legacyCertId = legacyCertId;
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
            request.addHeader(EIDAS_CERTIFICATE_ID_HEADER, legacyCertId);
            request.addHeader(EIDAS_CLUSTER_ID_HEADER, eidasIdentity.getClusterId());
            request.addHeader(EIDAS_APPID_HEADER, eidasIdentity.getAppId());
            request.addHeader(EIDAS_PROXY_REQUESTER, eidasIdentity.getRequester());
        } else if (shouldAddRequestSignature) {
            // Do not add a signature header on the proxy requests.
            // This is because we don't want to leak unnecessary information to proxy providers.
            //
            // Requests to the EIDAS proxy do not need to be signed. The proxy will sign the request
            // on the way out if necessary.
            if (eidasIdentity != null && !eidasIdentity.getClusterId().contains("farnham")) {
                addQsealcSignature(request);
            } else {
                addRequestSignature(request);
            }
        }

        return super.execute(request, conn, context);
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

    private void addQsealcSignature(HttpRequest request) {

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
                SerializationUtils.serializeToString(new JwtHeaderEntity(eidasIdentity.getAppId()));

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
                QsealcSigner.build(
                        eidasProxyConfiguration.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity);

        String signature = signer.getSignatureBase64(baseTokenString.getBytes());
        String jwt = baseTokenString + "." + signature;
        log.info("QSEALC Signature is :  {}", jwt);
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
                "X-Signature-Info",
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

        byte[] headerBytes = sortedHeaders.getBytes(Charset.forName("UTF-8"));
        byte[] digest = Hash.sha256(headerBytes);
        return Optional.of(EncodingUtils.encodeAsBase64String(digest));
    }
}
