package se.tink.backend.aggregation.nxgen.http.log.adapter;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.http.log.executor.LoggingExecutor;

/** Adapter able to map Jersey response to common model */
public class DefaultJerseyResponseLoggingAdapter
        extends LoggingAdapter<ClientRequest, ClientResponse> {

    public DefaultJerseyResponseLoggingAdapter(LoggingExecutor loggingExecutor) {
        super(loggingExecutor);
    }

    @Override
    protected int mapStatus(ClientResponse response) {
        return response.getStatus();
    }

    @Override
    protected Map<String, String> mapResponseHeaders(ClientResponse response) {
        return response.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.join(",", e.getValue())));
    }

    @Override
    protected InputStream convertResponse(ClientResponse response) {
        InputStream stream = response.getEntityInputStream();

        if (!response.getEntityInputStream().markSupported()) {
            stream = new BufferedInputStream(stream);
            response.setEntityInputStream(stream);
        }
        return stream;
    }
}
