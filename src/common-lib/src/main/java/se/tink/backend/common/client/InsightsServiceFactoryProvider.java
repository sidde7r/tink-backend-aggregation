package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.annotation.Nullable;
import se.tink.backend.guice.annotations.InsightsConfiguration;
import se.tink.backend.insights.client.ClientInsightsServiceFactory;
import se.tink.backend.insights.client.InsightsServiceFactory;
import se.tink.libraries.endpoints.EndpointConfiguration;

public class InsightsServiceFactoryProvider  implements Provider<InsightsServiceFactory> {
    private final EndpointConfiguration endpoint;

    @Inject
    public InsightsServiceFactoryProvider(@Nullable @InsightsConfiguration EndpointConfiguration endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public InsightsServiceFactory get() {
        if (endpoint == null){
            return null;
        }
        return new ClientInsightsServiceFactory(endpoint.getPinnedCertificates(), endpoint.getUrl());
    }
}
