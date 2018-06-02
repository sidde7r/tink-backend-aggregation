package se.tink.backend.insights.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.DistributedDatabaseConfiguration;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.common.dao.CategoryDao;
import se.tink.backend.common.dao.InvestmentDao;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.dao.StatisticDao;
import se.tink.backend.common.dao.transactions.TransactionRepositoryImpl;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.cassandra.CassandraPeriodByUserIdRepository;
import se.tink.backend.common.repository.cassandra.CassandraStatisticRepository;
import se.tink.backend.common.repository.cassandra.CassandraTransactionByUserIdAndPeriodRepository;
import se.tink.backend.common.repository.cassandra.CassandraTransactionDeletedRepository;
import se.tink.backend.common.repository.cassandra.CategoryChangeRecordRepository;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.repository.cassandra.InstrumentHistoryRepository;
import se.tink.backend.common.repository.cassandra.InstrumentRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.cassandra.LoanDetailsRepository;
import se.tink.backend.common.repository.cassandra.PortfolioHistoryRepository;
import se.tink.backend.common.repository.cassandra.PortfolioRepository;
import se.tink.backend.common.repository.cassandra.TransferDestinationPatternRepository;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.common.repository.elasticsearch.TransactionSearchIndex;
import se.tink.backend.common.dao.transactions.TransactionRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2AuthorizationRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.StatisticRepository;
import se.tink.backend.common.repository.mysql.main.SubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.SubscriptionTokenRepository;
import se.tink.backend.common.repository.mysql.main.UserDemographicsRepository;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.common.repository.mysql.main.UserEventRepository;
import se.tink.backend.common.repository.mysql.main.UserForgotPasswordTokenRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserSessionRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.search.SearchProxy;
import se.tink.backend.guice.configuration.RepositoryModule;
import se.tink.backend.insights.app.repositories.ArchivedInsightRepository;
import se.tink.backend.insights.app.repositories.InsightRepository;
import se.tink.backend.insights.storage.CachedInMemoryArchivedInsightRepositoryImpl;
import se.tink.backend.insights.storage.CachedInMemoryInsightRepositoryImpl;
import se.tink.libraries.metrics.MetricRegistry;

public class InsightsRepositoryModule extends RepositoryModule {

    InsightsRepositoryModule(DatabaseConfiguration databaseConfiguration,
            DistributedDatabaseConfiguration distributedDatabaseConfiguration) {
        super(databaseConfiguration, distributedDatabaseConfiguration);
    }

    protected void configure() {
        super.configure();

        // Bind local in memory repositories for Insights. To be added to our DB:s.
        bind(ArchivedInsightRepository.class).to(CachedInMemoryArchivedInsightRepositoryImpl.class)
                .in(Scopes.SINGLETON);
        bind(InsightRepository.class).to(CachedInMemoryInsightRepositoryImpl.class)
                .in(Scopes.SINGLETON);
    }

    @Provides
    protected TransactionSearchIndex providerTransactionSearchIndex(MetricRegistry registry) {
        return new TransactionSearchIndex(SearchProxy.getInstance().getClient(), new ObjectMapper(), registry);
    }

    @Override
    protected void bindCentralizedDaos() {
        bind(CategoryDao.class).in(Scopes.SINGLETON);
        bind(ProviderDao.class).in(Scopes.SINGLETON);
        bind(StatisticDao.class).in(Scopes.SINGLETON);
    }

    @Override
    protected void bindDistributedDaos() {

        bind(AccountDao.class).in(Scopes.SINGLETON);
        bind(InvestmentDao.class).in(Scopes.SINGLETON);
        bind(LoanDAO.class).in(Scopes.SINGLETON);
        bind(TransactionDao.class).in(Scopes.SINGLETON);
        bind(TransactionRepository.class).to(TransactionRepositoryImpl.class);
    }

    @Override
    protected void bindRepositories() {

        bindSpringBean(AccountBalanceHistoryRepository.class);
        bindSpringBean(AccountRepository.class);
        bindSpringBean(CurrencyRepository.class);
        bindSpringBean(CassandraPeriodByUserIdRepository.class);
        bindSpringBean(CassandraTransactionDeletedRepository.class);
        bindSpringBean(CassandraTransactionByUserIdAndPeriodRepository.class);
        bindSpringBean(CassandraStatisticRepository.class);
        bindSpringBean(CategoryChangeRecordRepository.class);
        bindSpringBean(CategoryRepository.class);
        bindSpringBean(CredentialsRepository.class);
        bindSpringBean(FollowItemRepository.class);
        bindSpringBean(InstrumentRepository.class);
        bindSpringBean(InstrumentHistoryRepository.class);
        bindSpringBean(LoanDataRepository.class);
        bindSpringBean(LoanDetailsRepository.class);
        bindSpringBean(MarketRepository.class);
        bindSpringBean(OAuth2AuthorizationRepository.class);
        bindSpringBean(OAuth2ClientRepository.class);
        bindSpringBean(PortfolioRepository.class);
        bindSpringBean(PortfolioHistoryRepository.class);
        bindSpringBean(ProviderRepository.class);
        bindSpringBean(StatisticRepository.class);
        bindSpringBean(SubscriptionRepository.class);  // Todo: Remove, Used for authentication
        bindSpringBean(SubscriptionTokenRepository.class);  // Todo: Remove, Used for authentication
        bindSpringBean(TransferDestinationPatternRepository.class);
        bindSpringBean(TransferEventRepository.class);
        bindSpringBean(TransferRepository.class);
        bindSpringBean(UserDemographicsRepository.class);
        bindSpringBean(UserDeviceRepository.class);
        bindSpringBean(UserEventRepository.class);                // Todo: Remove, Used for authentication
        bindSpringBean(UserForgotPasswordTokenRepository.class);  // Todo: Remove, Used for authentication
        bindSpringBean(UserRepository.class);
        bindSpringBean(UserSessionRepository.class);
        bindSpringBean(UserStateRepository.class);
        bindSpringBean(FraudDetailsRepository.class);
    }
}
