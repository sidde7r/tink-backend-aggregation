package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.sun.jersey.api.client.Client;
import org.joda.time.LocalDate;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.CreateProductExecutor;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.mapping.ApplicationToLoanPostRequestMapper;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.mapping.ApplicationToLoanPostRequestMapperImpl;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.agents.utils.jersey.JerseyClientFactory;
import se.tink.backend.aggregation.annotations.CurrentDate;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.utils.RateLimitedCountdown;
import se.tink.backend.aggregation.utils.RateLimitedCountdownImpl;
import se.tink.backend.common.config.SEBMortgageIntegrationConfiguration;
import se.tink.libraries.metrics.MetricRegistry;

public class SEBCreatableProductModule extends AbstractModule {
    private final AgentContext context;
    private final Credentials credentials;
    private final SEBMortgageApiConfiguration mortgageApiConfiguration;
    private final Client cookieClientWithoutSSL;

    public SEBCreatableProductModule(AgentContext context, Credentials credentials) {
        this.context = context;
        this.credentials = credentials;
        this.mortgageApiConfiguration = new SEBMortgageApiConfiguration();

        // TODO: Create certificate on our VPN end to terminate real connection
        cookieClientWithoutSSL = new JerseyClientFactory().createCookieClientWithoutSSL();
    }

    /**
     * To be able to read in configuration not coming from constructor, we need to have a custom way of doing it.
     * This is e.g. required for the HttpClient being used, so we don't want to pass it down as method argument
     * in the call chain, but rather instantiate with it.
     */
    public void setMortgageConfiguration(SEBMortgageIntegrationConfiguration mortgageConfiguration) {
        this.mortgageApiConfiguration.setConfiguration(mortgageConfiguration);
    }

    @Override
    protected void configure() {
        // Instance bindings
        bind(AgentContext.class).toInstance(context);
        bind(Credentials.class).toInstance(credentials);
        bind(Client.class).toInstance(cookieClientWithoutSSL);
        bind(MetricRegistry.class).toInstance(context.getMetricRegistry());
        bind(ApiConfiguration.class).toInstance(mortgageApiConfiguration);

        // Countdown instances when injected for bank id looping
        bind(RateLimitedCountdown.class)
                .annotatedWith(Names.named("SEBMortgage.bankIdCollectCountdown"))
                .to(RateLimitedCountdownImpl.class);

        // Regular inteface -> impl bindings
        bind(CreateProductExecutor.class).to(SEBCreateProductExecutor.class);
        bind(SEBMortgageApiClient.class).to(SEBMortgageApiClientImpl.class);
        bind(SEBMortgageBankIdCollector.class).to(SEBMortgageBankIdCollectorImpl.class);
        bind(HttpClient.class).to(JerseyClientWrapper.class);
        bind(ApplicationToLoanPostRequestMapper.class).to(ApplicationToLoanPostRequestMapperImpl.class);
        bind(ProductInformationGetRatesMapper.class).to(ProductInformationGetRatesMapperImpl.class);
    }

    @Provides
    private AggregationLogger provideAggregationLogger() {
        return new AggregationLogger(SEBCreatableProductModule.class);
    }

    @Provides
    @CurrentDate
    private LocalDate provideCurrentLocalDate() {
        return new LocalDate();
    }

    /**
     * Countdown configuration (60 * 0.5 reqs/sec -> min 120 seconds until countdown expiry)
     */
    @Provides
    @Named("RateLimitedCountdownImpl.rateLimiter")
    private RateLimiter provideCountdownRateLimiter() {
        return RateLimiter.create(0.5);
    }

    @Provides
    @Named("RateLimitedCountdownImpl.counts")
    private Integer provideCoundownCounts() {
        return 60;
    }
}
