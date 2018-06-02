package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.client.ClientAggregationServiceFactory;

public class TestAggregationServiceFactoryProvider implements Provider<AggregationServiceFactory> {

    private String wireMockUrl;

    @Inject
    public TestAggregationServiceFactoryProvider(@Named("wireMockUrl") String wireMockUrl) {
        this.wireMockUrl = wireMockUrl;
    }

    @Override
    public AggregationServiceFactory get() {
        return ClientAggregationServiceFactory.buildWithoutPinning(wireMockUrl);
    }
}
