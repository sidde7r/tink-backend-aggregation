package se.tink.backend.aggregation.nxgen.http.filter;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class LogResponseFilter extends Filter {
    protected static final LogTag logTag = LogTag.from("#http_response_log_tag");
    protected static final AggregationLogger log = new AggregationLogger(LogResponseFilter.class);

    protected final Class responseModel;

    public LogResponseFilter(Class responseModel) {
        this.responseModel = responseModel;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        log.infoExtraLong(
                String.format(
                        "Response (%s): %s",
                        responseModel.getSimpleName(), response.getBody(String.class)),
                logTag);
        return response;
    }
}
