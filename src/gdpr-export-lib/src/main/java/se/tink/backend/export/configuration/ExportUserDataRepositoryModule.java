package se.tink.backend.export.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.DistributedDatabaseConfiguration;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.dao.CategoryDao;
import se.tink.backend.common.dao.DeviceConfigurationDao;
import se.tink.backend.common.dao.InvestmentDao;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.dao.transactions.TransactionRepositoryImpl;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.cassandra.ApplicationEventRepository;
import se.tink.backend.common.repository.cassandra.ApplicationFormEventRepository;
import se.tink.backend.common.repository.cassandra.CassandraPeriodByUserIdRepository;
import se.tink.backend.common.repository.cassandra.CassandraTransactionByUserIdAndPeriodRepository;
import se.tink.backend.common.repository.cassandra.CassandraTransactionDeletedRepository;
import se.tink.backend.common.repository.cassandra.CategoryChangeRecordRepository;
import se.tink.backend.common.repository.cassandra.CheckpointRepository;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
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
import se.tink.backend.common.repository.cassandra.OAuth2ClientEventRepository;
import se.tink.backend.common.repository.cassandra.PortfolioHistoryRepository;
import se.tink.backend.common.repository.cassandra.PortfolioRepository;
import se.tink.backend.common.repository.cassandra.ProductFilterRepository;
import se.tink.backend.common.repository.cassandra.ProductInstanceRepository;
import se.tink.backend.common.repository.cassandra.ProductTemplateRepository;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.cassandra.TrackingEventRepository;
import se.tink.backend.common.repository.cassandra.TrackingSessionRepository;
import se.tink.backend.common.repository.cassandra.TrackingTimingRepository;
import se.tink.backend.common.repository.cassandra.TrackingViewRepository;
import se.tink.backend.common.repository.cassandra.TransactionExternalIdRepository;
import se.tink.backend.common.repository.cassandra.TransferDestinationPatternRepository;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.common.repository.cassandra.UserLocationRepository;
import se.tink.backend.common.repository.cassandra.UserProfileDataRepository;
import se.tink.backend.common.repository.cassandra.UserTransferDestinationRepository;
import se.tink.backend.common.repository.elasticsearch.TransactionSearchIndex;
import se.tink.backend.common.dao.transactions.TransactionRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroBufferedAccountRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroBufferedTransactionRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroSubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.ApplicationFormRepository;
import se.tink.backend.common.repository.mysql.main.ApplicationRepository;
import se.tink.backend.common.repository.mysql.main.BooliEstimateRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.DeletedUserRepository;
import se.tink.backend.common.repository.mysql.main.DataExportRequestRepository;
import se.tink.backend.common.repository.mysql.main.DeviceConfigurationRepository;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.common.repository.mysql.main.FeedbackRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsContentRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.FraudItemRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2AuthorizationRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2WebHookRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.backend.common.repository.mysql.main.ProviderImageRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.SubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.SubscriptionTokenRepository;
import se.tink.backend.common.repository.mysql.main.UserAdvertiserIdRepository;
import se.tink.backend.common.repository.mysql.main.UserDemographicsRepository;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.common.repository.mysql.main.UserEventRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookFriendRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookProfileRepository;
import se.tink.backend.common.repository.mysql.main.UserForgotPasswordTokenRepository;
import se.tink.backend.common.repository.mysql.main.UserOriginRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserSessionRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.search.SearchProxy;
import se.tink.backend.consent.cache.ConsentCache;
import se.tink.backend.consent.dao.ConsentDAO;
import se.tink.backend.consent.repository.cassandra.ConsentRepository;
import se.tink.backend.consent.repository.cassandra.UserConsentRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Currency;
import se.tink.backend.core.Provider;
import se.tink.backend.guice.configuration.RepositoryModule;
import se.tink.backend.utils.ClearingNumberBankToProviderMapImpl;
import se.tink.backend.utils.ProviderImageMap;
import se.tink.libraries.metrics.MetricRegistry;

