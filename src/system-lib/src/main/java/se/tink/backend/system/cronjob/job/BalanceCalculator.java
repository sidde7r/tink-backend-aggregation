package se.tink.backend.system.cronjob.job;

import com.google.common.util.concurrent.AtomicDouble;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import se.tink.backend.core.Account;
import se.tink.backend.core.CassandraTransaction;
import se.tink.backend.core.Transaction;
import se.tink.libraries.uuid.UUIDUtils;

public class BalanceCalculator {
    private static final double EPSILON = 0.1;

    private AtomicInteger usersWithWrongBalance = new AtomicInteger(0);
    private AtomicDouble totalBalanceDiff = new AtomicDouble(0);
    private AtomicDouble highestBalanceDiffGauge = new AtomicDouble(0);

    CalculateBalanceResult calculateBalance(List<Account> accounts, List<Transaction> transactions) {
        Map<String, List<Transaction>> transactionsByAccountId = transactions.stream().collect(
                Collectors.groupingBy(Transaction::getAccountId));

        for (Account account : accounts) {
            List<Transaction> transactionsForAccount = transactionsByAccountId
                    .get(account.getId());
            double diff = calculateDiff(account, transactionsForAccount);


            // If the balances differ more than an Epsilon value, we report this with some metrics.
            if (diff > EPSILON) {
                totalBalanceDiff.addAndGet(diff);
                usersWithWrongBalance.incrementAndGet();

                if (diff > highestBalanceDiffGauge.get()) {
                    highestBalanceDiffGauge.set(diff);
                }
            }
        }

        CalculateBalanceResult result = new CalculateBalanceResult();
        result.setHighestBalanceDiffGauge(highestBalanceDiffGauge.get());
        result.setTotalBalanceDiff(totalBalanceDiff.get());
        result.setUsersWithWrongBalance(usersWithWrongBalance.get());

        return result;
    }

    private double calculateDiff(Account account, List<Transaction> transactionsForAccount) {
        BigDecimal storedBalance = new BigDecimal(account.getBalance());
        BigDecimal calculatedBalance = new BigDecimal(0);

        // Add on all non-pending transaction amounts in history (both negative and positive) for this account.
        for (Transaction transaction : transactionsForAccount) {
            String transactionAccountId = transaction.getAccountId();

            if (Objects.equals(transactionAccountId, account.getId()) && !transaction.isPending()) {
                calculatedBalance = calculatedBalance.add(new BigDecimal(transaction.getOriginalAmount()));
            }
        }

        return Math.abs(storedBalance.subtract(calculatedBalance).doubleValue());
    }
}
