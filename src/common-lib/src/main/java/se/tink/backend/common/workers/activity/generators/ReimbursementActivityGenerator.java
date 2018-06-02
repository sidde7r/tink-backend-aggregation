package se.tink.backend.common.workers.activity.generators;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public abstract class ReimbursementActivityGenerator extends ActivityGenerator {

    private static final long RECENT_TRANSACTION_CUTOFF_MS = 7 * 24 * 60 * 60 * 1000;
    private static final double RECENT_TRANSACTION_FACTOR = 4;
    private static final int RECENT_CUTOFF_DAYS = 42; // 6 weeks required for ABN AMRO (Tink need less).

    protected ReimbursementActivityGenerator(
            Class<? extends ActivityGenerator> generatorClass, double minimumImportance, double maximumImportance,
            DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(generatorClass, minimumImportance, maximumImportance, deepLinkBuilderFactory);
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {

        Date today = DateUtils.getToday();
        Date cutoffDate = DateUtils.addDays(today, -RECENT_CUTOFF_DAYS);

        String refundCategoryCode = context.getCategoryConfiguration().getRefundCode();
        Category refundCategory = context.getCategoriesByCodeForLocale().get(refundCategoryCode);

        List<Transaction> transactions = context.getUnusedTransactions().stream()
                .filter(t -> isCandidate(t, refundCategory)).filter(t -> cutoffDate.before(t.getDate()))
                .collect(Collectors.toList());

        for (final Transaction transaction : transactions) {

            String key = String.format("%s.%s.%s", Activity.Types.REIMBURSEMENT,
                    ThreadSafeDateFormat.FORMATTER_DAILY.format(transaction.getDate()), transaction.getId());

            String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

            context.addActivity(createActivity(
                    context.getUser().getId(),
                    transaction.getDate(),
                    Activity.Types.REIMBURSEMENT,
                    generateTitle(context, transaction),
                    generateMessage(context, transaction),
                    transaction,
                    key,
                    feedActivityIdentifier,
                    calculateTransactionImportance(transaction, today)));
        }
    }

    abstract String generateTitle(ActivityGeneratorContext context, Transaction transaction);
    abstract String generateMessage(ActivityGeneratorContext context, Transaction transaction);

    private double calculateTransactionImportance(Transaction transaction, Date now) {
        if (now.getTime() - transaction.getDate().getTime() <= RECENT_TRANSACTION_CUTOFF_MS) {
            return Math.abs(transaction.getAmount()) * RECENT_TRANSACTION_FACTOR;
        } else {
            return Math.abs(transaction.getAmount());
        }
    }

    protected boolean isCandidate(Transaction transaction, Category refundCategory) {

        // Only incoming transactions.
        if (transaction.getOriginalAmount() < 0) {
            return false;
        }

        // FIXME: Make the categorization command identify incoming Swish and Tikkie transactions and categorize them as refunds.
        // Only reimbursements are eligible for the netting functionality in the client.
//        if (transaction.getOriginalAmount() > 0 && !Objects.equals(transaction.getCategoryId(), refundCategory.getId())) {
//            return false;
//        }

        // If the user set the category oneself, there's no reason to notify the user in the feed.
        if (transaction.isUserModifiedCategory()) {
            return false;
        }

        // The user has already declined to net the transaction.
        if (Objects.equals(transaction.getPayloadValue(TransactionPayloadTypes.LINK_COUNTERPART_PROMPT_ANSWER), "NO")) {
            return false;
        }

        return true;
    }
}
