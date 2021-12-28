package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.tink.backend.aggregation.agents.common.ConnectivityRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurRequestHeaderFactory;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SegmentIdFetchRequest implements ConnectivityRequest<String> {

    private static final String URL_PATH = "/cs/Satellite/cajasur/es/particulares-0";

    private final String authUrlDomain;

    public SegmentIdFetchRequest(String authUrlDomain) {
        this.authUrlDomain = authUrlDomain;
    }

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
        return httpClient.request(new URL(String.format("%s%s", authUrlDomain, URL_PATH)));
    }

    @Override
    public String execute(RequestBuilder requestBuilder) {
        String body = requestBuilder.get(String.class);
        Document componentDoc = Jsoup.parse(body);
        return componentDoc.getElementById("idSegmento").val();
    }
}
