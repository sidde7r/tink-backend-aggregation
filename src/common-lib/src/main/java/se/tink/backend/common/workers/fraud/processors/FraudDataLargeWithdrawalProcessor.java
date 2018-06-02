package se.tink.backend.common.workers.fraud.processors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import com.google.common.base.Objects;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.core.Category;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudTransactionContent;
import se.tink.backend.core.FraudTransactionEntity;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Orderings;

public class FraudDataLargeWithdrawalProcessor extends FraudDataProcessor {

    private static final int MIN_WARN_THRESHOLD = 99;
    private static final LogUtils log = new LogUtils(FraudDataLargeWithdrawalProcessor.class);
    
    @Override
    public void process(FraudDataProcessorContext context) {
        
        double minWarnThreshold = context.getUserCurrency().getFactor() * MIN_WARN_THRESHOLD;
        final Category withdrawalCategory = context.getCategoriesByCodeForLocale()
                .get(context.getCategoryConfiguration().getWithdrawalsCode());
        DescriptiveStatistics withdrawalStatistics = new DescriptiveStatistics();        
        
        // Filter withdrawal transactions for last week.
        
        final Date cutOffDate = DateUtils.addWeeks(new Date(), -1);

        Iterable<Transaction> withdrawalTransactions = Iterables.filter(context.getTransactionsById().values(),
                t -> Objects.equal(t.getCategoryId(), withdrawalCategory.getId()));

        log.info(
                context.getUser().getId(),
                "Withdrawal transaction size is "
                        + Iterables.size(withdrawalTransactions));

        List<FraudDetailsContent> transactionFraudDetails = Lists.newArrayList();

        for (Transaction transaction : Ordering.from(Orderings.TRANSACTION_DATE_ORDERING)
                .sortedCopy(withdrawalTransactions)) {

            double threshold = withdrawalStatistics.getMean() + (2 * withdrawalStatistics.getStandardDeviation());
            double amount = Math.abs(transaction.getAmount());

            if (withdrawalStatistics.getN() < 5) {
                threshold = minWarnThreshold;
            }

            withdrawalStatistics.addValue(Math.abs(transaction.getAmount()));
            
            if (transaction.getDate().before(cutOffDate)) {
                continue;
            }

            if (transaction.isUserModifiedCategory()) {
                continue;
            }
            
            // Check if this withdrawal is unusual large.

            if (amount <= threshold || amount <= minWarnThreshold) {
                continue;
            }

            FraudTransactionContent transactionContent = new FraudTransactionContent();
            transactionContent.setContentType(FraudDetailsContentType.LARGE_WITHDRAWAL);
            transactionContent.setTransactionIds(Lists.newArrayList(transaction.getId()));
            transactionContent.setTransactions(Lists.newArrayList(new FraudTransactionEntity(transaction)));
            transactionFraudDetails.add(transactionContent);
            
        }

        log.info(
                context.getUser().getId(),
                "Generated fraud details content size for withdrawal transactions is "
                        + Iterables.size(transactionFraudDetails));
        context.addFraudDetailsContent(transactionFraudDetails);
    }
}
