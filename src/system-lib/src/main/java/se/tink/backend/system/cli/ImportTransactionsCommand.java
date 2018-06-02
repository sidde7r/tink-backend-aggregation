package se.tink.backend.system.cli;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.Market;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.utils.LogUtils;

public class ImportTransactionsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private class Column {
        private static final String CATEGORY_CODE = "categoryCode";
        private static final String DESCRIPTION = "description";
        private static final String USER_MODIFIED_CATEGORY = "userModifiedCategory";
    }
    
    private static final CSVFormat CSV_FORMAT = CSVFormat
            .newFormat('\t')
            .withHeader(Column.CATEGORY_CODE, Column.USER_MODIFIED_CATEGORY, Column.DESCRIPTION)
            .withRecordSeparator('\n')
            .withNullString("NULL");
    
    private final static LogUtils log = new LogUtils(ImportTransactionsCommand.class);
    
    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    private CredentialsRepository credentialsRepository;
    private TransactionDao transactionDao;
    private MarketRepository marketRepository;
    private UserRepository userRepository;
    
    private ImmutableMap<String, Category> categoriesByCode;
    
    public ImportTransactionsCommand() {
        super("import-transactions", "Import transactions from CSV file without processing them.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        if (!configuration.isDevelopmentMode()) {
            System.err.println("Development command, please run locally");
            return;
        }

        accountRepository = serviceContext.getRepository(AccountRepository.class);
        categoryRepository = serviceContext.getRepository(CategoryRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        marketRepository = serviceContext.getRepository(MarketRepository.class);
        userRepository = serviceContext.getRepository(UserRepository.class);
        
        final String input = System.getProperty("input");
        final boolean dryRun = Boolean.getBoolean("dryRun");
        if (dryRun)
            log.info("NB! This is just a dry run. No transactions will be persisted.");
        
        UserData userData = createUser();
        
        loadCategories();
        importTransactions(userData, input, dryRun);
    }

    private UserData createUser() {
   
        User user = userRepository.findOneByUsername("dummy-user");
        
        if (user == null) {
            user = new User();
            user.setFlags(Lists.newArrayList(FeatureFlags.TINK_TEST_ACCOUNT));
            user.setProfile(UserProfile.createDefault(getDefaultMarket()));
            user.setUsername("dummy-user");
            
            userRepository.save(user);
            
            Credentials credentials = new Credentials();
            credentials.setProviderName("dummy-provider");
            credentials.setStatus(CredentialsStatus.UPDATED);
            credentials.setUserId(user.getId());
            
            credentialsRepository.save(credentials);
            
            Account account = new Account();
            account.setCredentialsId(credentials.getId());
            account.setUserId(user.getId());
            
            accountRepository.save(account);
        }
        
        UserData userData = new UserData();
        userData.setAccounts(accountRepository.findByUserId(user.getId()));
        userData.setCredentials(credentialsRepository.findAllByUserId(user.getId()));
        userData.setUser(user);
        
        return userData;
    }

    private Market getDefaultMarket() {
        return Iterables.find(marketRepository.findAll(), Market::isDefaultMarket);
    }
    
    private void loadCategories() {
        categoriesByCode = Maps.uniqueIndex(categoryRepository.findAll(), Category::getCode);
    }

    private void importTransactions(UserData userData, String filename, boolean dryRun) throws FileNotFoundException,
            IOException {
        
        Account account = userData.getAccounts().get(0);
        String accountId = account.getId();
        String credentialsId = account.getCredentialsId();
        String userId = account.getUserId();
        
        List<Transaction> buffer = Lists.newArrayList();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),
                StandardCharsets.UTF_8))) {
            
            for (String line; (line = br.readLine()) != null;) {
                CSVParser parser = CSVParser.parse(line, CSV_FORMAT);
                CSVRecord records = parser.getRecords().get(0);
                
                String categoryCode = records.get(Column.CATEGORY_CODE);
                Category category = categoriesByCode.get(categoryCode);
                
                if (category == null) {
                    log.error(String.format("Unable to find category for code '%s'. Skipping.", categoryCode));
                    continue;
                }
                
                Transaction transaction = new Transaction();
                
                transaction.setAccountId(accountId);
                transaction.setAmount(Objects.equal(category.getType(), CategoryTypes.INCOME) ? 1. : -1);
                transaction.setCategory(category);
                transaction.setCredentialsId(credentialsId);
                transaction.setDate(new Date());
                transaction.setDescription(records.get(Column.DESCRIPTION));
                transaction.setFormattedDescription(transaction.getDescription());
                transaction.setOriginalAmount(transaction.getAmount());
                transaction.setOriginalDate(transaction.getDate());
                transaction.setOriginalDescription(transaction.getDescription());
                transaction.setType(TransactionTypes.DEFAULT);
                transaction.setUserId(userId);
                transaction.setUserModifiedCategory(Boolean.valueOf(records.get(Column.USER_MODIFIED_CATEGORY)));
                
                buffer.add(transaction);
                
                if (buffer.size() >= 5000) {
                    storeAndRelease(userData.getUser(), buffer, dryRun);
                }
            }
            
            storeAndRelease(userData.getUser(), buffer, dryRun);
        }
    }
    
    private void storeAndRelease(User user, List<Transaction> buffer, boolean dryRun) {
        if (buffer.isEmpty()) {
            return;
        }
        
        if (!dryRun) {
            transactionDao.save(user, buffer);
        }
        
        buffer.clear();
    }
}
