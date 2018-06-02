package se.tink.backend.system.cronjob.job;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.connector.rpc.PartnerAccountPayload;
import se.tink.backend.core.Account;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.helper.traversal.UserSampleFilter;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * Calculates the correct balance by going through all non-pending transactions and adding their amounts.
 * If the calculated balance differs from what is in store, it logs an error about this. This is useful
 * since some partners want us to calculate balance for them on each ingestion of transactions. If we do something wrong
 * or have a race condition etc., we want to know this.
 * <p>
 * This calculator only cares about non-pending transactions for now.
 */
public class BalanceCalculatorJob {
    private static final LogUtils log = new LogUtils(BalanceCalculatorJob.class);

    private static final MetricId METRIC_PREFIX = MetricId.newId("cronjob_calculate_balance");
    private static final MetricId SAMPLED = METRIC_PREFIX.suffix("sampled");
    private static final MetricId WRONG_BALANCE = METRIC_PREFIX.suffix("wrong_balance");
    private static final MetricId AVERAGE_DIFF = METRIC_PREFIX.suffix("average_diff");
    private static final MetricId HIGHEST_DIFF = METRIC_PREFIX.suffix("highest_diff");

    private final Counter sampleSizeCounter;

    private TransactionDao transactionDao;
    private AccountRepository accountRepository;
    private UserRepository userRepository;
    private MetricRegistry metricRegistry;
    private BalanceCalculator balanceCalculator;
    private CalculateBalanceResult result;

    @Inject
    public BalanceCalculatorJob(TransactionDao transactionDao,
            AccountRepository accountRepository, UserRepository userRepository, MetricRegistry metricRegistry,
            BalanceCalculator balanceCalculator) {
        this.transactionDao = transactionDao;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.metricRegistry = metricRegistry;
        this.balanceCalculator = balanceCalculator;

        sampleSizeCounter = metricRegistry.meter(SAMPLED);
    }

    public void run() throws Exception {
        log.info("Starting calculate balance job");

        userRepository.streamAll()
                .filter(new UserSampleFilter(userRepository))
                .forEach(this::calculateAndSetBalance);

        metricRegistry.meter(WRONG_BALANCE).inc(result.getUsersWithWrongBalance());
        metricRegistry.lastUpdateGauge(AVERAGE_DIFF)
                .update(result.getTotalBalanceDiff() / result.getUsersWithWrongBalance());
        metricRegistry.lastUpdateGauge(HIGHEST_DIFF).update(result.getHighestBalanceDiffGauge());

        log.info("Done with calculate balance job");
    }

    private void calculateAndSetBalance(User user) {
        List<Account> accounts = accountRepository.findByUserId(user.getId());
        List<Transaction> transactions = transactionDao.findAllByUserId(user.getId());

        if (accounts == null || accounts.isEmpty() || transactions == null || transactions.isEmpty()) {
            return;
        }

        accounts = accounts.stream().filter(this::isCalculateBalanceAccount).collect(Collectors.toList());

        if (accounts.isEmpty()) {
            return;
        }

        sampleSizeCounter.inc();

        result = balanceCalculator.calculateBalance(accounts, transactions);
    }

    private boolean isCalculateBalanceAccount(Account account) {
        String payloadSerialized = account.getPayload(Account.PayloadKeys.PARTNER_PAYLOAD);
        if (Strings.isNullOrEmpty(payloadSerialized)) {
            return false;
        }

        PartnerAccountPayload payload = SerializationUtils
                .deserializeFromString(payloadSerialized, PartnerAccountPayload.class);

        return payload != null && payload.isCalculateBalance();
    }
}
