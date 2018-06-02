package se.tink.backend.system;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Providers;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.List;
import java.util.Optional;
import se.tink.backend.categorization.client.FastTextServiceFactory;
import se.tink.backend.common.AbstractServiceContainer;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.common.tasks.interfaces.TaskHandler;
import se.tink.backend.common.tasks.kafka.KafkaQueueResetter;
import se.tink.backend.common.tasks.kafka.StreamingKafkaConsumer;
import se.tink.backend.common.tasks.nsq.NSQConsumer;
import se.tink.backend.common.tasks.usecases.serialization.DelegatingTaskHandler;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.firehose.v1.guice.QueueProducerModule;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.insights.client.InsightsServiceFactory;
import se.tink.backend.system.cli.DumpUserDataCommand;
import se.tink.backend.system.cli.EvaluateCategorizationModel;
import se.tink.backend.system.cli.ImportTransactionsCommand;
import se.tink.backend.system.cli.RecalculateStatisticsCommand;
import se.tink.backend.system.cli.TransactionEncodingCheckerCommand;
import se.tink.backend.system.cli.abnamro.AbnAmroCredentialsMigrationCommand;
import se.tink.backend.system.cli.abnamro.CreateAccountsForConnectorTestsCommand;
import se.tink.backend.system.cli.abnamro.ExportCustomerDetailsCommand;
import se.tink.backend.system.cli.abnamro.IcsCreditCardsEvaluationCommand;
import se.tink.backend.system.cli.abnamro.IcsDuplicateCreditCardCommand;
import se.tink.backend.system.cli.abnamro.RecategorizeTransactionsCommand;
import se.tink.backend.system.cli.abnamro.ReprocessTransactionDescriptionsCommand;
import se.tink.backend.system.cli.abnamro.ResyncTransactionsCommand;
import se.tink.backend.system.cli.abnamro.SubscriptionsConsistencyCheckCommand;
import se.tink.backend.system.cli.analytics.CalculateUserDemographicsCommand;
import se.tink.backend.system.cli.analytics.CalculateUserRefreshFrequencyCommand;
import se.tink.backend.system.cli.analytics.CalculateWeeklyRefreshStatistics;
import se.tink.backend.system.cli.analytics.CopyUserDemographicsToCassandraCommand;
import se.tink.backend.system.cli.analytics.EvaluateMortgageTargeting;
import se.tink.backend.system.cli.analytics.EvaluatePaydateCommand;
import se.tink.backend.system.cli.analytics.EvaluatePaymentProviderCommand;
import se.tink.backend.system.cli.analytics.FetchChurnPredictionDataCommand;
import se.tink.backend.system.cli.analytics.FetchCreditScorePredictionDataCommand;
import se.tink.backend.system.cli.applications.GenerateApplicationDocumentsCommand;
import se.tink.backend.system.cli.benchmark.BenchmarkLoadingTransactionsCommand;
import se.tink.backend.system.cli.benchmark.BenchmarkLoadingUserDataCommand;
import se.tink.backend.system.cli.benchmark.BenchmarkParallelReads;
import se.tink.backend.system.cli.benchmark.BenchmarkStreamingTransactionsCommand;
import se.tink.backend.system.cli.cleanup.CleanupAccountHistoryCommand;
import se.tink.backend.system.cli.cleanup.CleanupDuplicateCredentialsCommand;
import se.tink.backend.system.cli.cleanup.CleanupDuplicateTransactionsCommand;
import se.tink.backend.system.cli.cleanup.CleanupHealthcheckZnodesCommand;
import se.tink.backend.system.cli.cleanup.CleanupLockZnodesCommand;
import se.tink.backend.system.cli.cleanup.CleanupOrphanAccountsCommand;
import se.tink.backend.system.cli.cleanup.CleanupOrphanedTransactionsPerAgent;
import se.tink.backend.system.cli.cleanup.CleanupSavingsItemCommand;
import se.tink.backend.system.cli.cleanup.DeleteAccountsAndTransactionsCommand;
import se.tink.backend.system.cli.cleanup.DeleteCredentialsAccountCommand;
import se.tink.backend.system.cli.cleanup.DeleteCredentialsCommand;
import se.tink.backend.system.cli.cleanup.DetectDuplicatedAccountsCommand;
import se.tink.backend.system.cli.cleanup.FixSettledTransactionData;
import se.tink.backend.system.cli.debug.AddUsernameCommand;
import se.tink.backend.system.cli.debug.ChangeCertainDateCommand;
import se.tink.backend.system.cli.debug.ChangeCertainDateForAgentCommand;
import se.tink.backend.system.cli.debug.ChangeCredentialsTypeCommand;
import se.tink.backend.system.cli.debug.ChangeProviderRefreshFrequencyFactorCommand;
import se.tink.backend.system.cli.debug.DebugApplicationsCommand;
import se.tink.backend.system.cli.debug.DebugProviderCommand;
import se.tink.backend.system.cli.debug.DebugTransferCommand;
import se.tink.backend.system.cli.debug.DebugUserCommand;
import se.tink.backend.system.cli.debug.DebugUserStatisticsCommand;
import se.tink.backend.system.cli.debug.EnableCredentialsDebuggingCommand;
import se.tink.backend.system.cli.debug.EnableUserDebuggingCommand;
import se.tink.backend.system.cli.debug.IntegrationDebugCommand;
import se.tink.backend.system.cli.debug.MigrateCredentialsToBankIdCommand;
import se.tink.backend.system.cli.debug.ProviderStatusCommand;
import se.tink.backend.system.cli.debug.TransactionProcessorRollbackCommand;
import se.tink.backend.system.cli.debug.TransactionUserLearningDebugCommand;
import se.tink.backend.system.cli.debug.TransferStatisticsCommand;
import se.tink.backend.system.cli.export.ExportUserDataCommand;
import se.tink.backend.system.cli.extraction.DetermineUserLocationCommand;
import se.tink.backend.system.cli.extraction.ExtractCategorizationTrainingDataCommand;
import se.tink.backend.system.cli.extraction.ExtractDemoUsersCommand;
import se.tink.backend.system.cli.extraction.ExtractDescriptionsToCategor;
import se.tink.backend.system.cli.extraction.ExtractMarketToCategor;
import se.tink.backend.system.cli.extraction.ExtractMortgageDataCommand;
import se.tink.backend.system.cli.extraction.ExtractPeriodBreaksCommand;
import se.tink.backend.system.cli.extraction.ExtractUsersActiveBeforeCommand;
import se.tink.backend.system.cli.fraud.CreditSafeDataRefresherCommand;
import se.tink.backend.system.cli.fraud.FraudDataMigrationCommand;
import se.tink.backend.system.cli.fraud.ImportCreditsafeCompanyDataCommand;
import se.tink.backend.system.cli.gdpr.DeletedUsersVerificationCommand;
import se.tink.backend.system.cli.interestmap.AssignUsersAreasCommand;
import se.tink.backend.system.cli.interestmap.BuildGeoJsonCommand;
import se.tink.backend.system.cli.interestmap.MergeOSMAndDistrictCommand;
import se.tink.backend.system.cli.interestmap.MergeOSMGeoJsonFilesIntoAreasCommand;
import se.tink.backend.system.cli.loans.LoanAdHocCommand;
import se.tink.backend.system.cli.location.GeoLookupUserLocationCommand;
import se.tink.backend.system.cli.location.SeedCityCoordinatesCommand;
import se.tink.backend.system.cli.location.TestLocationAggregation;
import se.tink.backend.system.cli.merchants.ImproveMerchantQualityCommand;
import se.tink.backend.system.cli.migration.AffectedTransactionsCommand;
import se.tink.backend.system.cli.migration.CleanDuplicateTransactionIdsCommand;
import se.tink.backend.system.cli.migration.FixNullTransactionAmountsCommand;
import se.tink.backend.system.cli.migration.GdprDeletedUserMigrationCommand;
import se.tink.backend.system.cli.migration.MigrateAmexBankId;
import se.tink.backend.system.cli.migration.MigrateCSNAccountBankId;
import se.tink.backend.system.cli.migration.MigrateCredentialsCommand;
import se.tink.backend.system.cli.migration.MigrateCredentialsLFPasswordToBankIdCommand;
import se.tink.backend.system.cli.migration.MigrateNationalIdCommand;
import se.tink.backend.system.cli.migration.MigratePasswordCredentialsToBankIdCommand;
import se.tink.backend.system.cli.migration.MigrateSwedbankPortfolioId;
import se.tink.backend.system.cli.migration.RestoreDeletedTransactionsCommand;
import se.tink.backend.system.cli.migration.TransferDoubleToBigDecimalMigration;
import se.tink.backend.system.cli.notifications.SendPushNotificationsCommand;
import se.tink.backend.system.cli.reporting.MortgageReportingCommand;
import se.tink.backend.system.cli.reporting.OAuthClientInformationCommand;
import se.tink.backend.system.cli.reporting.OAuthReportingCommand;
import se.tink.backend.system.cli.security.ReencryptCredentialsCommand;
import se.tink.backend.system.cli.seeding.AddOnlineMerchants;
import se.tink.backend.system.cli.seeding.CleanDatabaseCommand;
import se.tink.backend.system.cli.seeding.CreateDistributedTablesCommand;
import se.tink.backend.system.cli.seeding.DeleteUserCommand;
import se.tink.backend.system.cli.seeding.DownloadCoordinatesForPostalCodeAreasCommand;
import se.tink.backend.system.cli.seeding.RefreshApplicationsCommand;
import se.tink.backend.system.cli.seeding.RefreshProductsCommand;
import se.tink.backend.system.cli.seeding.SeedCoordinatesForAllUsersCommand;
import se.tink.backend.system.cli.seeding.SeedDatabaseCommand;
import se.tink.backend.system.cli.seeding.SeedMerchantsCommand;
import se.tink.backend.system.cli.seeding.SeedMerchantsWithPropertiesCommand;
import se.tink.backend.system.cli.seeding.SeedPostalCodeAreasCommand;
import se.tink.backend.system.cli.seeding.SeedProvidersCommand;
import se.tink.backend.system.cli.seeding.SeedProvidersForMarketCommand;
import se.tink.backend.system.cli.seeding.TargetProductsCommand;
import se.tink.backend.system.cli.seeding.UpdateCategoryTreeCommand;
import se.tink.backend.system.cli.seeding.search.RebuildSearchIndicesCommand;
import se.tink.backend.system.cli.segmentation.DistributeUserFlagCommand;
import se.tink.backend.system.cli.segmentation.ModifyUserFlagsCommand;
import se.tink.backend.system.cli.segmentation.UserFlagModificationCommand;
import se.tink.backend.system.cli.statistics.EvaluatePeriodCalculationCommand;
import se.tink.backend.system.cli.statistics.market.FetchMarketStatisticsCommand;
import se.tink.backend.system.cli.training.TrainFastTextModelCommand;
import se.tink.backend.system.client.InProcessSystemServiceFactoryBuilder;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.guice.configuration.SystemModuleFactory;
import se.tink.backend.system.resources.CronServiceResource;
import se.tink.backend.system.tasks.CheckpointRollbackTaskHandler;
import se.tink.backend.system.tasks.DeleteTransactionTaskHandler;
import se.tink.backend.system.tasks.Task;
import se.tink.backend.system.tasks.UpdateTransactionsTaskHandler;
import se.tink.backend.system.workers.cli.processing.RemoveUserModifiedCategoryAndReprocessTransactionsCommand;
import se.tink.backend.system.workers.cli.processing.RepairTransactionsCommand;
import se.tink.backend.system.workers.cli.processing.ReprocessTransactionsCommand;
import se.tink.backend.system.workers.processor.chaining.DefaultUserChainFactoryCreator;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.libraries.auth.ApiTokenAuthorizationHeaderPredicate;
import se.tink.libraries.auth.ContainerAuthorizationResourceFilterFactory;
import se.tink.libraries.auth.YubicoAuthorizationHeaderPredicate;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.discovery.ServiceDiscoveryHelper;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;
import se.tink.libraries.metrics.MetricRegistry;

