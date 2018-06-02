package se.tink.backend.system.guice.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.ApplicationCredentialsController;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.mail.monthly.summary.MonthlySummaryGenerator;
import se.tink.backend.common.mail.monthly.summary.MonthlySummaryReminderGenerator;
import se.tink.backend.common.product.CredentialsMortgageAmountFinder;
import se.tink.backend.common.search.SearchParser;
import se.tink.backend.common.search.TransactionsSearcher;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.common.tasks.kafka.KafkaQueueResetter;
import se.tink.backend.common.template.PooledRythmProxy;
import se.tink.backend.common.workers.notifications.channels.MobileNotificationSender;
import se.tink.backend.common.workers.notifications.channels.MobileNotificationSenderImpl;
import se.tink.backend.system.LeaderCandidate;
import se.tink.backend.system.api.NotificationGatewayService;
import se.tink.backend.system.cli.seeding.ProductRefreshConfiguration;
import se.tink.backend.system.controllers.ProductController;
import se.tink.backend.system.notifications.NotificationGateway;
import se.tink.backend.system.product.mortgage.ApplicationMortgageParameterFinder;
import se.tink.backend.system.product.mortgage.CompoundMortgageParameterFinder;
import se.tink.backend.system.product.mortgage.DefaultMortgageParameterFinder;
import se.tink.backend.system.product.mortgage.FraudDetailsPropertyTypeFinder;
import se.tink.backend.system.product.mortgage.MortgageParameterFinder;
import se.tink.backend.system.product.mortgage.MortgageProductRefresher;
import se.tink.backend.system.product.mortgage.ResidenceValuationMortgageParameterFinder;
import se.tink.backend.system.product.savings.SavingsProductRefresher;
import se.tink.backend.system.resources.CronServiceResource;
import se.tink.backend.system.statistics.SystemStatisticsReporter;
import se.tink.backend.system.transports.NotificationTransport;
import se.tink.backend.system.workers.processor.creditsafe.CreditSafeDataRefresher;
import se.tink.libraries.metrics.LowHeapMemoryMonitor;

public class SystemServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(NotificationGatewayService.class)
                .to(NotificationTransport.class).in(Scopes.SINGLETON);
        bind(NotificationGateway.class).in(Scopes.SINGLETON);
        bind(MobileNotificationSender.class).to(MobileNotificationSenderImpl.class).in(Scopes.SINGLETON);

        bind(ProductController.class).in(Scopes.SINGLETON);
        bind(MortgageProductRefresher.class).in(Scopes.SINGLETON);
        bind(MortgageParameterFinder.class).to(CompoundMortgageParameterFinder.class).in(Scopes.SINGLETON);
        bind(ApplicationMortgageParameterFinder.class).in(Scopes.SINGLETON);
        bind(ResidenceValuationMortgageParameterFinder.class).in(Scopes.SINGLETON);
        bind(DefaultMortgageParameterFinder.class).in(Scopes.SINGLETON);
        bind(CredentialsMortgageAmountFinder.class).in(Scopes.SINGLETON);
        bind(FraudDetailsPropertyTypeFinder.class).in(Scopes.SINGLETON);
        bind(SavingsProductRefresher.class).in(Scopes.SINGLETON);
        bind(ProductRefreshConfiguration.class).toInstance(new ProductRefreshConfiguration(
                ProductRefreshConfiguration.Scope.SYSTEM_SERVICE)); // Use default properties for other values
        bind(KafkaQueueResetter.class).in(Scopes.SINGLETON);
        bind(LeaderCandidate.class).in(Scopes.SINGLETON);
        bind(CronServiceResource.class).in(Scopes.SINGLETON);
        bind(SystemStatisticsReporter.class).in(Scopes.SINGLETON);
        bind(PooledRythmProxy.class).in(Scopes.SINGLETON);
        bind(SearchParser.class).in(Scopes.SINGLETON);
        bind(TransactionsSearcher.class).in(Scopes.SINGLETON);
        bind(MonthlySummaryGenerator.class).in(Scopes.SINGLETON);
        bind(MonthlySummaryReminderGenerator.class).in(Scopes.SINGLETON);
        bind(ApplicationCredentialsController.class).in(Scopes.SINGLETON);
        bind(CreditSafeDataRefresher.class).in(Scopes.SINGLETON);
        bind(AnalyticsController.class).in(Scopes.SINGLETON);
        bind(ElasticSearchClient.class).in(Scopes.SINGLETON);
        // TODO Remove after getting rid of dependencies on ServiceContext
        bind(ServiceContext.class).in(Scopes.SINGLETON);
        bind(LowHeapMemoryMonitor.class).in(Scopes.SINGLETON);
    }

    /**
     * @param parameterFinder1 Highest prioritized finder
     * @param parameterFinder2 ...
     * @param parameterFinder3 ...
     * @return List of prioritized mortgage parameter finders
     */
    @Provides
    ImmutableList<MortgageParameterFinder> provideMortgageParameterFinders(
            ApplicationMortgageParameterFinder parameterFinder1,
            ResidenceValuationMortgageParameterFinder parameterFinder2,
            DefaultMortgageParameterFinder parameterFinder3) {
        return ImmutableList.of(parameterFinder1, parameterFinder2, parameterFinder3);
    }
}
