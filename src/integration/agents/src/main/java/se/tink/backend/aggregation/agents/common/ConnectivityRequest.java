package se.tink.backend.aggregation.agents.common;

import se.tink.backend.aggregation.nxgen.http.CookieRepository;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public interface ConnectivityRequest<RESPONSE> {

    RequestBuilder withHeaders(final RequestBuilder requestBuilder);

    RequestBuilder withBody(final RequestBuilder requestBuilder);

    RequestBuilder withUrl(final TinkHttpClient httpClient);

    RESPONSE execute(final RequestBuilder requestBuilder);

    default RESPONSE call(final TinkHttpClient httpClient, final SessionStorage sessionStorage) {
        RequestBuilder requestBuilder = withBody(withHeaders(withUrl(httpClient)));
        CookieRepository.getInstance(sessionStorage).getCookies().forEach(requestBuilder::cookie);
        return execute(requestBuilder);
    }
}
