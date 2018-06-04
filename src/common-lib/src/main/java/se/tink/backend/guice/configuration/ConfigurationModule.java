package se.tink.backend.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.config.AnalyticsConfiguration;
import se.tink.backend.common.config.BackOfficeConfiguration;
import se.tink.backend.common.config.CacheConfiguration;
import se.tink.backend.common.config.FacebookConfiguration;
import se.tink.backend.common.config.FirehoseConfiguration;
import se.tink.backend.common.config.FlagsConfiguration;
import se.tink.backend.common.config.GrpcConfiguration;
import se.tink.backend.common.config.IDControlConfiguration;
import se.tink.backend.common.config.IntercomConfiguration;
import se.tink.backend.common.config.NotificationsConfiguration;
import se.tink.backend.common.config.SchedulerConfiguration;
import se.tink.backend.common.config.SearchConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.config.StatisticConfiguration;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.common.config.TemplateConfiguration;
import se.tink.backend.common.config.TransfersConfiguration;
import se.tink.backend.guice.annotations.AggregationConfiguration;
import se.tink.backend.guice.annotations.AggregationControllerConfiguration;
import se.tink.backend.guice.annotations.ConnectorConfiguration;
import se.tink.backend.guice.annotations.EncryptionConfiguration;
import se.tink.backend.guice.annotations.ExportUserDataConfiguration;
import se.tink.backend.guice.annotations.FastTextConfiguration;
import se.tink.backend.guice.annotations.InsightsConfiguration;
import se.tink.backend.guice.annotations.MainConfiguration;
import se.tink.backend.guice.annotations.OAuthConfiguration;
import se.tink.backend.guice.annotations.ProductExecutorEndpointConfiguration;
import se.tink.backend.guice.annotations.SystemConfiguration;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.discovery.CoordinationConfiguration;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.endpoints.EndpointsConfiguration;
import se.tink.libraries.metrics.PrometheusConfiguration;

public class ConfigurationModule extends AbstractModule {

    private final ServiceConfiguration configuration;

    public ConfigurationModule(ServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(EndpointConfiguration.class).annotatedWith(ConnectorConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getConnector()));
        bind(EndpointConfiguration.class).annotatedWith(SystemConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getSystem()));
        bind(EndpointConfiguration.class).annotatedWith(MainConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getMain()));
        bind(EndpointConfiguration.class).annotatedWith(AggregationConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getAggregation()));
        bind(EndpointConfiguration.class).annotatedWith(InsightsConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getInsights()));
        bind(EndpointConfiguration.class).annotatedWith(ExportUserDataConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getDataExport()));
        bind(EndpointConfiguration.class).annotatedWith(EncryptionConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getEncryption()));
        bind(EndpointConfiguration.class).annotatedWith(FastTextConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getFasttext()));
        bind(EndpointConfiguration.class).annotatedWith(Names.named("CategorizerConfiguration"))
                .toProvider(Providers.of(configuration.getEndpoints().getCategorization()));
        bind(EndpointConfiguration.class).annotatedWith(OAuthConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getOauth()));
        bind(EndpointConfiguration.class).annotatedWith(AggregationControllerConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getAggregationcontroller()));
        bind(EndpointConfiguration.class).annotatedWith(ProductExecutorEndpointConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getExecutor()));

        bind(EndpointsConfiguration.class).toProvider(Providers.of(configuration.getEndpoints()));

        bindConstant().annotatedWith(Names.named("deepLinkPrefix"))
                .to(configuration.getNotifications().getDeepLinkPrefix());
        bindConstant().annotatedWith(Names.named("developmentMode")).to(configuration.isDevelopmentMode());
        bindConstant().annotatedWith(Names.named("productionMode")).to(!configuration.isDevelopmentMode());
        bindConstant().annotatedWith(Names.named("isSupplementalOnAggregation"))
                .to(configuration.isSupplementalOnAggregation());
        bindConstant().annotatedWith(Names.named("useAggregationController"))
                .to(configuration.isUseAggregationController());
        bindConstant().annotatedWith(Names.named("isAggregationCluster"))
                .to(configuration.isAggregationCluster());
        bindConstant().annotatedWith(Names.named("isProvidersOnAggregation"))
                .to(configuration.isProvidersOnAggregation());

        // Tink monolith (common-lib and main-api) configurations
        bind(AbnAmroConfiguration.class).toProvider(Providers.of(configuration.getAbnAmro()));
        bind(AnalyticsConfiguration.class).toProvider(Providers.of(configuration.getAnalytics()));
        bind(CacheConfiguration.class).toProvider(Providers.of(configuration.getCacheConfiguration()));
        bind(Cluster.class).toInstance(configuration.getCluster());
        bind(se.tink.backend.common.config.ConnectorConfiguration.class)
                .toProvider(Providers.of(configuration.getConnector()));
        bind(NotificationsConfiguration.class).toInstance(configuration.getNotifications());
        bind(ProviderCacheConfiguration.class).toInstance(new ProviderCacheConfiguration(5, TimeUnit.MINUTES));
        bind(SearchConfiguration.class).toProvider(Providers.of(configuration.getSearchConfiguration()));
        bind(ServiceConfiguration.class).toInstance(configuration);
        bind(TasksQueueConfiguration.class).toProvider(Providers.of(configuration.getTaskQueue()));
        bind(TransfersConfiguration.class).toProvider(Providers.of(configuration.getTransfers()));
        bind(SchedulerConfiguration.class).toProvider(Providers.of(configuration.getScheduler()));
        bind(FacebookConfiguration.class).toInstance(configuration.getFacebook());
        bind(TemplateConfiguration.class).toInstance(configuration.getTemplate());
        bind(IDControlConfiguration.class).toInstance(configuration.getIdControl());
        bind(FlagsConfiguration.class).toInstance(configuration.getFlags());
        bind(BackOfficeConfiguration.class).toInstance(configuration.getBackOffice());
        bind(StatisticConfiguration.class).toInstance(configuration.getStatistics());
        bind(GrpcConfiguration.class).toInstance(configuration.getGrpc());
        bind(FirehoseConfiguration.class).toInstance(configuration.getFirehose());
        // Tink public library configurations
        bind(CoordinationConfiguration.class).toProvider(Providers.of(configuration.getCoordination()));
        bind(PrometheusConfiguration.class).toInstance(configuration.getPrometheus());
        bind(IntercomConfiguration.class).toInstance(configuration.getAnalytics().getIntercom());
    }

}
