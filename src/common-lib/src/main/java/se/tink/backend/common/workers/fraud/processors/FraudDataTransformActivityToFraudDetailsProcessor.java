package se.tink.backend.common.workers.fraud.processors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.time.DateUtils;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudTransactionContent;
import se.tink.backend.core.FraudTransactionEntity;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.LogUtils;

/**
 * Transforms chosen activities to fraud details content.
 */
public class FraudDataTransformActivityToFraudDetailsProcessor extends FraudDataProcessor {

    private static final TypeReference<List<? extends Transaction>> TRANSACTION_LIST_TYPE_REFERENCE = new TypeReference<List<? extends Transaction>>() {
    };
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<String> fraudActivityTypes =
            Sets.newHashSet("double-charge", "large-expense");

    private static final LogUtils log = new LogUtils(FraudDataTransformActivityToFraudDetailsProcessor.class);

    @Override
    public void process(FraudDataProcessorContext context) {

        // Find activities that should be fraud warnings.

        Iterable<Activity> fraudActivities = Iterables.filter(context.getActivities(),
                a -> fraudActivityTypes.contains(a.getType()));

        // Transform activity to fraudDetailsContent. Only include last week.

        final Date cutOffDate = DateUtils.addWeeks(new Date(), -1);

        log.info(
                context.getUser().getId(),
                "Activity size to transform to fraud details is "
                        + Iterables.size(fraudActivities));

        try {
            Iterable<FraudDetailsContent> transactionFraudDetails = Iterables.transform(
                    Iterables.filter(fraudActivities, a -> a.getDate().after(cutOffDate)),
                    a -> {
                        FraudTransactionContent transactionContent = new FraudTransactionContent();

                        switch (a.getType()) {
                        case Activity.Types.DOUBLE_CHARGE:
                            List<Transaction> transactionList = mapper.convertValue(a.getContent(),
                                    TRANSACTION_LIST_TYPE_REFERENCE);
                            transactionContent.setTransactionIds(Lists.newArrayList(Iterables.transform(
                                    transactionList, Transaction::getId)));
                            transactionContent.setTransactions(Lists.newArrayList(Iterables.transform(
                                    transactionList, FraudTransactionEntity::new)));

                            transactionContent.setContentType(FraudDetailsContentType.DOUBLE_CHARGE);
                            return transactionContent;
                        case Activity.Types.LARGE_EXPENSE:
                            Transaction largeExpenseTransaction = mapper.convertValue(a.getContent(),
                                    Transaction.class);
                            transactionContent.setContentType(FraudDetailsContentType.LARGE_EXPENSE);
                            transactionContent.setTransactionIds(Lists.newArrayList(largeExpenseTransaction.getId()));
                            transactionContent.setTransactions(
                                    Lists.newArrayList(new FraudTransactionEntity(largeExpenseTransaction)));
                            return transactionContent;
                        }
                        return transactionContent;
                    });

            log.info(
                    context.getUser().getId(),
                    "Transformed fraud details size is "
                            + Iterables.size(transactionFraudDetails));

            context.addFraudDetailsContent(Lists.newArrayList(transactionFraudDetails));
        } catch (Exception e) {
            log.error(context.getUser().getId(), "Could not transform activities to fraud details content.", e);
        }
    }
}
