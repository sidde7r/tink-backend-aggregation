package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.common.Request;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public abstract class AbstractRequest<T> implements Request<T> {

    private String csrfToken;
    private String url;

    protected AbstractRequest(String urlPath) {
        this.url =
                new StringBuilder(BPostBankConstants.ORIGIN)
                        .append(urlPath.startsWith("/") ? "" : "/")
                        .append(urlPath)
                        .toString();
    }

    protected AbstractRequest(String urlPath, BPostBankAuthContext authContext) {
        this(urlPath);
        this.csrfToken = authContext.getCsrfToken();
    }

    @Override
    public RequestBuilder withHeaders(RequestBuilder requestBuilder) throws RequestException {
        if (csrfToken != null) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("X-Device-Type", "1");
            headers.put("Accept-Language", "nl-be");
            headers.put("lang", "nl-BE");
            headers.put(BPostBankConstants.CSRF_TOKEN_HEADER_KEY, csrfToken);
            return requestBuilder.headers(headers);
        }
        return requestBuilder;
    }

    @Override
    public RequestBuilder withUrl(TinkHttpClient httpClient) {
        return httpClient.request(url);
    }
}
