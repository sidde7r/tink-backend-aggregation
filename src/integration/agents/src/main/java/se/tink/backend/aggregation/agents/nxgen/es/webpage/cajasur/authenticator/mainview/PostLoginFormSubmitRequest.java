package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.mainview;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpHeaders;
import se.tink.backend.aggregation.agents.common.ConnectivityRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurRequestHeaderFactory;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurSessionState;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class PostLoginFormSubmitRequest implements ConnectivityRequest<URL> {

    private final String authUrlDomain;
    private final String urlPath;
    private final Map<String, String> formParams;

    public PostLoginFormSubmitRequest(String authUrlDomain, CajasurSessionState sessionState) {
        this.authUrlDomain = authUrlDomain;
        Element form =
                Jsoup.parse(sessionState.getLoginResponse())
                        .getElementsByAttribute("action")
                        .first();
        urlPath = parseUrlPath(form);
        formParams = parseFormParams(form);
    }

    @Override
    public RequestBuilder withHeaders(RequestBuilder requestBuilder) {
        return requestBuilder
                .headers(CajasurRequestHeaderFactory.createBasicHeaders())
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .accept("text/html, application/xhtml+xml, image/jxr, */*");
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.join(
                        "&",
                        formParams.entrySet().stream()
                                .map(entry -> entry.getKey() + "=" + entry.getValue())
                                .collect(Collectors.toList())));
    }

    @Override
    public RequestBuilder withUrl(TinkHttpClient httpClient) {
        return httpClient.request(new URL(authUrlDomain + urlPath));
    }

    @Override
    public URL execute(RequestBuilder requestBuilder) {
        Document document = Jsoup.parse(requestBuilder.post(String.class));
        String contentAttribute = document.getElementsByTag("meta").attr("content");
        String redirectUrl = contentAttribute.substring(contentAttribute.lastIndexOf("url=") + 4);
        if (!redirectUrl.contains("http")) {
            redirectUrl = authUrlDomain + redirectUrl;
        }
        return new URL(redirectUrl);
    }

    private String parseUrlPath(Element form) {
        return form.attr("action");
    }

    private Map<String, String> parseFormParams(Element form) {
        Map<String, String> params = new LinkedHashMap<>();
        params.putAll(FormElementsExtractor.extractInputElements(form));
        params.putAll(FormElementsExtractor.extractSelectElements(form));
        return params;
    }
}
