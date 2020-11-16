package se.tink.backend.aggregation.nxgen.http.log.executor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;

/**
 * An adapter class that is used to be a bridge between logging executor and any model. Perform the
 * mapping in that adapter
 *
 * @param <T> Request entity
 * @param <S> Response entity
 */
public abstract class LoggingAdapter<T, S> {

    private static final int MAX_SIZE = 500 * 1024;
    private static final String NOT_IMPLEMENTED = "Not implemented";

    private final LoggingExecutor loggingExecutor;

    public LoggingAdapter(LoggingExecutor loggingExecutor) {
        this.loggingExecutor = loggingExecutor;
    }

    public void logRequest(T request) {
        loggingExecutor.log(
                RequestLogEntry.builder()
                        .method(mapMethod(request))
                        .url(mapUrl(request))
                        .headers(mapRequestHeaders(request))
                        .body(mapRequestBody(request))
                        .build());
    }

    public void logResponse(S response) {
        loggingExecutor.log(
                ResponseLogEntry.builder()
                        .status(mapStatus(response))
                        .headers(mapResponseHeaders(response))
                        .body(mapResponseBody(response))
                        .build());
    }

    /**
     * Override to provide a method the request is using
     *
     * @param request a request to map from
     * @return a http method like GET or POST
     */
    protected String mapMethod(T request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * Override to provide an URL the request was calling
     *
     * @param request a request to map from
     * @return a http URL with host like http://localhost/something?query=true
     */
    protected String mapUrl(T request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * Override to provide request headers
     *
     * @param request a request to map from
     * @return a Map of headers
     */
    protected Map<String, String> mapRequestHeaders(T request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * Override to map request body to String directly without the use of InputStream
     *
     * @param request a request to map from
     * @return a String body
     */
    protected String mapRequestBody(T request) {
        try {
            if (hasRequestBody(request)) {
                return parseInputStream(convertRequest(request));
            } else {
                return null;
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot parse request body");
        }
    }

    /**
     * Override to provide response status
     *
     * @param response a response to map from
     * @return a http status like 200 or 503
     */
    protected int mapStatus(S response) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * Override to provide response headers
     *
     * @param response a response to map from
     * @return a Map of headers
     */
    protected Map<String, String> mapResponseHeaders(S response) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * Override to map response body to String directly without the use of InputStream
     *
     * @param response a response to map from
     * @return a String body
     */
    protected String mapResponseBody(S response) {
        try {
            return parseInputStream(convertResponse(response));
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot parse request body");
        }
    }

    /**
     * Override this method if you want to control if the body is there
     *
     * @param request a request to check if body is contained
     * @return true if the request contains body, false otherwise
     */
    protected boolean hasRequestBody(T request) {
        return true;
    }

    /**
     * Override this method to convert your request to an InputStream containing body If there is no
     * body return null
     *
     * @param request a request to convert
     * @return InputStream or null if no body available
     * @throws IOException in case of error during InputStream creation
     */
    protected InputStream convertRequest(T request) throws IOException {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * Override this method to convert your response to an InputStream containing body If there is
     * no body return null
     *
     * @param response a response to convert
     * @return InputStream or null if no body available
     * @throws IOException in case of error during InputStream creation
     */
    protected InputStream convertResponse(S response) throws IOException {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    private String parseInputStream(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try (StringBuilderWriter sw = new StringBuilderWriter()) {
            inputStream.mark(Integer.MAX_VALUE);

            InputStreamReader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            long charsCopied = IOUtils.copyLarge(in, sw, 0, MAX_SIZE);
            if (charsCopied == MAX_SIZE) {
                sw.write(" ... more ...");
            }

            return sw.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Could read input stream", ex);
        } finally {
            try {
                inputStream.reset();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
}
