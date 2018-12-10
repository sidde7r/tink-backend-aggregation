package se.tink.backend.aggregation.nxgen.http.metrics;

import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.libraries.metrics.utils.MetricsUtils;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

/*
 * Measure round trip time and response status of each request.
 */
public class MetricFilter extends Filter {

    private final static MetricId METRIC_ID = MetricId.newId("http_client");
    private final MetricRegistry registry;
    private final Provider provider;

    public MetricFilter(AgentWorkerContext context) {
        registry = context.getMetricRegistry();
        provider = context.getRequest().getProvider();
    }

    private MetricId populateMetric(MetricId metric, HttpResponse response) {
        return metric.label("provider", MetricsUtils.cleanMetricName(provider.getName()))
                    .label("agent", provider.getClassName())
                    .label("status", Integer.toString(response.getStatus()));
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        long startTime = System.nanoTime();
        HttpResponse httpResponse = nextFilter(httpRequest);
        long elapsedTime = System.nanoTime() - startTime;
        registry.timer(populateMetric(METRIC_ID, httpResponse)).update(elapsedTime, TimeUnit.NANOSECONDS);
        return httpResponse;
    }
}
