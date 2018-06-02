package se.tink.backend.main.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.maxmind.geoip2.DatabaseReader;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import se.tink.backend.api.AccountService;
import se.tink.backend.api.ActivityService;
import se.tink.backend.api.ApplicationService;
import se.tink.backend.api.AuthenticationService;
import se.tink.backend.api.CalendarService;
import se.tink.backend.api.CategoryService;
import se.tink.backend.api.ConsentService;
import se.tink.backend.api.DeviceService;
import se.tink.backend.api.UserDataControlService;
import se.tink.backend.api.FollowService;
import se.tink.backend.api.InvestmentService;
import se.tink.backend.api.PropertyService;
import se.tink.backend.api.ProviderService;
import se.tink.backend.api.StatisticsService;
import se.tink.backend.api.TrackingService;
import se.tink.backend.api.TransferService;
import se.tink.backend.api.UserService;
import se.tink.backend.api.VersionService;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.template.PooledRythmProxy;
import se.tink.backend.core.MortgageDistribution;
import se.tink.backend.main.controllers.UserDataControlServiceController;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.main.controllers.AccountServiceController;
import se.tink.backend.main.controllers.ActivityServiceController;
import se.tink.backend.main.controllers.CalendarServiceController;
import se.tink.backend.main.controllers.CategoryController;
import se.tink.backend.main.controllers.DeviceServiceController;
import se.tink.backend.main.controllers.InvestmentServiceController;
import se.tink.backend.main.controllers.MarketServiceController;
import se.tink.backend.main.controllers.ProviderServiceController;
import se.tink.backend.main.controllers.StatisticsServiceController;
import se.tink.backend.main.controllers.TrackingServiceController;
import se.tink.backend.main.controllers.TransferServiceController;
import se.tink.backend.main.controllers.UserTrackerController;
import se.tink.backend.main.histograms.SavingDistribution;
import se.tink.backend.main.i18n.AbnAmroInsightsLocalizableKeys;
import se.tink.backend.main.i18n.InsightsLocalizableKeys;
import se.tink.backend.main.i18n.TinkInsightsLocalizableKeys;
import se.tink.backend.main.providers.transfer.TransferDestinationPatternProvider;
import se.tink.backend.main.providers.transfer.TransferDestinationPatternProviderImpl;
import se.tink.backend.main.providers.transfer.TransferSourceAccountProvider;
import se.tink.backend.main.providers.transfer.TransferSourceAccountProviderImpl;
import se.tink.backend.main.providers.transfer.UserTransferDestinationProvider;
import se.tink.backend.main.providers.transfer.UserTransferDestinationProviderImpl;
import se.tink.backend.main.resources.UserServiceResource;
import se.tink.backend.main.resources.VersionServiceResource;
import se.tink.backend.main.transports.AccountServiceJerseyTransport;
import se.tink.backend.main.transports.ActivityServiceJerseyTransport;
import se.tink.backend.main.transports.ApplicationServiceJerseyTransport;
import se.tink.backend.main.transports.AuthenticationServiceJerseyTransport;
import se.tink.backend.main.transports.CalendarServiceJerseyTransport;
import se.tink.backend.main.transports.CategoryJerseyTransport;
import se.tink.backend.main.transports.ConsentServiceJerseyTransport;
import se.tink.backend.main.transports.DeviceServiceJerseyTransport;
import se.tink.backend.main.transports.UserDataControlServiceJerseyTransport;
import se.tink.backend.main.transports.FollowServiceJerseyTransport;
import se.tink.backend.main.transports.InvestmentServiceJerseyTransport;
import se.tink.backend.main.transports.PropertyServiceJerseyTransport;
import se.tink.backend.main.transports.ProviderServiceJerseyTransport;
import se.tink.backend.main.transports.StatisticsServiceJerseyTransport;
import se.tink.backend.main.transports.TrackingServiceJerseyTransport;
import se.tink.backend.main.transports.TransferServiceJerseyTransport;
import se.tink.backend.main.utils.ActivityHtmlHelper;
import se.tink.backend.main.utils.OnboardingSelector;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;

