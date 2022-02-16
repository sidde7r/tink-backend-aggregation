package se.tink.backend.aggregation.nxgen.http.log.adapter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.entity.BasicHttpEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.LoggingExecutor;

/** Adapter able to map Apache request to common model */
public class DefaultApacheRequestLoggingAdapter extends LoggingAdapter<HttpRequest, HttpResponse> {

    public DefaultApacheRequestLoggingAdapter(List<LoggingExecutor> loggingExecutors) {
        super(loggingExecutors);
    }

    @Override
    protected boolean hasRequestBody(HttpRequest request) {
        return tryGetHttpEntity(request).isPresent();
    }

    @Override
    protected InputStream convertRequestBody(HttpRequest request) throws IOException {
        HttpEntity entity =
                tryGetHttpEntity(request)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Cannot convert request without a body"));

        InputStream inputStream = entity.getContent();
        if (!entity.isRepeatable()) {
            inputStream = new BufferedInputStream(inputStream);
            if (entity instanceof BasicHttpEntity) {
                ((BasicHttpEntity) entity).setContent(inputStream);
            }
        }
        return inputStream;
    }

    private Optional<HttpEntity> tryGetHttpEntity(HttpRequest request) {
        HttpRequest unwrappedRequest = getWrapped(request);
        if (unwrappedRequest instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest entityEnclosing =
                    (HttpEntityEnclosingRequestBase) unwrappedRequest;
            return Optional.ofNullable(entityEnclosing.getEntity());
        }
        return Optional.empty();
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
