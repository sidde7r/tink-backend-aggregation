package se.tink.backend.system.guice;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.repository.cassandra.CassandraTransactionDeletedRepository;
import se.tink.backend.common.repository.cassandra.CategoryChangeRecordRepository;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.elasticsearch.TransactionSearchIndex;
import se.tink.backend.common.dao.transactions.TransactionRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.GiroRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.core.ProviderTypes;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.uuid.UUIDUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestRepositoriesModule extends AbstractModule {

    @Override
    protected void configure() {
        TransactionRepository transactionByUserIdAndPeriodRepository = mock(TransactionRepository.class);
        TransactionSearchIndex transactionSearchIndex = mock(TransactionSearchIndex.class);
        CategoryChangeRecordRepository categoryChangeRecordRepository = mock(CategoryChangeRecordRepository.class);
        CassandraTransactionDeletedRepository cassandraTransactionDeletedRepository = mock(CassandraTransactionDeletedRepository.class);
        ProviderDao providerDao = mock(ProviderDao.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        CredentialsRepository credentialsRepository = mock(CredentialsRepository.class);
        LoanDataRepository loanDataRepository = mock(LoanDataRepository.class);
        AccountRepository accountRepository = mock(AccountRepository.class);
        MerchantRepository merchantRepository = mock(MerchantRepository.class);
        GiroRepository giroRepository = mock(GiroRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        LoanDAO loanDAO = mock(LoanDAO.class);
        CategoryChangeRecordDao categoryChangeRecordDao = mock(CategoryChangeRecordDao.class);
        PostalCodeAreaRepository postalCodeAreaRepository = mock(PostalCodeAreaRepository.class);

        SECategories seCategories = new SECategories();

        ImmutableList<String> allExpensesCategoryCodes = ImmutableList.of(seCategories.getAlcoholTobaccoCode(), seCategories.getBarsCode(),
                seCategories.getClothesCode(), seCategories.getCoffeeCode(), seCategories.getElectronicsCode()
                , seCategories.getExcludeCode(), seCategories.getExpenseUnknownCode(), seCategories.getFoodOtherCode(),
                seCategories.getGroceriesCode(), seCategories.getMortgageCode(),
                seCategories.getRentCode(), seCategories.getRestaurantsCode(), seCategories.getRefundCode(),
                seCategories.getSalaryCode(), seCategories.getSavingsCode(), seCategories.getServicesCode(),
                seCategories.getVacationCode(), seCategories.getWithdrawalsCode());
        List<Category> categories = allExpensesCategoryCodes.stream().map(sc -> {
            Category c = new Category();
            c.setId(UUIDUtils.generateUUID());
            c.setCode(sc);
            c.setType(CategoryTypes.EXPENSES);
            return c;
        }).collect(Collectors.toList());

        // basic mocking behaviour
        Category unknownTransferCategory = new Category();
        unknownTransferCategory.setCode(SECategories.Codes.TRANSFERS_OTHER_OTHER);
        unknownTransferCategory.setType(CategoryTypes.TRANSFERS);
        unknownTransferCategory.setId(UUIDUtils.generateUUID());
        categories.add(unknownTransferCategory);

        Category unknownIncomeCategory = new Category();
        unknownIncomeCategory .setCode(SECategories.Codes.INCOME_OTHER_OTHER);
        unknownIncomeCategory .setType(CategoryTypes.INCOME);
        unknownIncomeCategory .setId(UUIDUtils.generateUUID());
        categories.add(unknownIncomeCategory);

        Category barsCategory = categories.stream().filter(c -> c.getCode().equals(SECategories.Codes.EXPENSES_FOOD_BARS)).findFirst().get();
        Map<String, Category> categoriesById = categories.stream().collect(Collectors.toMap(c -> c.getId(), c -> c));
        when(categoryRepository.findById(anyString())).thenReturn(barsCategory);
        when(categoryRepository.findByCode(SECategories.Codes.EXPENSES_FOOD_BARS)).thenReturn(barsCategory);
        when(categoryRepository.findLeafCategories()).thenReturn(ImmutableList.of(barsCategory));
        when(categoryRepository.getCategoriesById("sv_SE")).thenReturn(categoriesById);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(loanDAO.saveIfUpdated(any(Loan.class))).thenReturn(true);

        Provider provider = new Provider();
        provider.setName("swedbank-bankid");
        provider.setDisplayName("Swedbank");
        provider.setGroupDisplayName("Swedbank och Sparbankerna");
        provider.setStatus(ProviderStatuses.ENABLED);
        provider.setType(ProviderTypes.BANK);
        provider.setMarket("SE");
        when(providerDao.getProvidersByName()).thenReturn(ImmutableMap.of("swedbank-bankid", provider));
        ImmutableList<String> cities = ImmutableList.of("Stockholm", "Göteborg", "Malmö", "Linköping", "Jönköping", "Uppsala", "Örebro", "Falköping", "Västerås", "Skellefteå");
        when(postalCodeAreaRepository.findAllCities()).thenReturn(cities);

        // guice binding
        bind(Cluster.class).toInstance(Cluster.TINK);
        bind(TransactionRepository.class).toProvider(Providers.of(transactionByUserIdAndPeriodRepository));
        bind(CategoryChangeRecordRepository.class).toProvider(Providers.of(categoryChangeRecordRepository));
        bind(TransactionSearchIndex.class).toProvider(Providers.of(transactionSearchIndex));
        bind(CassandraTransactionDeletedRepository.class).toProvider(Providers.of(cassandraTransactionDeletedRepository));
        bind(ProviderDao.class).toProvider(Providers.of(providerDao));
        bind(CategoryRepository.class).toProvider(Providers.of(categoryRepository));
        bind(CredentialsRepository.class).toProvider(Providers.of(credentialsRepository));
        bind(LoanDataRepository.class).toProvider(Providers.of(loanDataRepository));
        bind(LoanDAO.class).toProvider((Providers.of(loanDAO)));
        bind(AccountRepository.class).toProvider(Providers.of(accountRepository));
        bind(GiroRepository.class).toProvider(Providers.of(giroRepository));
        bind(MerchantRepository.class).toProvider(Providers.of(merchantRepository));
        bind(UserRepository.class).toProvider(Providers.of(userRepository));
        bind(CategorizationConfiguration.class).toInstance(new CategorizationConfiguration());
        bind(CategoryConfiguration.class).toInstance(new SECategories());
        bind(CategoryChangeRecordDao.class).toProvider(Providers.of(categoryChangeRecordDao));
        bind(PostalCodeAreaRepository.class).toProvider(Providers.of(postalCodeAreaRepository));
    }
}
