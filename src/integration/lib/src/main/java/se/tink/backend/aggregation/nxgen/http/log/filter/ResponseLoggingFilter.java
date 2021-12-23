package se.tink.backend.aggregation.nxgen.http.log.filter;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.log.adapter.DefaultJerseyResponseLoggingAdapter;

@RequiredArgsConstructor
public class ResponseLoggingFilter extends ClientFilter {

    private final DefaultJerseyResponseLoggingAdapter responseLoggingAdapter;

    @Override
    public ClientResponse handle(ClientRequest cr) {
        ClientResponse response = getNext().handle(cr);
        responseLoggingAdapter.logResponse(response);
        return response;
    }
}