public class SystemServiceContainer extends AbstractServiceContainer {

    private static final ImmutableList<Command> commands = ImmutableList.of(
            new FetchMarketStatisticsCommand(),
            new SeedDatabaseCommand(),
            new SeedMerchantsCommand(),
            new SeedProvidersCommand(),
            new SeedProvidersForMarketCommand(),
            new DebugProviderCommand(),
            new SeedCityCoordinatesCommand(),
            new DownloadCoordinatesForPostalCodeAreasCommand(),
            new SeedPostalCodeAreasCommand(),
            new CleanDatabaseCommand(),
            new ExtractDemoUsersCommand(),
            new RebuildSearchIndicesCommand(),
            new ReprocessTransactionsCommand(),
            new RepairTransactionsCommand(),
            new RecalculateStatisticsCommand(),
            new UpdateCategoryTreeCommand(),
            new DumpUserDataCommand(),
            new DeleteUserCommand(),
            new SendPushNotificationsCommand(),
            new ExtractPeriodBreaksCommand(),
            new DetermineUserLocationCommand(),
            new AddOnlineMerchants(),
            new CalculateUserDemographicsCommand(),
            new CalculateUserRefreshFrequencyCommand(),
            new GeoLookupUserLocationCommand(),
            new CreateDistributedTablesCommand(),
            new DistributeUserFlagCommand(),
            new TransactionEncodingCheckerCommand(),
            new EvaluatePeriodCalculationCommand(),
            new CopyUserDemographicsToCassandraCommand(),
            new DebugUserCommand(),
            new DebugUserStatisticsCommand(),
            new DebugTransferCommand(),
            new SeedMerchantsWithPropertiesCommand(),
            new ImproveMerchantQualityCommand(),
            new FraudDataMigrationCommand(),
            new DeleteCredentialsAccountCommand(),
            new ImportCreditsafeCompanyDataCommand(),
            new CreditSafeDataRefresherCommand(),
            new ExtractUsersActiveBeforeCommand(),
            new LoanAdHocCommand(),
            new CleanupDuplicateTransactionsCommand(),
            new DeleteAccountsAndTransactionsCommand(),
            new TransactionProcessorRollbackCommand(),
            new EvaluatePaydateCommand(),
            new EvaluatePaymentProviderCommand(),
            new TransferStatisticsCommand(),
            new BenchmarkLoadingUserDataCommand(),
            new BenchmarkLoadingTransactionsCommand(),
            new BenchmarkParallelReads(),
            new BenchmarkStreamingTransactionsCommand(),
            new EnableCredentialsDebuggingCommand(),
            new EnableUserDebuggingCommand(),
            new ChangeProviderRefreshFrequencyFactorCommand(),
            new ProviderStatusCommand(),
            new MigrateCredentialsToBankIdCommand(),
            new ChangeCredentialsTypeCommand(),
            new CleanupHealthcheckZnodesCommand(),
            new EvaluateMortgageTargeting(),
            new CleanupLockZnodesCommand(),
            new UserFlagModificationCommand(),
            new TargetProductsCommand(),
            new RefreshProductsCommand(),
            new ChangeCertainDateCommand(),
            new ChangeCertainDateForAgentCommand(),
            new CalculateWeeklyRefreshStatistics(),
            new DebugApplicationsCommand(),
            new GenerateApplicationDocumentsCommand(),
            new RefreshApplicationsCommand(),
            new MortgageReportingCommand(),
            new DetectDuplicatedAccountsCommand(),
            new ExtractCategorizationTrainingDataCommand(),
            new DeleteCredentialsCommand(),
            new CleanupAccountHistoryCommand(),
            new ExtractMortgageDataCommand(),
            new FetchCreditScorePredictionDataCommand(),
            new CleanupSavingsItemCommand(),
            new IntegrationDebugCommand(),
            // Interest Map commands
            new BuildGeoJsonCommand(),
            new AssignUsersAreasCommand(),
            new SeedCoordinatesForAllUsersCommand(),
            new MergeOSMGeoJsonFilesIntoAreasCommand(),
            new MergeOSMAndDistrictCommand(),
            // ABN AMRO commands.
            new ResyncTransactionsCommand(),
            new ReprocessTransactionDescriptionsCommand(),
            new IcsCreditCardsEvaluationCommand(),
            new IcsDuplicateCreditCardCommand(),
            new RecategorizeTransactionsCommand(),
            new CreateAccountsForConnectorTestsCommand(),
            new SubscriptionsConsistencyCheckCommand(),
            new ExportCustomerDetailsCommand(),
            new AbnAmroCredentialsMigrationCommand(),
            // OAuth2 commands
            new OAuthReportingCommand(),
            new OAuthClientInformationCommand(),
            // Temporary data fetching commands for thesis projects.
            new FetchChurnPredictionDataCommand(),
            // Migration
            new TransferDoubleToBigDecimalMigration(),
            new CleanupDuplicateCredentialsCommand(),
            new MigrateCSNAccountBankId(),
            new MigrateNationalIdCommand(),
            new ReencryptCredentialsCommand(),
            new MigrateCredentialsCommand(),
            new MigrateSwedbankPortfolioId(),
            new MigrateAmexBankId(),
            // Development commands
            new TestLocationAggregation(),
            new EvaluateCategorizationModel(),
            new ImportTransactionsCommand(),
            new TransactionUserLearningDebugCommand(),
            new AddUsernameCommand(),
            new RemoveUserModifiedCategoryAndReprocessTransactionsCommand(),
            new AffectedTransactionsCommand(),
            new ModifyUserFlagsCommand(),
            new ExtractDescriptionsToCategor(),
            new ExtractMarketToCategor(),
            new RestoreDeletedTransactionsCommand(),
            new FixNullTransactionAmountsCommand(),
            new CleanupOrphanAccountsCommand(),
            new CleanupOrphanedTransactionsPerAgent(),
            new ExportUserDataCommand(),
            new TrainFastTextModelCommand(),
            new CleanDuplicateTransactionIdsCommand(),
            new FixSettledTransactionData(),
            new MigrateCredentialsLFPasswordToBankIdCommand(),
            new MigratePasswordCredentialsToBankIdCommand(),
            // GDPR
            new DeletedUsersVerificationCommand(),
            new GdprDeletedUserMigrationCommand()
    );

