package se.tink.backend.system.cli.abnamro;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicLongMap;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.abnamro.utils.AbnAmroIcsCredentials;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.backend.categorization.MerchantCategoryMatcher;
import se.tink.backend.categorization.api.AbnAmroCategories;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.ThreadPoolObserverTransformer;
import se.tink.backend.utils.LogUtils;

/**
 * Command for evaluation ICS Credit cards.
 * <p>
 * Features:
 * - Calculate categorization statistics
 * - Print uncategorized transactions for ABN AMRO ICS credit cards
 */
public class IcsCreditCardsEvaluationCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(IcsCreditCardsEvaluationCommand.class);
    private ImmutableMap<String, Category> categoriesById;
    private TransactionDao transactionDao;
    private MerchantCategoryMatcher merchantCategoryMatcher;

    public IcsCreditCardsEvaluationCommand() {
        super("ics-categories-statistics", "Evaluation ABN AMRO ICS credit cards.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext)
            throws IOException {

        if (!Objects.equal(configuration.getCluster(), Cluster.ABNAMRO)) {
            log.error("This command is only enabled in the ABN AMRO cluster.");
            return;
        }

        CategoryRepository categoryRepository = serviceContext.getRepository(CategoryRepository.class);
        final CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        merchantCategoryMatcher = MerchantCategoryMatcher.builder(Cluster.ABNAMRO).build();

        categoriesById = FluentIterable
                .from(categoryRepository.findAll("en_US"))
                .uniqueIndex(Category::getId);

        final AtomicLongMap<String> merchantDescriptionCounter = AtomicLongMap.create();
        final AtomicLongMap<String> categoriesCounter = AtomicLongMap.create();

        userRepository.streamAll()
                .flatMapIterable(user -> credentialsRepository.findAllByUserId(user.getId()))
                .compose(ThreadPoolObserverTransformer.buildFromSystemPropertiesWithConcurrency(1).<Credentials>build())
                .forEach(credentials -> process(credentials, merchantDescriptionCounter, categoriesCounter));

        reportMerchantDescriptions(merchantDescriptionCounter);
        reportCategoryStatistics(categoriesCounter);
    }

    private void process(Credentials credentials, AtomicLongMap<String> merchantDescriptionStatistics,
            AtomicLongMap<String> categoriesCounter) {

        if (!Objects.equal(credentials.getProviderName(), AbnAmroIcsCredentials.ABN_AMRO_ICS_PROVIDER_NAME)) {
            return;
        }

        List<Transaction> transactions = getTransactions(credentials);

        log.info(credentials.getUserId(), String.format("Processing %d transactions...", transactions.size()));

        for (Transaction transaction : transactions) {
            String merchantDescription = transaction
                    .getInternalPayload(AbnAmroUtils.InternalPayloadKeys.MERCHANT_DESCRIPTION);

            if (!Strings.isNullOrEmpty(merchantDescription)) {
                merchantDescriptionStatistics.incrementAndGet(merchantDescription);
            }

            // Only count transactions that the user hasn't changed category on
            if (!transaction.isUserModifiedCategory()) {
                categoriesCounter.getAndIncrement(transaction.getCategoryId());
            }

            // Print details about uncategorized transactions to be able to improve the quality
            if (isUncategorizedCategory(categoriesById.get(transaction.getCategoryId()))) {
                log.info(credentials, "");
                log.info(credentials, String.format("Description: %s", transaction.getDescription()));
                log.info(credentials, String.format("Original Description: %s", transaction.getOriginalDescription()));
                log.info(credentials, String.format("Merchant Description: %s", merchantDescription));
            }
        }
    }

    private static boolean isUncategorizedCategory(Category category) {
        return Objects.equal(category.getCode(), AbnAmroCategories.Codes.EXPENSES_MISC_UNCATEGORIZED);
    }

    private void reportMerchantDescriptions(AtomicLongMap<String> merchantStatistics) {

        // Sort and split statistics into the descriptions that we have mapped and what we have not mapped

        SortedMap<String, Long> mapped = Maps.newTreeMap();
        SortedMap<String, Long> nonMapped = Maps.newTreeMap();

        for (Map.Entry<String, Long> entry : merchantStatistics.asMap().entrySet()) {
            // Test and see if we have mapped this merchant description or not
            if (merchantCategoryMatcher.findByDescription(entry.getKey()) == null) {
                nonMapped.put(entry.getKey(), entry.getValue());
            } else {
                mapped.put(entry.getKey(), entry.getValue());
            }
        }

        log.info("");
        log.info("===== Mapped Merchant description statistics (MCC codes) =====");
        for (Map.Entry<String, Long> entry : mapped.entrySet()) {
            log.info(String.format("%-30s\t%s", entry.getKey(), entry.getValue()));
        }

        log.info("");
        log.info("===== Non mapped Merchant description statistics (MCC codes) =====");
        for (Map.Entry<String, Long> entry : nonMapped.entrySet()) {
            log.info(String.format("%-30s\t%s", entry.getKey(), entry.getValue()));
        }

    }

    private void reportCategoryStatistics(AtomicLongMap<String> categoriesStatistics) {
        log.info("");
        log.info("===== Category statistics =====");
        for (Map.Entry<String, Long> entry : categoriesStatistics.asMap().entrySet()) {
            Category category = categoriesById.get(entry.getKey());

            log.info(String.format("%-15s %-30s %s", category.getType(), category.getDisplayName(), entry.getValue()));
        }

        log.info("Total number of transactions (category not modified by users): " + categoriesStatistics.sum());
    }

    private List<Transaction> getTransactions(final Credentials credentials) {

        List<Transaction> transactions = transactionDao.findAllByUserId(credentials.getUserId());

        return FluentIterable.from(transactions).filter(
                transaction -> Objects.equal(credentials.getId(), transaction.getCredentialsId())).toList();
    }
}

