package se.tink.backend.aggregation.nxgen.http.metrics;

import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

/*
 * Measure round trip time and response status of each request.
 */
@FilterOrder(category = FilterPhases.SEND, order = 0)
public class MetricFilter extends Filter {

    private static final MetricId METRIC_ID = MetricId.newId("http_client");
    private final MetricRegistry registry;
    private final Provider provider;

    public MetricFilter(MetricRegistry registry, Provider provider) {
        this.registry = registry;
        this.provider = provider;
    }

    private MetricId populateMetric(MetricId metric, HttpResponse response) {
        return metric.label("provider", provider.getName())
                .label("agent", provider.getClassName())
                .label("provider_type", provider.getMetricTypeName())
                .label("market", provider.getMarket())
                .label("status", Integer.toString(response.getStatus()))
                .label("access_type", provider.getAccessType().toString());
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
