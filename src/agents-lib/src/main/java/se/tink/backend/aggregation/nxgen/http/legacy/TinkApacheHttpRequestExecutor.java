package se.tink.backend.aggregation.nxgen.http.legacy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
import se.tink.libraries.cryptography.RSAUtils;

/*
    This HttpRequestExecutor is only necessary because of bugs in the underlying libraries (jersey and apache).
    Adding cookies to single requests will lead to multiple `Cookie` headers because apache adds cookies from
    the internal cookieStore (which is populated by Set-Cookie directives).

    The work-around is to merge all `Cookie` headers into one.

    (This class also removes the header `Cookie2` which is added by apache).
 */
public class TinkApacheHttpRequestExecutor extends HttpRequestExecutor {
    private static final Logger log = LoggerFactory.getLogger(TinkApacheHttpRequestExecutor.class);
    private static final String TEST_PRIVATE_KEY_PATH = "data/test/cryptography/private_rsa_key.pem";
    private static final String SIGNATURE_HEADER_KEY = "X-Signature";

    private final SignatureKeyPair signatureKeyPair;

    private Algorithm algorithm;

    public TinkApacheHttpRequestExecutor(SignatureKeyPair signatureKeyPair) {
        this.signatureKeyPair = signatureKeyPair;

        try {
            if (signatureKeyPair == null || signatureKeyPair.getPrivateKey() == null) {
                log.warn("Signature key path was empty, using the test key as a fallback.");
                algorithm = Algorithm.RSA256(null, RSAUtils.getPrivateKey(TEST_PRIVATE_KEY_PATH));
                return;
            }

            algorithm = Algorithm.RSA256(null, signatureKeyPair.getPrivateKey());
        } catch (Exception e) {
            log.error("No signature header will be added to requests.", e);
        }
    }

    @Override
    public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context)
            throws IOException, HttpException {
        // Remove the default "Cookie2" header that ApacheHttp adds
        request.removeHeaders("Cookie2");
        mergeCookieHeaders(request);

        addRequestSignature(request);
        return super.execute(request, conn, context);
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
        if (algorithm == null) {
            return;
        }

        // TODO: Create a webpage with info on how to verify signature.
        // This header needs to be added before we fetch the headers to create the signature.
        request.addHeader("X-Signature-Info",
                "Visit https://developers.tink.se/request-signature-verification for more info.");

        RequestLine requestLine = request.getRequestLine();
        Header[] allHeaders = request.getAllHeaders();

        JWTCreator.Builder requestSignatureHeader = JWT.create()
                .withIssuedAt(new Date())
                .withKeyId(signatureKeyPair.getKeyId())
                .withClaim("method", requestLine.getMethod())
                .withClaim("uri", requestLine.getUri())
                .withClaim("headers", toSignatureFormat(allHeaders));

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
