package se.tink.backend.connector.configuration;

import com.google.inject.Scopes;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.DistributedDatabaseConfiguration;
import se.tink.backend.common.dao.transactions.TransactionRepositoryImpl;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.cassandra.CassandraPeriodByUserIdRepository;
import se.tink.backend.common.repository.cassandra.CassandraStatisticRepository;
import se.tink.backend.common.repository.cassandra.CassandraTransactionByUserIdAndPeriodRepository;
import se.tink.backend.common.repository.cassandra.CassandraTransactionDeletedRepository;
import se.tink.backend.common.repository.cassandra.CategoryChangeRecordRepository;
import se.tink.backend.common.repository.cassandra.CheckpointRepository;
import se.tink.backend.common.repository.cassandra.ExternallyDeletedTransactionRepository;
import se.tink.backend.common.repository.cassandra.TransactionExternalIdRepository;
import se.tink.backend.common.dao.transactions.TransactionRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroBufferedAccountRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroBufferedTransactionRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroSubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.FraudItemRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2WebHookRepository;
import se.tink.backend.common.repository.mysql.main.StatisticRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.guice.configuration.RepositoryModule;

public class ConnectorRepositoryModule extends RepositoryModule {

    public ConnectorRepositoryModule(DatabaseConfiguration databaseConfiguration,
            DistributedDatabaseConfiguration distributedDatabaseConfiguration) {
        super(databaseConfiguration, distributedDatabaseConfiguration);
    }

    @Override
    protected void bindDistributedDaos() {
        bind(TransactionRepository.class).to(TransactionRepositoryImpl.class);
        bind(TransactionDao.class).in(Scopes.SINGLETON);
    }

    @Override
    protected void bindRepositories() {
        // Bind repositories. Let's do it in alphabetical order.
        bindSpringBean(AbnAmroBufferedAccountRepository.class);
        bindSpringBean(AbnAmroBufferedTransactionRepository.class);
        bindSpringBean(AbnAmroSubscriptionRepository.class);
        bindSpringBean(AccountBalanceHistoryRepository.class);
        bindSpringBean(AccountRepository.class);
        bindSpringBean(CassandraTransactionByUserIdAndPeriodRepository.class);
        bindSpringBean(CassandraTransactionDeletedRepository.class);
        bindSpringBean(CassandraStatisticRepository.class);
        bindSpringBean(CassandraPeriodByUserIdRepository.class);
        bindSpringBean(CategoryChangeRecordRepository.class);
        bindSpringBean(CategoryRepository.class);
        bindSpringBean(CheckpointRepository.class);
        bindSpringBean(CredentialsRepository.class);
        bindSpringBean(ExternallyDeletedTransactionRepository.class);
        bindSpringBean(FraudDetailsRepository.class);
        bindSpringBean(FraudItemRepository.class);
        bindSpringBean(MarketRepository.class);
        bindSpringBean(OAuth2ClientRepository.class);
        bindSpringBean(OAuth2WebHookRepository.class);
        bindSpringBean(StatisticRepository.class);
        bindSpringBean(TransactionExternalIdRepository.class);
        bindSpringBean(UserRepository.class);
        bindSpringBean(UserStateRepository.class);
    }
}
