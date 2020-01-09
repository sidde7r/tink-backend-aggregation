package se.tink.backend.aggregation.nxgen.http.filter.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@FilterOrder(category = FilterPhases.SEND, order = 0)
public class ExecutionTimeLoggingFilter extends Filter {

    private final Logger log = LoggerFactory.getLogger(ExecutionTimeLoggingFilter.class);

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        long start = System.currentTimeMillis();
        HttpResponse response = getNext().handle(httpRequest);
        long end = System.currentTimeMillis();
        log.info("Service {} was executed in {} milliseconds.", httpRequest.getUrl(), end - start);
        return response;
    }
}
