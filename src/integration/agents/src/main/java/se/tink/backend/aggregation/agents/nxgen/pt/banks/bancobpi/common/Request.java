package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public interface Request<RESPONSE> {

    RequestBuilder withHeaders(final TinkHttpClient httpClient, final RequestBuilder requestBuilder)
            throws LoginException;

    RequestBuilder withBody(final TinkHttpClient httpClient, final RequestBuilder requestBuilder);

    RequestBuilder withUrl(final TinkHttpClient httpClient);

    RESPONSE execute(final RequestBuilder requestBuilder, final TinkHttpClient httpClient)
            throws LoginException;

    default RESPONSE call(final TinkHttpClient httpClient) throws LoginException {
        return execute(
                withBody(httpClient, withHeaders(httpClient, withUrl(httpClient))), httpClient);
    }
}
