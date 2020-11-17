package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.http.log.executor.LoggingAdapter;
import se.tink.backend.aggregation.nxgen.http.log.executor.LoggingExecutor;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class IngLoggingAdapter extends LoggingAdapter<HttpRequest, HttpResponse> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public IngLoggingAdapter(LoggingExecutor loggingExecutor) {
        super(loggingExecutor);
    }

    @Override
    protected String mapMethod(HttpRequest request) {
        return request.getMethod().name();
    }

    @Override
    protected String mapUrl(HttpRequest request) {
        return request.getUrl().toString();
    }

    @Override
    protected Map<String, String> mapRequestHeaders(HttpRequest request) {
        return request.getHeaders().entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Entry::getKey,
                                e ->
                                        e.getValue().stream()
                                                .map(Object::toString)
                                                .collect(Collectors.joining(","))));
    }

    @Override
    protected int mapStatus(HttpResponse response) {
        return response.getStatus();
    }

    @Override
    protected Map<String, String> mapResponseHeaders(HttpResponse response) {
        return response.getHeaders().entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Entry::getKey,
                                e ->
                                        e.getValue().stream()
                                                .map(Object::toString)
                                                .collect(Collectors.joining(","))));
    }

    @Override
    protected boolean hasRequestBody(HttpRequest request) {
        return request.getBody() != null;
    }

    @Override
    protected String mapRequestBody(HttpRequest request) {
        if (request.getBody() instanceof String) {
            return (String) request.getBody();
        }
        try {
            return objectMapper.writeValueAsString(request.getBody());
        } catch (JsonProcessingException ex) {
            return "error";
        }
    }

    @Override
    protected InputStream convertResponse(HttpResponse response) {
        return response.getBodyInputStream();
    }
}
