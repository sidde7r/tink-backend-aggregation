package se.tink.backend.aggregation.nxgen.http.log.adapter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.entity.BasicHttpEntity;
import se.tink.libraries.har_logger.src.model.HarRequest;
import se.tink.libraries.har_logger.src.model.HarResponse;

public class TinkApacheHttpRequestLoggingAdapter {

    public static HarRequest mapRequest(HttpRequest request, Instant requestTime)
            throws IOException {
        return HarRequest.builder()
                .timestamp(Date.from(requestTime))
                .method(request.getRequestLine().getMethod())
                .url(mapUrl(request))
                .httpVersion(request.getRequestLine().getProtocolVersion().toString())
                .headers(mapHeaders(request.getAllHeaders()))
                .body(mapRequestBody(request))
                .build();
    }

    public static HarResponse mapResponse(HttpResponse response) throws IOException {
        return HarResponse.builder()
                .httpVersion(response.getProtocolVersion().toString())
                .statusCode(response.getStatusLine().getStatusCode())
                .statusText(response.getStatusLine().getReasonPhrase())
                .headers(mapHeaders(response.getAllHeaders()))
                .body(mapEntityBody(response.getEntity()))
                .build();
    }

    private static String mapUrl(HttpRequest request) {
        return getWrapped(request).getRequestLine().getUri();
    }

    private static Map<String, List<String>> mapHeaders(Header[] headers) {
        final Map<String, List<String>> mappedHeaders = new HashMap<>();
        for (Header header : headers) {
            final String headerName = header.getName();
            final List<String> headerValues;
            if (mappedHeaders.containsKey(header.getName())) {
                headerValues = mappedHeaders.get(header.getName());
            } else {
                headerValues = new ArrayList<>();
                mappedHeaders.put(headerName, headerValues);
            }
            headerValues.add(header.getValue());
        }
        return mappedHeaders;
    }

    private static HttpRequest getWrapped(HttpRequest request) {
        if (request instanceof HttpRequestWrapper) {
            return ((HttpRequestWrapper) request).getOriginal();
        } else {
            return request;
        }
    }

    @SuppressWarnings("java:S1168") // suppress "Return an empty array instead of null." warning
    private static byte[] mapRequestBody(HttpRequest request) throws IOException {
        HttpRequest analyzed = getWrapped(request);
        if (analyzed instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest entityEnclosing = (HttpEntityEnclosingRequest) analyzed;
            return mapEntityBody(entityEnclosing.getEntity());
        }
        return null;
    }

    @SuppressWarnings("java:S1168") // suppress "Return an empty array instead of null." warning
    private static byte[] mapEntityBody(HttpEntity entity) throws IOException {
        if (entity == null) {
            return null;
        }
        InputStream inputStream = entity.getContent();
        if (!entity.isRepeatable()) {
            inputStream = new BufferedInputStream(inputStream);
            if (entity instanceof BasicHttpEntity) {
                ((BasicHttpEntity) entity).setContent(inputStream);
            }
        }
        inputStream.mark(Integer.MAX_VALUE);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, outputStream);
        inputStream.reset();
        return outputStream.toByteArray();
    }
}
