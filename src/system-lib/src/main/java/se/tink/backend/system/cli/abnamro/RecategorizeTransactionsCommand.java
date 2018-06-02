package se.tink.backend.system.cli.abnamro;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.categorization.api.AbnAmroCategories;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class RecategorizeTransactionsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(RecategorizeTransactionsCommand.class);
    
    private CategoryRepository categoryRepository;
    private TransactionDao transactionDao;
    private UserRepository userRepository;
    
    private ImmutableMap<String, Category> categoriesByCode;
    private ImmutableMap<String, Category> categoriesById;
    
    private long timeout = 24;
    private TimeUnit timeoutUnit = TimeUnit.HOURS;
    
    private static final List<KVPair<String, String>> RULES = ImmutableList
            .<KVPair<String, String>>builder()
            
            // home.mortgage
            .add(new KVPair<String, String>("HYPOTHEEK", AbnAmroCategories.Codes.EXPENSES_HOME_MORTGAGE))
            
            // home.communications
            .add(new KVPair<String, String>("CAIWAY", AbnAmroCategories.Codes.EXPENSES_HOME_COMMUNICATIONS))
            .add(new KVPair<String, String>("NETFLIX", AbnAmroCategories.Codes.EXPENSES_HOME_COMMUNICATIONS))
            .add(new KVPair<String, String>("SIMYO", AbnAmroCategories.Codes.EXPENSES_HOME_COMMUNICATIONS))
            .add(new KVPair<String, String>("UPC", AbnAmroCategories.Codes.EXPENSES_HOME_COMMUNICATIONS))
            .add(new KVPair<String, String>("HOLLANDSNIEUWE", AbnAmroCategories.Codes.EXPENSES_HOME_COMMUNICATIONS))
            .add(new KVPair<String, String>("EREDIVISIELIVE", AbnAmroCategories.Codes.EXPENSES_HOME_COMMUNICATIONS))
            .add(new KVPair<String, String>("SPOTIFY", AbnAmroCategories.Codes.EXPENSES_HOME_COMMUNICATIONS))
            
            // home.incurences-fees
            .add(new KVPair<String, String>("AEGON", AbnAmroCategories.Codes.EXPENSES_HOME_INCURENCES_FEES))
            .add(new KVPair<String, String>("ALLIANZ", AbnAmroCategories.Codes.EXPENSES_HOME_INCURENCES_FEES))
            .add(new KVPair<String, String>("DELA", AbnAmroCategories.Codes.EXPENSES_HOME_INCURENCES_FEES))
            .add(new KVPair<String, String>("INDEPENDER", AbnAmroCategories.Codes.EXPENSES_HOME_INCURENCES_FEES))
            .add(new KVPair<String, String>("MENZIS", AbnAmroCategories.Codes.EXPENSES_HOME_INCURENCES_FEES))
            .add(new KVPair<String, String>("NATIONALE[ -]?NEDERLANDEN", AbnAmroCategories.Codes.EXPENSES_HOME_INCURENCES_FEES))
            .add(new KVPair<String, String>("VERZEKER", AbnAmroCategories.Codes.EXPENSES_HOME_INCURENCES_FEES))
            .add(new KVPair<String, String>("VERZEKERING", AbnAmroCategories.Codes.EXPENSES_HOME_INCURENCES_FEES))
            .add(new KVPair<String, String>("VERZEKERINGEN", AbnAmroCategories.Codes.EXPENSES_HOME_INCURENCES_FEES))
            .add(new KVPair<String, String>("ZILVEREN KRUIS", AbnAmroCategories.Codes.EXPENSES_HOME_INCURENCES_FEES))
            
            // home.services
            .add(new KVPair<String, String>("VVE", AbnAmroCategories.Codes.EXPENSES_HOME_SERVICES))
            
            // home.taxes
            .add(new KVPair<String, String>("BELASTING", AbnAmroCategories.Codes.EXPENSES_HOME_TAXES))
            .add(new KVPair<String, String>("BELASTINGDIENST", AbnAmroCategories.Codes.EXPENSES_HOME_TAXES))
            .add(new KVPair<String, String>("BELASTINGEN", AbnAmroCategories.Codes.EXPENSES_HOME_TAXES))
            .add(new KVPair<String, String>("GEMEENTE", AbnAmroCategories.Codes.EXPENSES_HOME_TAXES))
            
            // house.repairs
            .add(new KVPair<String, String>("BOUWMAAT", AbnAmroCategories.Codes.EXPENSES_HOUSE_REPAIRS))
            .add(new KVPair<String, String>("BOUWMARKT", AbnAmroCategories.Codes.EXPENSES_HOUSE_REPAIRS))
            .add(new KVPair<String, String>("FORMIDO", AbnAmroCategories.Codes.EXPENSES_HOUSE_REPAIRS))
            .add(new KVPair<String, String>("GAMMA", AbnAmroCategories.Codes.EXPENSES_HOUSE_REPAIRS))
            .add(new KVPair<String, String>("HUBO", AbnAmroCategories.Codes.EXPENSES_HOUSE_REPAIRS))
            .add(new KVPair<String, String>("KARWEI", AbnAmroCategories.Codes.EXPENSES_HOUSE_REPAIRS))
            .add(new KVPair<String, String>("PRAXIS", AbnAmroCategories.Codes.EXPENSES_HOUSE_REPAIRS))
            
            // food.groceries
            .add(new KVPair<String, String>("ALDI", AbnAmroCategories.Codes.EXPENSES_FOOD_GROCERIES))
            .add(new KVPair<String, String>("C1000", AbnAmroCategories.Codes.EXPENSES_FOOD_GROCERIES))
            .add(new KVPair<String, String>("SUPERMARKT", AbnAmroCategories.Codes.EXPENSES_FOOD_GROCERIES))
            .add(new KVPair<String, String>("BAKKER", AbnAmroCategories.Codes.EXPENSES_FOOD_GROCERIES))
            .add(new KVPair<String, String>("SLAGER", AbnAmroCategories.Codes.EXPENSES_FOOD_GROCERIES))
            .add(new KVPair<String, String>("HELLOFRESH", AbnAmroCategories.Codes.EXPENSES_FOOD_GROCERIES))
            
            // food.restaurants
            .add(new KVPair<String, String>("RESTAURANT", AbnAmroCategories.Codes.EXPENSES_FOOD_RESTAURANTS))
            .add(new KVPair<String, String>("RESTAURANTE", AbnAmroCategories.Codes.EXPENSES_FOOD_RESTAURANTS))
            .add(new KVPair<String, String>("RESTAURANTS", AbnAmroCategories.Codes.EXPENSES_FOOD_RESTAURANTS))
            .add(new KVPair<String, String>("SEPAY[ -]?COMBI FOOD", AbnAmroCategories.Codes.EXPENSES_FOOD_RESTAURANTS))
            .add(new KVPair<String, String>("TAKE[ -]?AWAY", AbnAmroCategories.Codes.EXPENSES_FOOD_RESTAURANTS))
            .add(new KVPair<String, String>("WOK", AbnAmroCategories.Codes.EXPENSES_FOOD_RESTAURANTS))
            
            // entertainment.culture
            .add(new KVPair<String, String>("EFTELING", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE))
            .add(new KVPair<String, String>("MONKEY TOWN", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE))
            .add(new KVPair<String, String>("MUSEUM", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE))
            .add(new KVPair<String, String>("PATHE", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE))
            .add(new KVPair<String, String>("TUNFUN", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE))
            .add(new KVPair<String, String>("WALIBI", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE))
            .add(new KVPair<String, String>("ZWEMBAD", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE))
            
            // entertainment.hobby
            .add(new KVPair<String, String>("HEALTHCITY", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY))
            .add(new KVPair<String, String>("NIKESTORE", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY))

            // entertainment.lotteries
            .add(new KVPair<String, String>("BANKGIRO", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_LOTTERIES))
            .add(new KVPair<String, String>("CASINO", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_LOTTERIES))
            .add(new KVPair<String, String>("LOTERIJ", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_LOTTERIES))
            .add(new KVPair<String, String>("STAATSLOTERIJ", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_LOTTERIES))
            .add(new KVPair<String, String>("VRIENDENLOTERIJ", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_LOTTERIES))
            .add(new KVPair<String, String>("UNIBET", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_LOTTERIES))

            // entertainment.accommodation
            .add(new KVPair<String, String>("CAMPING", AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_ACCOMMODATION))

            // shopping.books
            .add(new KVPair<String, String>("AKO", AbnAmroCategories.Codes.EXPENSES_SHOPPING_BOOKS))
            .add(new KVPair<String, String>("BRUNA", AbnAmroCategories.Codes.EXPENSES_SHOPPING_BOOKS))
            .add(new KVPair<String, String>("PAROOL", AbnAmroCategories.Codes.EXPENSES_SHOPPING_BOOKS))
            .add(new KVPair<String, String>("SANOMA", AbnAmroCategories.Codes.EXPENSES_SHOPPING_BOOKS))
            .add(new KVPair<String, String>("TELEGRAAF", AbnAmroCategories.Codes.EXPENSES_SHOPPING_BOOKS))
            .add(new KVPair<String, String>("VOLKSKRANT", AbnAmroCategories.Codes.EXPENSES_SHOPPING_BOOKS))
            
            // shopping.gifts
            .add(new KVPair<String, String>("GREETZ", AbnAmroCategories.Codes.EXPENSES_SHOPPING_GIFTS))
            .add(new KVPair<String, String>("HALLMARKS", AbnAmroCategories.Codes.EXPENSES_SHOPPING_GIFTS))
            
            // transport.car
            .add(new KVPair<String, String>("PARKEREN", AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))
            .add(new KVPair<String, String>("PARKING", AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))
            .add(new KVPair<String, String>("Q[ -]?PARK", AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))
            .add(new KVPair<String, String>("TANKSTATION", AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))

            // transport.flights
            .add(new KVPair<String, String>("RYANAIR", AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .add(new KVPair<String, String>("EASYJET", AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .add(new KVPair<String, String>("LUFTHANSA", AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .add(new KVPair<String, String>("KLM", AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .add(new KVPair<String, String>("TRANSAVIA", AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))

            // wellness.beauty
            .add(new KVPair<String, String>("HAIR", AbnAmroCategories.Codes.EXPENSES_WELLNESS_BEAUTY))
            .add(new KVPair<String, String>("KAPPER", AbnAmroCategories.Codes.EXPENSES_WELLNESS_BEAUTY))
            .add(new KVPair<String, String>("KAPSALON", AbnAmroCategories.Codes.EXPENSES_WELLNESS_BEAUTY))
            .add(new KVPair<String, String>("SUNDAYS", AbnAmroCategories.Codes.EXPENSES_WELLNESS_BEAUTY))
            
            // wellness.dailycare
            .add(new KVPair<String, String>("ETOS", AbnAmroCategories.Codes.EXPENSES_WELLNESS_DAILYCARE))
            .add(new KVPair<String, String>("KRUIDVAT", AbnAmroCategories.Codes.EXPENSES_WELLNESS_DAILYCARE))
            .add(new KVPair<String, String>("RITUALS", AbnAmroCategories.Codes.EXPENSES_WELLNESS_DAILYCARE))
            .add(new KVPair<String, String>("TREKPLEISTER", AbnAmroCategories.Codes.EXPENSES_WELLNESS_DAILYCARE))
            .add(new KVPair<String, String>("BOLDKING", AbnAmroCategories.Codes.EXPENSES_WELLNESS_DAILYCARE))
            
            // misc.charity
            .add(new KVPair<String, String>("AMNESTY", AbnAmroCategories.Codes.EXPENSES_MISC_CHARITY))
            .add(new KVPair<String, String>("KWF", AbnAmroCategories.Codes.EXPENSES_MISC_CHARITY))
            .add(new KVPair<String, String>("WERELD NATUUR FONDS", AbnAmroCategories.Codes.EXPENSES_MISC_CHARITY))

            // misc.education
            .add(new KVPair<String, String>("DIENST UITVOERING", AbnAmroCategories.Codes.EXPENSES_MISC_EDUCATION))

            // misc.kids
            .add(new KVPair<String, String>("BART SMIT", AbnAmroCategories.Codes.EXPENSES_MISC_KIDS))
            .add(new KVPair<String, String>("INTERTOYS", AbnAmroCategories.Codes.EXPENSES_MISC_KIDS))
            .add(new KVPair<String, String>("KINDEROPVANG", AbnAmroCategories.Codes.EXPENSES_MISC_KIDS))
            
            // misc.uncategorized
            .add(new KVPair<String, String>("CLICKANDBUY INTERNATIONAL LTD", AbnAmroCategories.Codes.EXPENSES_MISC_UNCATEGORIZED))
            .add(new KVPair<String, String>("EUROPE S.A.R.L. ET CIE S.C.A", AbnAmroCategories.Codes.EXPENSES_MISC_UNCATEGORIZED))
            .add(new KVPair<String, String>("GLOBAL.?COLLECT", AbnAmroCategories.Codes.EXPENSES_MISC_UNCATEGORIZED))
            .add(new KVPair<String, String>("INT CARD SERVICES", AbnAmroCategories.Codes.EXPENSES_MISC_UNCATEGORIZED))
            .add(new KVPair<String, String>("STG ADYEN", AbnAmroCategories.Codes.EXPENSES_MISC_UNCATEGORIZED))
            .add(new KVPair<String, String>("STICHTING DERDENGELDEN BUC KAROO", AbnAmroCategories.Codes.EXPENSES_MISC_UNCATEGORIZED))

            // This should according to Lizzy be lotteries since it almost always is used by different gambling
            // companies. I think don't think that we should do that assumption since it is a payment provider
            // so leaving it like this. /EP
            .add(new KVPair<String, String>("WORLDPAY AP LTD", AbnAmroCategories.Codes.EXPENSES_MISC_UNCATEGORIZED))
            .build();
    
    private static final List<KVPair<Pattern, String>> PATTERNS;
    
    static {

        ImmutableList.Builder<KVPair<Pattern, String>> patterns = ImmutableList.builder();

        for (KVPair<String, String> rule : RULES) {
            patterns.add(new KVPair<Pattern, String>(Pattern.compile(rule.getKey(), Pattern.CASE_INSENSITIVE),
                    rule.getValue()));
        }

        PATTERNS = patterns.build();
    }
    
    public RecategorizeTransactionsCommand() {
        super("recategorize-transactions", "Recategorize transactions for ABN AMRO users.");
    }
    
    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext) throws Exception {

        log.info("Recategorize transactions for ABN AMRO users.");
        
        if (!Objects.equal(configuration.getCluster(), Cluster.ABNAMRO)) {
            log.error("This command is only enabled in the ABN AMRO cluster.");
            return;
        }
        
        final boolean dryRun = Boolean.getBoolean("dryRun");
        if (dryRun) {
            log.info("NB! This is just a dry run. No changes will be persisted.");
        }
        
        String users = System.getProperty("users");
        
        if (Strings.isNullOrEmpty(users)) {
            log.error(
                    "You need to specify which users to re-sync. Either `users=all` or `users=<comma separated list of user ids>`.");
            return;
        }
        
        categoryRepository = serviceContext.getRepository(CategoryRepository.class);

        categoriesByCode = Maps.uniqueIndex(categoryRepository.findAll(), Category::getCode);

        categoriesById = Maps.uniqueIndex(categoryRepository.findAll(), Category::getId);
        
        transactionDao = serviceContext.getDao(TransactionDao.class);
        userRepository = serviceContext.getRepository(UserRepository.class);
        
        final AtomicInteger changedCount = new AtomicInteger();
        final ExecutorService executor = Executors.newFixedThreadPool(Integer.getInteger("poolSize", 10));
        
        if ("all".equalsIgnoreCase(users)) {
            userRepository.streamAll().forEach(user -> executor.execute(() -> process(user, dryRun, changedCount)));
        } else {
            Iterable<String> userIds = Splitter.on(',').split(users);
            for (String userId : userIds) {
                final User user = userRepository.findOne(userId);
                if (user == null) {
                    log.warn(userId, "User could not be found. Skipping.");
                    continue;
                }

                executor.execute(() -> process(user, dryRun, changedCount));
            }
        }
        
        executor.shutdown();
        executor.awaitTermination(timeout, timeoutUnit);

        log.info(String.format("Done! Changed category for %d transactions.", changedCount.get()));
        if (dryRun) {
            log.info("NB! This was just a dry run. No changes were persisted.");
        }
    }
    
    private String getDescription(Transaction transaction) {
        
        String description = transaction.getFormattedDescription();
        
        if (Strings.isNullOrEmpty(description)) {
            description = transaction.getOriginalDescription();
        }
        
        return description;
    }

    private void migrate(Transaction transaction, String toCode) {
        transaction.setCategory(categoriesByCode.get(toCode));
    }
    
    private void process(User user, boolean dryRun, AtomicInteger changedCount) {
        List<Transaction> transactions = transactionDao.findAllByUserId(user.getId());
        process(user, transactions, dryRun, changedCount);
    }
    
    private void process(User user, List<Transaction> transactions, boolean dryRun, AtomicInteger changedCount) {
        
        List<Transaction> changedTransactions = Lists.newArrayList();
        
        for (Transaction transaction : transactions) {
            if (process(transaction)) {
                changedTransactions.add(transaction);
                changedCount.incrementAndGet();
            }
        }
        
        if (!dryRun && !changedTransactions.isEmpty()) {
            transactionDao.saveAndIndex(user, changedTransactions, true);
        }
    }
    
    private boolean process(Transaction transaction) {
        
        // Only process expenses.
        if (!Objects.equal(CategoryTypes.EXPENSES, transaction.getCategoryType())) {
            return false;
        }
        
        final String originalCategoryId = transaction.getCategoryId(); 
        final String description = getDescription(transaction);
        
        if (qualify(transaction, AbnAmroCategories.Codes.EXPENSES_SHOPPING_HOBBY)) {
            migrate(transaction, AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY);
            
        } else if (qualify(transaction, AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_SPORT)) {
            migrate(transaction, AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY);

        } else if (qualify(transaction, AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_VACATION)) {
            if (Math.abs(transaction.getOriginalAmount()) < 20 && !transaction.isUserModifiedCategory()) {
                migrate(transaction, AbnAmroCategories.Codes.EXPENSES_FOOD_COFFEE);
            } else {
                migrate(transaction, AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_ACCOMMODATION);
            }

        } else if (qualify(transaction, AbnAmroCategories.Codes.EXPENSES_WELLNESS_PHARMACY)) {
            migrate(transaction, AbnAmroCategories.Codes.EXPENSES_WELLNESS_HEALTHCARE);
        }
        
        if (!transaction.isUserModifiedCategory() && !Strings.isNullOrEmpty(description)) {
            for (KVPair<Pattern, String> rule : PATTERNS) {
                if (qualify(description, rule.getKey())) {
                    migrate(transaction, rule.getValue());
                    break;
                }
            }
        }
        
        if (Objects.equal(originalCategoryId, transaction.getCategoryId())) {
            return false;
        } else {
            final Category originalCategory = categoriesById.get(originalCategoryId);
            final Category newCategory = categoriesById.get(transaction.getCategoryId());

            log.debug(transaction.getUserId(), transaction.getCredentialsId(), String.format(
                    "Changed category from `%s` to `%s` [\"%s\"].", originalCategory.getCode(), newCategory.getCode(),
                    description));
            
            return true;
        }
    }

    private boolean qualify(Transaction transaction, String categoryCode) {
        Category category = categoriesByCode.get(categoryCode);
        if (category == null) {
            return false;
        } else {
            return Objects.equal(transaction.getCategoryId(), category.getId());
        }
    }
    
    private boolean qualify(String description, Pattern pattern) {
        return pattern.matcher(description).find();
    }
}
