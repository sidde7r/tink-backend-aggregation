package se.tink.backend.aggregation.nxgen.http.filter.filters.executiontime;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.executiontime.TimeMeasuredRequestExecutor.ExecutionDetails;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@FilterOrder(category = FilterPhases.SEND, order = 0)
public class ExecutionTimeLoggingFilter extends Filter {

    private final Logger log = LoggerFactory.getLogger(ExecutionTimeLoggingFilter.class);

    private final Function<HttpRequest, TimeMeasuredRequestExecutor> measureRequestTimeExecution;

    public ExecutionTimeLoggingFilter(
            Function<HttpRequest, TimeMeasuredRequestExecutor> measureRequestTimeExecution) {
        this.measureRequestTimeExecution = measureRequestTimeExecution;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        ExecutionDetails executionDetails =
                measureRequestTimeExecution
                        .andThen(executionTimeLogger -> executionTimeLogger.execute(this))
                        .apply(httpRequest);
        if (executionDetails.shouldBeLogged()) {
            log.info(
                    "Request with url `{}` has exceeded the time of execution [{}ms execution time | {}ms threshold].",
                    httpRequest.getUrl(),
                    executionDetails.getExecutionTime(),
                    executionDetails.getThreshold());
        }
        return executionDetails.getResponse();
    }
}
