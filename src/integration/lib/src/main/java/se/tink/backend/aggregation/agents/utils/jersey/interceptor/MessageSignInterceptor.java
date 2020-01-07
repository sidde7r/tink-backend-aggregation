package se.tink.backend.aggregation.agents.utils.jersey.interceptor;

import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@FilterOrder(category = FilterPhases.SECURITY, order = 0)
public abstract class MessageSignInterceptor extends Filter {

    public HttpResponse handle(HttpRequest request)
            throws HttpClientException, HttpResponseException {
        appendAdditionalHeaders(request);
        prepareDigestAndAddAsHeader(request);
        getSignatureAndAddAsHeader(request);
        reorganiseHeaders(request);
        return nextFilter(request);
    }

    protected abstract void appendAdditionalHeaders(HttpRequest request);

    protected abstract void getSignatureAndAddAsHeader(HttpRequest request);

    protected abstract void prepareDigestAndAddAsHeader(HttpRequest request);

    protected void reorganiseHeaders(HttpRequest request) {}
}
