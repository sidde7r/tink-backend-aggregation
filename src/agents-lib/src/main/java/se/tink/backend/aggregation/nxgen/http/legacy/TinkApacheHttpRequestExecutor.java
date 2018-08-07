package se.tink.backend.aggregation.nxgen.http.legacy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
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
    private static final String PRIVATE_KEY_PATH = "data/test/cryptography/private_rsa_key.pem";

    private Algorithm algorithm;

    public TinkApacheHttpRequestExecutor() {
        try {
            algorithm = Algorithm.RSA256(null, RSAUtils.getPrivateKey(PRIVATE_KEY_PATH));
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

        RequestLine requestLine = request.getRequestLine();
        Header[] allHeaders = request.getAllHeaders();

        JWTCreator.Builder requestSignatureHeader = JWT.create()
                .withIssuedAt(new Date())
                .withClaim("method", requestLine.getMethod())
                .withClaim("uri", requestLine.getUri())
                .withClaim("headers", toSignatureFormat(allHeaders));

        try {
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                String requestBody = IOUtils.toString(entity.getContent(), "UTF-8");
                byte[] hashedRequestBody = Hash.sha256(requestBody);
                requestSignatureHeader.withClaim("body", EncodingUtils.encodeAsBase64String(hashedRequestBody));
            }
        } catch (IOException e) {
            log.error("Could not get the request body from the entity content", e);
        }

        request.addHeader("X-Aggregator-Signature", requestSignatureHeader.sign(algorithm));

        // TODO: Create a webpage with info on how to verify signature.
        // request.addHeader("X-Aggregator-Signature-Info",
        //        "Visit https://developers.tink.se/request-signature-verification for more info.");
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
