package se.tink.backend.product.execution.integration.configurations;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import se.tink.backend.common.client.SystemServiceFactoryProvider;
import se.tink.backend.product.execution.api.MonitoringService;
import se.tink.backend.product.execution.api.SBABProductExecutorService;
import se.tink.backend.product.execution.api.SEBProductExecutorService;
import se.tink.backend.product.execution.resources.MonitoringResource;
import se.tink.backend.product.execution.resources.SBABProductExecutorResource;
import se.tink.backend.product.execution.resources.SEBProductExecutorResource;
import se.tink.backend.product.execution.unit.agents.CreateProductExecutor;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.HttpClient;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.JerseyClientWrapper;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.ProductInformationGetRatesMapper;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.ProductInformationGetRatesMapperImpl;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.SEBCreateProductExecutor;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.SEBMortgageApiClient;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.SEBMortgageApiClientImpl;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.SEBMortgageBankIdCollector;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.SEBMortgageBankIdCollectorImpl;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.mapping.ApplicationToLoanPostRequestMapper;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.mapping.ApplicationToLoanPostRequestMapperImpl;
import se.tink.backend.product.execution.utils.RateLimitedCountdown;
import se.tink.backend.product.execution.utils.RateLimitedCountdownImpl;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.libraries.metrics.MetricCollector;
import se.tink.libraries.metrics.MetricRegistry;

public class TestServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SEBProductExecutorService.class).to(SEBProductExecutorResource.class);
        bind(SBABProductExecutorService.class).to(SBABProductExecutorResource.class);
        bind(MonitoringService.class).to(MonitoringResource.class);

        bind(SystemServiceFactory.class).toProvider(SystemServiceFactoryProvider.class).in(Scopes.SINGLETON);

        // Countdown instances when injected for bank id looping
        bind(RateLimitedCountdown.class)
                .annotatedWith(Names.named("SEBMortgage.bankIdCollectCountdown"))
                .to(RateLimitedCountdownImpl.class);

        // Regular interface -> impl bindings
        bind(HttpClient.class).to(JerseyClientWrapper.class);
        bind(CreateProductExecutor.class).to(SEBCreateProductExecutor.class);
        bind(SEBMortgageApiClient.class).to(SEBMortgageApiClientImpl.class);
        bind(SEBMortgageBankIdCollector.class).to(SEBMortgageBankIdCollectorImpl.class);
        bind(ApplicationToLoanPostRequestMapper.class).to(ApplicationToLoanPostRequestMapperImpl.class);
        bind(ProductInformationGetRatesMapper.class).to(ProductInformationGetRatesMapperImpl.class);

        bind(MetricCollector.class).in(Scopes.SINGLETON);
        bind(MetricRegistry.class).in(Scopes.SINGLETON);
    }
}
