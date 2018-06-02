package se.tink.backend.main.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.categorization.client.FastTextServiceFactory;
import se.tink.backend.client.InProcessServiceFactory;
import se.tink.backend.client.ServiceFactory;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.client.AggregationServiceFactoryProvider;
import se.tink.backend.common.client.EncryptionServiceFactoryProvider;
import se.tink.backend.common.client.FastTextServiceFactoryProvider;
import se.tink.backend.common.client.GdprExportServiceFactoryProvider;
import se.tink.backend.common.client.InsightsServiceFactoryProvider;
import se.tink.backend.common.client.ProductExecutorServiceFactoryProvider;
import se.tink.backend.common.client.SystemServiceFactoryProvider;
import se.tink.backend.encryption.client.EncryptionServiceFactory;
import se.tink.backend.export.client.GdprExportServiceFactory;
import se.tink.backend.insights.client.InsightsServiceFactory;
import se.tink.backend.product.execution.ProductExecutorServiceFactory;
import se.tink.backend.system.client.SystemServiceFactory;

public class MainServiceFactoryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ServiceFactory.class).to(InProcessServiceFactory.class).in(Scopes.SINGLETON);
        bind(SystemServiceFactory.class).toProvider(SystemServiceFactoryProvider.class).in(Scopes.SINGLETON);
        bind(AggregationServiceFactory.class).toProvider(AggregationServiceFactoryProvider.class).in(Scopes.SINGLETON);
        bind(EncryptionServiceFactory.class).toProvider(EncryptionServiceFactoryProvider.class).in(Scopes.SINGLETON);
        bind(FastTextServiceFactory.class).toProvider(FastTextServiceFactoryProvider.class).in(Scopes.SINGLETON);
        bind(InsightsServiceFactory.class).toProvider(InsightsServiceFactoryProvider.class).in(Scopes.SINGLETON);
        bind(ProductExecutorServiceFactory.class).toProvider(ProductExecutorServiceFactoryProvider.class).in(Scopes.SINGLETON);
        bind(GdprExportServiceFactory.class).toProvider(GdprExportServiceFactoryProvider.class).in(Scopes.SINGLETON);
        bind(AggregationControllerCommonClient.class).in(Scopes.SINGLETON);
    }
}
