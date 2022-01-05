package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.common.ConnectivityRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurRequestHeaderFactory;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@AllArgsConstructor
public class ObfuscatedLoginJavaScriptFetchRequest implements ConnectivityRequest<String> {

    private static final String URL_PATH = "/internetcs/js/login/encriptarLoginOfuscado.js";

    private final String authUrlDomain;

    @Override
    public RequestBuilder withHeaders(RequestBuilder requestBuilder) {
        return requestBuilder
                .headers(CajasurRequestHeaderFactory.createBasicHeaders())
                .accept("text/html", "application/xhtml+xml", "image/jxr", "*/*");
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public RequestBuilder withUrl(TinkHttpClient httpClient) {
        return httpClient.request(new URL(authUrlDomain + URL_PATH));
    }

    @Override
    public String execute(RequestBuilder requestBuilder) {
        return requestBuilder.get(String.class);
    }
}