public class ExportUserDataRepositoryModule extends RepositoryModule {

    public ExportUserDataRepositoryModule(DatabaseConfiguration databaseConfiguration,
            DistributedDatabaseConfiguration distributedDatabaseConfiguration) {
        super(databaseConfiguration, distributedDatabaseConfiguration);
    }

    @Provides
    protected TransactionSearchIndex providerTransactionSearchIndex(MetricRegistry registry) {
        return new TransactionSearchIndex(SearchProxy.getInstance().getClient(), new ObjectMapper(), registry);
    }


    @Override
    protected void bindCentralizedDaos() {
        bind(CategoryDao.class).in(Scopes.SINGLETON);
        bind(DeviceConfigurationDao.class).in(Scopes.SINGLETON);
        bind(ProviderDao.class).in(Scopes.SINGLETON);
    }

    @Override
    protected void bindDistributedDaos() {
        bind(AccountDao.class).in(Scopes.SINGLETON);
        bind(ApplicationDAO.class).in(Scopes.SINGLETON);
        bind(InvestmentDao.class).in(Scopes.SINGLETON);
        bind(ConsentDAO.class).in(Scopes.SINGLETON);
        bind(LoanDAO.class).in(Scopes.SINGLETON);
        bind(ProductDAO.class).in(Scopes.SINGLETON);
        bind(TransactionDao.class).in(Scopes.SINGLETON);
        bind(TransactionRepository.class).to(TransactionRepositoryImpl.class);
    }

    @Override
    protected void bindCaches() {
        bind(ConsentCache.class).in(Scopes.SINGLETON);
    }

