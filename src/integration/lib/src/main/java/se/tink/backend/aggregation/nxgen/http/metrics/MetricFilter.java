package se.tink.backend.aggregation.nxgen.http.metrics;

import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.utils.MetricsUtils;

/*
 * Measure round trip time and response status of each request.
 */
public class MetricFilter extends Filter {

    private static final MetricId METRIC_ID = MetricId.newId("http_client");
    private final MetricRegistry registry;
    private final Provider provider;

    public MetricFilter(MetricRegistry registry, Provider provider) {
        this.registry = registry;
        this.provider = provider;
    }

    private MetricId populateMetric(MetricId metric, HttpResponse response) {
        return metric.label("provider", MetricsUtils.cleanMetricName(provider.getName()))
                .label("agent", provider.getClassName())
                .label("status", Integer.toString(response.getStatus()));
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        long startTime = System.nanoTime();
        HttpResponse httpResponse = nextFilter(httpRequest);
        long elapsedTime = System.nanoTime() - startTime;
        registry.timer(populateMetric(METRIC_ID, httpResponse))
                .update(elapsedTime, TimeUnit.NANOSECONDS);
        return httpResponse;
    }
}
