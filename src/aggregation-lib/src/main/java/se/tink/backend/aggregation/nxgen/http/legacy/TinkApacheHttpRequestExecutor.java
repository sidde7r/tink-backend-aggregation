package se.tink.backend.aggregation.nxgen.http.legacy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;

/*
    This HttpRequestExecutor is only necessary because of bugs in the underlying libraries (jersey and apache).
    Adding cookies to single requests will lead to multiple `Cookie` headers because apache adds cookies from
    the internal cookieStore (which is populated by Set-Cookie directives).

    The work-around is to merge all `Cookie` headers into one.

    (This class also removes the header `Cookie2` which is added by apache).
 */
public class TinkApacheHttpRequestExecutor extends HttpRequestExecutor {

    @Override
    public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context)
            throws IOException, HttpException {
        // Remove the default "Cookie2" header that ApacheHttp adds
        request.removeHeaders("Cookie2");

        mergeCookieHeaders(request);
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
}
