package se.tink.backend.aggregation.nxgen.http.legacy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import se.tink.backend.common.config.SignatureKeyPair;

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

    private SignatureKeyPair signatureKeyPair;
    private Algorithm algorithm;
    private boolean shouldAddRequestSignature = true;

    public TinkApacheHttpRequestExecutor(SignatureKeyPair signatureKeyPair) {
        if (signatureKeyPair == null || signatureKeyPair.getPrivateKey() == null) {
            return;
        }

        this.signatureKeyPair = signatureKeyPair;

        algorithm = Algorithm.RSA256(signatureKeyPair.getPublicKey(), signatureKeyPair.getPrivateKey());
    }

    @Override
    public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context)
            throws IOException, HttpException {
        // Remove the default "Cookie2" header that ApacheHttp adds
        request.removeHeaders("Cookie2");
        mergeCookieHeaders(request);

        if (shouldAddRequestSignature) {
            addRequestSignature(request);
        }

        return super.execute(request, conn, context);
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

        String cookieValue = cookieHeaders.stream()
                .map(Header::getValue)
                .collect(Collectors.joining("; "));

        request.addHeader("Cookie", cookieValue);
    }

    private void addRequestSignature(HttpRequest request) {
        if (signatureKeyPair == null || algorithm == null) {
            return;
        }

        // This header needs to be added before we fetch the headers to create the signature.
        // Note: This header can be removed if this URL is added to the JWT.
        request.addHeader("X-Signature-Info",
                "Visit https://cdn.tink.se/aggregation-signature/how-to-verify.txt for more info.");

        RequestLine requestLine = request.getRequestLine();

        JWTCreator.Builder jwtBuilder = JWT.create()
                .withIssuedAt(new Date())
                .withClaim("method", requestLine.getMethod())
                .withClaim("uri", requestLine.getUri());

        // Only add keyId for request where we use signatureKeyPair
        if (signatureKeyPair != null) {
            jwtBuilder.withKeyId(signatureKeyPair.getKeyId());
        }

        getHttpHeadersHashAsBase64(request)
                .ifPresent(hash -> jwtBuilder.withClaim("headers", hash));

        getHttpBodyHashAsBase64(request)
                .ifPresent(hash -> jwtBuilder.withClaim("body", hash));

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

        String sortedHeaders = Arrays.stream(allHeaders)
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