    @Override
    protected void bindRepositories() {
        // Bind repositories. Let's do it in alphabetical order
        bindSpringBean(AbnAmroBufferedAccountRepository.class);
        bindSpringBean(AbnAmroBufferedTransactionRepository.class);
        bindSpringBean(AbnAmroSubscriptionRepository.class);
        bindSpringBean(AccountBalanceHistoryRepository.class);
        bindSpringBean(AccountRepository.class);
        bindSpringBean(ApplicationEventRepository.class);
        bindSpringBean(ApplicationFormEventRepository.class);
        bindSpringBean(ApplicationFormRepository.class);
        bindSpringBean(ApplicationRepository.class);
        bindSpringBean(BooliEstimateRepository.class);
        bindSpringBean(CassandraPeriodByUserIdRepository.class);
        bindSpringBean(CassandraTransactionDeletedRepository.class);
        bindSpringBean(CassandraTransactionByUserIdAndPeriodRepository.class);
        bindSpringBean(CategoryRepository.class);
        bindSpringBean(CheckpointRepository.class);
        bindSpringBean(ConsentRepository.class);
        bindSpringBean(CredentialsEventRepository.class);
        bindSpringBean(CredentialsRepository.class);
        bindSpringBean(CurrencyRepository.class);
        bindSpringBean(DeletedUserRepository.class);
        bindSpringBean(DataExportFragmentsRepository.class);
        bindSpringBean(DataExportsRepository.class);
        bindSpringBean(DataExportRequestRepository.class);
        bindSpringBean(DeviceConfigurationRepository.class);
        bindSpringBean(DeviceRepository.class);
        bindSpringBean(DocumentRepository.class);
        bindSpringBean(EventRepository.class);
        bindSpringBean(ExternallyDeletedTransactionRepository.class);
        bindSpringBean(FeedbackRepository.class);
        bindSpringBean(FollowItemRepository.class);
        bindSpringBean(FraudDetailsContentRepository.class);
        bindSpringBean(FraudDetailsRepository.class);
        bindSpringBean(FraudItemRepository.class);
        bindSpringBean(GiroRepository.class);
        bindSpringBean(InstrumentRepository.class);
        bindSpringBean(InstrumentHistoryRepository.class);
        bindSpringBean(LoanDataRepository.class);
        bindSpringBean(LoanDetailsRepository.class);
        bindSpringBean(MarketRepository.class);
        bindSpringBean(MerchantRepository.class);
        bindSpringBean(MerchantWizardSkippedTransactionRepository.class);
        bindSpringBean(OAuth2AuthorizationRepository.class);
        bindSpringBean(OAuth2ClientEventRepository.class);
        bindSpringBean(OAuth2ClientRepository.class);
        bindSpringBean(OAuth2WebHookRepository.class);
        bindSpringBean(PortfolioRepository.class);
        bindSpringBean(PortfolioHistoryRepository.class);
        bindSpringBean(PostalCodeAreaRepository.class);
        bindSpringBean(ProductFilterRepository.class);
        bindSpringBean(ProductInstanceRepository.class);
        bindSpringBean(ProductTemplateRepository.class);
        bindSpringBean(PropertyRepository.class);
        bindSpringBean(ProviderImageRepository.class);
        bindSpringBean(ProviderRepository.class);
        bindSpringBean(SignableOperationRepository.class);
        bindSpringBean(SubscriptionRepository.class);
        bindSpringBean(SubscriptionTokenRepository.class);
        bindSpringBean(TrackingEventRepository.class);
        bindSpringBean(TrackingSessionRepository.class);
        bindSpringBean(TrackingTimingRepository.class);
        bindSpringBean(TrackingViewRepository.class);
        bindSpringBean(CategoryChangeRecordRepository.class);
        bindSpringBean(TransactionExternalIdRepository.class);
        bindSpringBean(TransferDestinationPatternRepository.class);
        bindSpringBean(TransferEventRepository.class);
        bindSpringBean(TransferRepository.class);
        bindSpringBean(UserAdvertiserIdRepository.class);
        bindSpringBean(UserConsentRepository.class);
        bindSpringBean(UserDemographicsRepository.class);
        bindSpringBean(UserDeviceRepository.class);
        bindSpringBean(UserEventRepository.class);
        bindSpringBean(UserFacebookFriendRepository.class);
        bindSpringBean(UserFacebookProfileRepository.class);
        bindSpringBean(UserForgotPasswordTokenRepository.class);
        bindSpringBean(UserLocationRepository.class);
        bindSpringBean(UserOriginRepository.class);
        bindSpringBean(UserProfileDataRepository.class);
        bindSpringBean(UserRepository.class);
        bindSpringBean(UserSessionRepository.class);
        bindSpringBean(UserStateRepository.class);
        bindSpringBean(UserTransferDestinationRepository.class);
    }

    @Provides
    @Singleton
    public Supplier<ProviderImageMap> provideImageMapSupplier(final ProviderImageRepository providerImageRepository) {
        return Suppliers.memoizeWithExpiration(() -> new ProviderImageMap(
                providerImageRepository.findAll(),
                new ClearingNumberBankToProviderMapImpl()), 30, TimeUnit.MINUTES);
    }

    @Provides
    @Singleton
    public ProviderImageMap provideProviderImageMap(Supplier<ProviderImageMap> providerImageMapSupplier) {
        return providerImageMapSupplier.get();
    }

    @Provides
    @Singleton
    public ImmutableMap<String, Currency> provideCurrenciesByCode(CurrencyRepository currencyRepository) {
        return Maps.uniqueIndex(currencyRepository.findAll(), Currency::getCode);
    }

    @Provides
    @Singleton
    @Named("categoryCodeById")
    public BiMap<String, String> provideCategoryCodesById(CategoryRepository categoryRepository) {
        return ImmutableBiMap.copyOf(categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getCode)));
    }

    @Provides
    @Singleton
    public Map<String, Provider> provideProvidersByName(ProviderDao providerDao) {
        return providerDao.getProvidersByName();
    }

    @Provides
    @Singleton
    public ClusterCategories providesClusterCategories(CategoryRepository categoryRepository) {
        return new ClusterCategories(categoryRepository.findAll());
    }

}
