package se.tink.backend.aggregation.nxgen.http.legacy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.entity.BasicHttpEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.LoggingAdapter;
import se.tink.backend.aggregation.nxgen.http.log.executor.LoggingExecutor;

public class DefaultRequestLoggingAdapter extends LoggingAdapter<HttpRequest, HttpResponse> {

    public DefaultRequestLoggingAdapter(LoggingExecutor loggingExecutor) {
        super(loggingExecutor);
    }

    @Override
    protected boolean hasRequestBody(HttpRequest request) {
        return getWrapped(request) instanceof HttpEntityEnclosingRequest;
    }

    @Override
    protected InputStream convertRequest(HttpRequest request) throws IOException {
        HttpRequest analyzed = getWrapped(request);
        if (analyzed instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest entityEnclosing = (HttpEntityEnclosingRequestBase) analyzed;

            HttpEntity entity = entityEnclosing.getEntity();
            if (entity != null) {
                InputStream inputStream = entityEnclosing.getEntity().getContent();
                if (!entity.isRepeatable()) {
                    inputStream = new BufferedInputStream(inputStream);
                    if (entity instanceof BasicHttpEntity) {
                        ((BasicHttpEntity) entity).setContent(inputStream);
                    }
                }
                return inputStream;
            }
        }
        throw new IllegalArgumentException("Invalid request to convert");
    }

    @Override
    protected Map<String, String> mapRequestHeaders(HttpRequest request) {
        return Arrays.stream(request.getAllHeaders())
                .collect(Collectors.toMap(Header::getName, Header::getValue));
    }

    @Override
    protected String mapMethod(HttpRequest request) {
        return getWrapped(request).getRequestLine().getMethod();
    }

    @Override
    protected String mapUrl(HttpRequest request) {
        return getWrapped(request).getRequestLine().getUri();
    }

    private HttpRequest getWrapped(HttpRequest request) {
        if (request instanceof HttpRequestWrapper) {
            return ((HttpRequestWrapper) request).getOriginal();
        } else {
            return request;
        }
    }
}
