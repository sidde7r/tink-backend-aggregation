package se.tink.backend.aggregation.agents.utils.jersey;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.http.log.executor.LoggingAdapter;
import se.tink.backend.aggregation.nxgen.http.log.executor.LoggingExecutor;

public class ResponseLoggingFilter extends ClientFilter {

    private final ResponseLoggingAdapter loggingAdapter;

    public ResponseLoggingFilter(LoggingExecutor sharedExecutor) {
        this.loggingAdapter = new ResponseLoggingAdapter(sharedExecutor);
    }

    @Override
    public ClientResponse handle(ClientRequest cr) {
        ClientResponse response = getNext().handle(cr);
        loggingAdapter.logResponse(response);
        return response;
    }

    static class ResponseLoggingAdapter extends LoggingAdapter<ClientRequest, ClientResponse> {

        public ResponseLoggingAdapter(LoggingExecutor loggingExecutor) {
            super(loggingExecutor);
        }

        @Override
        protected int mapStatus(ClientResponse response) {
            return response.getStatus();
        }

        @Override
        protected Map<String, String> mapResponseHeaders(ClientResponse response) {
            return response.getHeaders().entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, e -> String.join(",", e.getValue())));
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
}
