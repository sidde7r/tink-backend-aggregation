package se.tink.backend.aggregation.nxgen.http.legacy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import tink.org.apache.http.Header;
import tink.org.apache.http.HttpClientConnection;
import tink.org.apache.http.HttpEntity;
import tink.org.apache.http.HttpEntityEnclosingRequest;
import tink.org.apache.http.HttpException;
import tink.org.apache.http.HttpRequest;
import tink.org.apache.http.HttpResponse;
import tink.org.apache.http.RequestLine;
import tink.org.apache.http.protocol.HttpContext;
import tink.org.apache.http.protocol.HttpRequestExecutor;
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
        request.addHeader("X-Signature-Info",
                "Visit https://cdn.tink.se/aggregation-signature/how-to-verify.txt for more info.");

        RequestLine requestLine = request.getRequestLine();
        Header[] allHeaders = request.getAllHeaders();

        JWTCreator.Builder requestSignatureHeader = JWT.create()
                .withIssuedAt(new Date())
                .withClaim("method", requestLine.getMethod())
                .withClaim("uri", requestLine.getUri())
                .withClaim("headers", toSignatureFormat(allHeaders));

        // Only add keyId for request where we use signatureKeyPair
        if (signatureKeyPair != null) {
            requestSignatureHeader.withKeyId(signatureKeyPair.getKeyId());
        }

        if (!(request instanceof HttpEntityEnclosingRequest)) {
            request.addHeader(SIGNATURE_HEADER_KEY, requestSignatureHeader.sign(algorithm));
            return;
        }

        try {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();

            // Handle the case of a empty post request
            if (entity == null) {
                request.addHeader(SIGNATURE_HEADER_KEY, requestSignatureHeader.sign(algorithm));
                return;
            }

            String requestBody = IOUtils.toString(entity.getContent(), "UTF-8");
            byte[] hashedRequestBody = Hash.sha256(requestBody);
            requestSignatureHeader.withClaim("body", EncodingUtils.encodeAsBase64String(hashedRequestBody));
        } catch (IOException e) {
            log.error("Could not get the request body from the entity content", e);
        }

        request.addHeader(SIGNATURE_HEADER_KEY, requestSignatureHeader.sign(algorithm));
    }

    private static String toSignatureFormat(Header[] headers) {
        List<String> signatureHeaders = new ArrayList<>();
        Arrays.stream(headers).forEach(header -> signatureHeaders.add(toSignatureFormat(header)));

        signatureHeaders.sort(String::compareTo);
        return signatureHeaders.toString();
    }

    private static String toSignatureFormat(Header header) {
        if (header == null) {
            return null;
        }

        return toSignatureFormat(header.getName(), header.getValue());
    }

    private static String toSignatureFormat(String key, String value) {
        return key + ": " + value;
    }
}
