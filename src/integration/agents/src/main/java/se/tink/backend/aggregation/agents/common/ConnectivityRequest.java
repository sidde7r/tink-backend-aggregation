package se.tink.backend.aggregation.agents.common;

import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public interface ConnectivityRequest<RESPONSE> {

    RequestBuilder withHeaders(final RequestBuilder requestBuilder);

    RequestBuilder withBody(final RequestBuilder requestBuilder);

    RequestBuilder withUrl(final TinkHttpClient httpClient);

    RESPONSE execute(final RequestBuilder requestBuilder);

    default RESPONSE call(final TinkHttpClient httpClient) {
        return execute(withBody(withHeaders(withUrl(httpClient))));
    }
}
