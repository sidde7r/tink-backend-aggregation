package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.mainview;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.common.ConnectivityRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurRequestHeaderFactory;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@AllArgsConstructor
public class MainViewRequest implements ConnectivityRequest<String> {

    private final URL url;

    @Override
    public RequestBuilder withHeaders(RequestBuilder requestBuilder) {
        return requestBuilder
                .headers(CajasurRequestHeaderFactory.createBasicHeaders())
                .accept("text/html, application/xhtml+xml, image/jxr, */*");
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public RequestBuilder withUrl(TinkHttpClient httpClient) {
        return httpClient.request(url);
    }

    @Override
    public String execute(RequestBuilder requestBuilder) {
        return requestBuilder.get(String.class);
    }
}
