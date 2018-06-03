package se.tink.backend.system.cli.seeding;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.cassandra.AggregatedAreaLoanDataRepository;
import se.tink.backend.common.repository.cassandra.ApplicationArchiveRepository;
import se.tink.backend.common.repository.cassandra.ApplicationEventRepository;
import se.tink.backend.common.repository.cassandra.ApplicationFormEventRepository;
import se.tink.backend.common.repository.cassandra.BankFeeStatisticsRepository;
import se.tink.backend.common.repository.cassandra.CassandraPeriodByUserIdRepository;
import se.tink.backend.common.repository.cassandra.CassandraStatisticRepository;
import se.tink.backend.common.repository.cassandra.CassandraTransactionByUserIdAndPeriodRepository;
import se.tink.backend.common.repository.cassandra.CassandraTransactionDeletedRepository;
import se.tink.backend.common.repository.cassandra.CassandraUserDemographicsRepository;
import se.tink.backend.common.repository.cassandra.CategoryChangeRecordRepository;
import se.tink.backend.common.repository.cassandra.CheckpointRepository;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.cassandra.DataExportFragmentsRepository;
import se.tink.backend.common.repository.cassandra.DataExportsRepository;
import se.tink.backend.common.repository.cassandra.DocumentRepository;
import se.tink.backend.common.repository.cassandra.EventRepository;
import se.tink.backend.common.repository.cassandra.ExternallyDeletedTransactionRepository;
import se.tink.backend.common.repository.cassandra.GiroRepository;
import se.tink.backend.common.repository.cassandra.InstrumentHistoryRepository;
import se.tink.backend.common.repository.cassandra.InstrumentRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.cassandra.LoanDetailsRepository;
import se.tink.backend.common.repository.cassandra.MerchantWizardSkippedTransactionRepository;
import se.tink.backend.common.repository.cassandra.NotificationEventRepository;
import se.tink.backend.common.repository.cassandra.OAuth2ClientEventRepository;
import se.tink.backend.common.repository.cassandra.PortfolioHistoryRepository;
import se.tink.backend.common.repository.cassandra.PortfolioRepository;
import se.tink.backend.common.repository.cassandra.ProducedEventQueueActivityRepository;
import se.tink.backend.common.repository.cassandra.ProductFilterRepository;
import se.tink.backend.common.repository.cassandra.ProductInstanceRepository;
import se.tink.backend.common.repository.cassandra.ProductTemplateRepository;
import se.tink.backend.common.repository.cassandra.ProviderTriesRepository;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.cassandra.TrackingEventRepository;
import se.tink.backend.common.repository.cassandra.TrackingSessionRepository;
import se.tink.backend.common.repository.cassandra.TrackingTimingRepository;
import se.tink.backend.common.repository.cassandra.TrackingViewRepository;
import se.tink.backend.common.repository.cassandra.TransactionCheckpointRepository;
import se.tink.backend.common.repository.cassandra.TransactionExternalIdRepository;
import se.tink.backend.common.repository.cassandra.TransferDestinationPatternRepository;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.common.repository.cassandra.UserCoordinatesRepository;
import se.tink.backend.common.repository.cassandra.UserLocationRepository;
import se.tink.backend.common.repository.cassandra.UserProfileDataRepository;
import se.tink.backend.common.repository.cassandra.UserTransferDestinationRepository;
import se.tink.backend.consent.repository.cassandra.ConsentRepository;
import se.tink.backend.consent.repository.cassandra.UserConsentRepository;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.cassandra.capabilities.Creatable;

public class CreateDistributedTablesCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(CreateDistributedTablesCommand.class);
    private static final ImmutableList<Class<? extends Creatable>> CREATABLE_REPOSITORIES =
            ImmutableList.<Class<? extends Creatable>>builder()
                    .add(CredentialsEventRepository.class)
                    .add(EventRepository.class)
                    .add(UserLocationRepository.class)
                    .add(CassandraStatisticRepository.class)
                    .add(CassandraTransactionByUserIdAndPeriodRepository.class)
                    .add(CassandraTransactionDeletedRepository.class)
                    .add(CassandraPeriodByUserIdRepository.class)
                    .add(CassandraUserDemographicsRepository.class)
                    .add(TrackingSessionRepository.class)
                    .add(TrackingEventRepository.class)
                    .add(TrackingTimingRepository.class)
                    .add(TrackingViewRepository.class)
                    .add(LoanDataRepository.class)
                    .add(LoanDetailsRepository.class)
                    .add(AggregatedAreaLoanDataRepository.class)
                    .add(ProviderTriesRepository.class)
                    .add(MerchantWizardSkippedTransactionRepository.class)
                    .add(BankFeeStatisticsRepository.class)
                    .add(TransferRepository.class)
                    .add(SignableOperationRepository.class)
                    .add(TransferEventRepository.class)
                    .add(TransferDestinationPatternRepository.class)
                    .add(UserTransferDestinationRepository.class)
                    .add(GiroRepository.class)
                    .add(UserProfileDataRepository.class)
                    .add(AccountBalanceHistoryRepository.class)
                    .add(ProductTemplateRepository.class)
                    .add(ProductInstanceRepository.class)
                    .add(ProductFilterRepository.class)
                    .add(DocumentRepository.class)
                    .add(UserCoordinatesRepository.class)
                    .add(OAuth2ClientEventRepository.class)
                    .add(ApplicationArchiveRepository.class)
                    .add(TransactionCheckpointRepository.class)
                    .add(CheckpointRepository.class)
                    .add(ApplicationEventRepository.class)
                    .add(ApplicationFormEventRepository.class)
                    .add(NotificationEventRepository.class)
                    .add(ExternallyDeletedTransactionRepository.class)
                    .add(TransactionExternalIdRepository.class)
                    .add(CategoryChangeRecordRepository.class)
                    .add(UserConsentRepository.class)
                    .add(ConsentRepository.class)
                    .add(InstrumentRepository.class)
                    .add(InstrumentHistoryRepository.class)
                    .add(PortfolioRepository.class)
                    .add(PortfolioHistoryRepository.class)
                    .add(ProducedEventQueueActivityRepository.class)
                    .add(DataExportFragmentsRepository.class)
                    .add(DataExportsRepository.class)
                    .build();

    public CreateDistributedTablesCommand() {
        super("create-distributed-tables", "Create all Cassandra tables.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        try {
            create(serviceContext);
        } catch (Exception e) {
            log.error("Something went wrong when creating table.", e);
        }
    }

    // Public to be reusable from SeedDatabaseCommand.
    public static void create(ServiceContext serviceContext) {
        for (Class<? extends Creatable> creatableRepository : CREATABLE_REPOSITORIES) {
            log.info("Creating repository (if not exists): " + creatableRepository.getSimpleName());
            serviceContext.getRepository(creatableRepository).createTableIfNotExist();
        }
    }
}