public class MainServiceModule extends AbstractModule {
    private static final LogUtils log = new LogUtils(MainServiceModule.class);
    @Override
    protected void configure() {
        bind(AccountServiceController.class).in(Scopes.SINGLETON);
        bind(ActivityHtmlHelper.class).in(Scopes.SINGLETON);
        bind(ActivityServiceController.class).in(Scopes.SINGLETON);
        bind(AnalyticsController.class).in(Scopes.SINGLETON);
        bind(CategoryController.class).in(Scopes.SINGLETON);
        bind(DeviceServiceController.class).in(Scopes.SINGLETON);
        bind(MarketServiceController.class).in(Scopes.SINGLETON);
        bind(OnboardingSelector.class).in(Scopes.SINGLETON);
        bind(InvestmentServiceController.class).in(Scopes.SINGLETON);
        bind(ProviderServiceController.class).in(Scopes.SINGLETON);
        bind(ResourceTimerFilterFactory.class).in(Scopes.SINGLETON);
        bind(StatisticsServiceController.class).in(Scopes.SINGLETON);
        bind(UserTrackerController.class).in(Scopes.SINGLETON);
        bind(TrackingServiceController.class).in(Scopes.SINGLETON);
        bind(TransferServiceController.class).in(Scopes.SINGLETON);
        bind(CalendarServiceController.class).in(Scopes.SINGLETON);

        bind(AccountService.class).to(AccountServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(ActivityService.class).to(ActivityServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(AuthenticationService.class).to(AuthenticationServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(CategoryService.class).to(CategoryJerseyTransport.class).in(Scopes.SINGLETON);
        bind(ConsentService.class).to(ConsentServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(DeviceService.class).to(DeviceServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(FollowService.class).to(FollowServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(PooledRythmProxy.class).in(Scopes.SINGLETON);
        bind(PropertyService.class).to(PropertyServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(InvestmentService.class).to(InvestmentServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(ProviderService.class).to(ProviderServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(StatisticsService.class).to(StatisticsServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(TrackingService.class).to(TrackingServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(TransferService.class).to(TransferServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(CalendarService.class).to(CalendarServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(VersionService.class).to(VersionServiceResource.class).in(Scopes.SINGLETON);
        bind(UserService.class).to(UserServiceResource.class).in(Scopes.SINGLETON);
        bind(ApplicationService.class).to(ApplicationServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(UserDataControlService.class).to(UserDataControlServiceJerseyTransport.class).in(Scopes.SINGLETON);

        // TODO Remove after getting rid of dependencies on ServiceContext
        bind(ServiceContext.class).in(Scopes.SINGLETON);

        bind(TransferDestinationPatternProvider.class).to(TransferDestinationPatternProviderImpl.class)
                .in(Scopes.SINGLETON);
        bind(UserTransferDestinationProvider.class).to(UserTransferDestinationProviderImpl.class);
        bind(TransferSourceAccountProvider.class).to(TransferSourceAccountProviderImpl.class);
    }

    @Provides
    @Singleton
    public MortgageDistribution provideMortgageDistribution() {
        try {
            return MortgageDistribution.loadDefault();
        } catch (IOException e) {
            log.error("Cannot load parse mortgage distribution");
            return new MortgageDistribution(Collections.emptyList());
        }
    }

    @Provides
    @Singleton
    public SavingDistribution provideSavingDistribution() {
        try {
            return SavingDistribution.loadDefault();
        } catch (IOException e) {
            log.error("Cannot load saving distribution");
            return new SavingDistribution(Collections.emptyList());
        }
    }

    @Provides
    @Singleton
    public DatabaseReader provideMarketsLookupDatabase() {
        try {
            return new DatabaseReader.Builder(new File("data/GeoIP2-Country.mmdb")).build();
        } catch (Exception e) {
            log.error("Could not initialize markets lookup database", e);
            return null;
        }
    }

    @Provides
    @Singleton
    public InsightsLocalizableKeys provideInsightsLocalizableKeys(Cluster cluster) {
        if (Objects.equals(cluster, Cluster.ABNAMRO)) {
            return new AbnAmroInsightsLocalizableKeys();
        }

        return new TinkInsightsLocalizableKeys();
    }
}
