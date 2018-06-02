package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.annotation.Nullable;
import se.tink.backend.guice.annotations.ProductExecutorEndpointConfiguration;
import se.tink.backend.product.execution.ProductExecutorServiceFactory;
import se.tink.backend.product.execution.client.ClientProductExecutorServiceFactory;
import se.tink.libraries.endpoints.EndpointConfiguration;

public class ProductExecutorServiceFactoryProvider implements Provider<ProductExecutorServiceFactory> {
    private final EndpointConfiguration endpoint;

    @Inject
    public ProductExecutorServiceFactoryProvider(@Nullable @ProductExecutorEndpointConfiguration EndpointConfiguration endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public ProductExecutorServiceFactory get() {
        if (endpoint == null){
            return null;
        }
        return new ClientProductExecutorServiceFactory(endpoint.getPinnedCertificates(), endpoint.getUrl());
    }
}
