package se.tink.backend.system.cli.merchants;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.merchants.MerchantDuplicateFinder;
import se.tink.backend.common.merchants.MerchantDuplicateResult;
import se.tink.backend.common.merchants.MerchantElasticSearchUtils;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class ImproveMerchantQualityCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("merchant-improve-quality-thread-%d")
            .build();
    private static LogUtils log = new LogUtils(ImproveMerchantQualityCommand.class);

    public ImproveMerchantQualityCommand() {
        super("merchant-improve-quality", "Improve the quality on merchants.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        // Load configs
        final int threadPoolSize = Integer.getInteger("threadPoolSize", 1);
        final int merchantLimit = Integer.getInteger("merchantLimit", Integer.MAX_VALUE);
        final boolean dryRun = Boolean.getBoolean("dryRun");
        final boolean enableRemoveMerchants = Boolean.getBoolean("enableRemoveMerchants");

        log.info("Config threadPoolSize: " + threadPoolSize);
        log.info("Config merchantLimit: " + merchantLimit);
        log.info("Config enableRemoveMerchants: " + enableRemoveMerchants);
        log.info("Config dryRun: " + dryRun);

        run(serviceContext, threadPoolSize, merchantLimit, dryRun, enableRemoveMerchants);
    }

    public void run(ServiceContext serviceContext, int threadPoolSize, int merchantLimit,
            boolean dryRun, boolean enableRemoveMerchants) throws Exception {

        MerchantRepository merchantRepository = serviceContext.getRepository(MerchantRepository.class);

        List<Merchant> merchants = Lists.newArrayList(Iterables.limit(merchantRepository.findAll(), merchantLimit));

        List<MerchantDuplicateResult> duplicates = new MerchantDuplicateFinder().findDuplicates(merchants);

        log.info("Duplicates: " + duplicates.size());

        List<String> duplicateIds = Lists
                .newArrayList(Iterables.transform(duplicates, input -> input.getDuplicate().getId()));

        // Lookup users that have the merchant(s) on one or several transactions
        Set<String> users = new MerchantElasticSearchUtils(serviceContext.getSearchClient())
                .findUsersWithMerchantOnTransactions(duplicateIds);

        // Update duplicates
        if (duplicates.size() > 0) {
            replaceDuplicateMerchants(serviceContext, duplicates, threadPoolSize, dryRun, users);

            if (enableRemoveMerchants) {
                removeReplacedMerchants(merchantRepository, duplicates, dryRun);
            }

        } else {
            log.info("No duplicates where found.");
        }
    }

    private void replaceDuplicateMerchants(ServiceContext serviceContext,
            final List<MerchantDuplicateResult> duplicates, int threadPoolSize, final Boolean dryRun,
            final Set<String> usersToProcess) throws Exception {

        // Repositories
        final TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);

        final ListenableThreadPoolExecutor<Runnable> executor = ListenableThreadPoolExecutor.builder(
                Queues.newLinkedBlockingQueue(),
                new TypedThreadPoolBuilder(threadPoolSize, threadFactory))
                .build();

        final AtomicLong progress = new AtomicLong();
        final AtomicLong totalTransactionsUpdated = new AtomicLong();

        logDuplicates(duplicates);

        for (final String userId : usersToProcess) {

            executor.execute(() -> {
                try {
                    log.info(userId, String.format("Processing user %s of %s...", progress.incrementAndGet(),
                            usersToProcess.size()));

                    int count = replaceMerchantsOnTransactions(userId, transactionDao,
                            duplicates, dryRun);

                    totalTransactionsUpdated.addAndGet(count);
                } catch (Exception e) {
                    log.error(userId, "Could not process user: " + userId, e);
                }
            });

        }

        log.info("Done submitting. Waiting for executor to finish.");

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        log.info("Total number of transactions changed: " + totalTransactionsUpdated.get());
    }

    /**
     * Replaces duplicate merchants on transactions
     */
    private int replaceMerchantsOnTransactions(String userId,
            TransactionDao transactionDao, List<MerchantDuplicateResult> duplicates, boolean dryRun) {

        List<Transaction> transactions = transactionDao.findAllByUserId(userId);
        List<Transaction> changes = Lists.newArrayList();

        for (Transaction t : transactions) {

            // Note: this loop doesn't handle all cases and can get wrong results when there are nested replacements
            // For example A replaced by B and B replaced by C and A replaced by C can get different results depending
            // on the order which they are executed

            for (MerchantDuplicateResult md : duplicates) {

                // Check if there are a duplicate that we should use instead
                if (md.getDuplicate().getId().equals(t.getMerchantId())) {

                    log.info(userId, String.format("Replacing merchant on transaction %s from %s to %s", t.getId(),
                            t.getMerchantId(), md.getReplacedBy().getId()));

                    t.setMerchantId(md.getReplacedBy().getId());
                    changes.add(t);
                }
            }
        }

        if (changes.size() > 0 && !dryRun) {
            log.info(userId, "Number of transactions saved and indexed: " + changes.size());
            transactionDao.saveAndIndex(userId, changes, true);
        }

        return changes.size();
    }

    private void logDuplicates(List<MerchantDuplicateResult> duplicates) {

        for (MerchantDuplicateResult row : duplicates) {

            Merchant duplicate = row.getDuplicate();
            Merchant replacedBy = row.getReplacedBy();

            log.info(String.format(
                    "Replacing merchant Id:[%s] Name:[%s] Address:[%s] with Id:[%s] Name:[%s] Address:[%s]",
                    duplicate.getId(), duplicate.getName(), duplicate.getFormattedAddress(), replacedBy.getId(),
                    replacedBy.getName(), replacedBy.getFormattedAddress()));

        }
    }

    private void removeReplacedMerchants(MerchantRepository merchantRepository,
            List<MerchantDuplicateResult> duplicates, boolean dryRun) {

        // Could be a case when we already have removed a merchant so keep track of them
        Set<String> removedMerchants = Sets.newHashSet();

        for (MerchantDuplicateResult row : duplicates) {

            Merchant duplicate = row.getDuplicate();

            if (removedMerchants.contains(duplicate.getId())) {
                log.info(String.format("Merchant with id %s has already been removed", duplicate.getId()));
            } else {
                if (!dryRun) {
                    removedMerchants.add(duplicate.getId());
                    merchantRepository.removeFromIndex(duplicate.getId());
                    merchantRepository.delete(duplicate.getId());
                }

                log.info(String.format("Removed merchant Id:[%s] Name:[%s] Address:[%s]", duplicate.getId(),
                        duplicate.getName(), duplicate.getFormattedAddress()));
            }
        }
    }
}