    public static void main(String[] args) throws Exception {
        new SystemServiceContainer().run(args);
    }

    @Override
    public String getName() {
        return SystemServiceFactory.SERVICE_NAME;
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
        commands.forEach(bootstrap::addCommand);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void build(ServiceConfiguration configuration, Environment environment) {

        if (configuration.getServiceAuthentication() != null
                && !configuration.getServiceAuthentication().getServerTokens().isEmpty()) {
            Predicate<String> authorizationAuthorizers = Predicates.or(
                    new ApiTokenAuthorizationHeaderPredicate(configuration.getServiceAuthentication()
                            .getServerTokens()),
                    new YubicoAuthorizationHeaderPredicate(
                            configuration.getYubicoClientId(),
                            configuration.getServiceAuthentication().getYubikeys()));
            environment.jersey().getResourceConfig().getResourceFilterFactories()
                    .add(new ContainerAuthorizationResourceFilterFactory(authorizationAuthorizers));
        }
        List<Module> modules = Lists.newArrayList(new QueueProducerModule());
        modules.addAll(SystemModuleFactory.build(configuration, environment.jersey()));
        Injector injector = DropwizardLifecycleInjectorFactory.build(environment.lifecycle(), modules);

        buildContainer(configuration, environment, injector.getInstance(ServiceContext.class), injector);
    }

    private void buildContainer(ServiceConfiguration configuration, Environment environment,
            ServiceContext serviceContext, Injector injector) {
        ServiceDiscoveryHelper serviceDiscoveryService = constructServiceDiscoveryHelperFromConfiguration(
                serviceContext.getCoordinationClient(), configuration, SystemServiceFactory.SERVICE_NAME);
        environment.lifecycle().manage(serviceDiscoveryService);

        LeaderCandidate leaderCandidate = injector.getInstance(LeaderCandidate.class);

        // Start the services.
        InProcessSystemServiceFactoryBuilder inProcessSystemServiceFactoryBuilder = new InProcessSystemServiceFactoryBuilder(
                serviceContext,
                injector.getInstance(InsightsServiceFactory.class), Optional.of(environment),
                Optional.of(leaderCandidate),
                injector.getInstance(CronServiceResource.class), injector.getInstance(MetricRegistry.class));
        KafkaQueueResetter queueResetter = injector.getInstance(KafkaQueueResetter.class);
        Cluster cluster = serviceContext.getConfiguration().getCluster();
        inProcessSystemServiceFactoryBuilder.buildAndRegister(
                queueResetter,
                injector.getInstance(FirehoseQueueProducer.class),
                Providers.of(MarketDescriptionFormatterFactory.byCluster(cluster)),
                Providers.of(MarketDescriptionExtractorFactory.byCluster(cluster)),
                injector.getInstance(ClusterCategories.class),
                injector.getInstance(FastTextServiceFactory.class),
                injector.getInstance(ElasticSearchClient.class),
                injector.getInstance(DefaultUserChainFactoryCreator.class));

        startWorkers(serviceContext, configuration, environment, queueResetter,
                injector.getInstance(MetricRegistry.class));
    }

    private void startWorkers(ServiceContext serviceContext,
            final ServiceConfiguration configuration, Environment environment, final KafkaQueueResetter queueResetter,
            MetricRegistry metricRegistry) {
        TasksQueueConfiguration queueConfiguration = configuration.getTaskQueue();
        if (TasksQueueConfiguration.SHOULD_RUN.contains(queueConfiguration.getMode())) {

            ImmutableList<? extends TaskHandler<? extends Task<?>>> taskHandlers = ImmutableList.of(
                    new UpdateTransactionsTaskHandler(serviceContext, queueConfiguration.getMainTopicLimit()),
                    new CheckpointRollbackTaskHandler(serviceContext),
                    new DeleteTransactionTaskHandler(serviceContext));

            DelegatingTaskHandler delegator = new DelegatingTaskHandler(metricRegistry, taskHandlers);
            if (queueConfiguration.useNSQ()) {
                environment.lifecycle()
                        .manage(new NSQConsumer(queueConfiguration, Task.TOPICS, delegator, metricRegistry));
            } else {
                environment.lifecycle().manage(new StreamingKafkaConsumer(queueConfiguration,
                        Task.TOPICS, delegator, metricRegistry));
            }
        }
    }
}
