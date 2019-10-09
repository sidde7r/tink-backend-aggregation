package se.tink.backend.aggregation.agents.utils.jersey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

@FilterOrder(category = FilterPhases.SECURITY, order = 0)
public abstract class MessageSignInterceptor extends Filter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MessageSignInterceptor.class.getName());

    public HttpResponse handle(HttpRequest request)
            throws HttpClientException, HttpResponseException {
        appendAdditionalHeaders(request);
        prepareDigestAndAddAsHeader(request);
        getSignatureAndAddAsHeader(request);
        reorganiseHeaders(request);
        HttpResponse response = nextFilter(request);
        return response;
    }

    protected abstract void appendAdditionalHeaders(HttpRequest request);

    protected abstract void getSignatureAndAddAsHeader(HttpRequest request);

    protected abstract void prepareDigestAndAddAsHeader(HttpRequest request);

    protected void reorganiseHeaders(HttpRequest request) {}
}
