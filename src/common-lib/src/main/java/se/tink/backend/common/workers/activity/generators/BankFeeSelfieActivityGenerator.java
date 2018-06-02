package se.tink.backend.common.workers.activity.generators;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import se.tink.backend.common.bankfees.BankFeeRules;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.repository.cassandra.BankFeeStatisticsRepository;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.renderers.models.BankFeeSelfieData;
import se.tink.backend.core.Activity;
import se.tink.backend.core.BankFeeConstants;
import se.tink.backend.core.BankFeeStatistics;
import se.tink.backend.core.BankFeeType;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.utils.StringUtils;

public class BankFeeSelfieActivityGenerator extends ActivityGenerator {

    private static AtomicReference<Supplier<ImmutableList<BankFeeStatistics>>> bankFees = new AtomicReference<>();
    private BankFeeStatisticsRepository bankFeeStatisticsRepository;

    public BankFeeSelfieActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(BankFeeSelfieActivityGenerator.class, 100, 100, deepLinkBuilderFactory);
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        if (!context.getUser().getFlags().contains(FeatureFlags.TINK_EMPLOYEE)) {
            return;
        }

        bankFeeStatisticsRepository = context.getServiceContext().getRepository(BankFeeStatisticsRepository.class);
        final Catalog catalog = context.getCatalog();
        final int currentYear = DateTime.now().getYear();
        final ListMultimap<BankFeeType, Transaction> transactionsByFeeType = ArrayListMultimap.create();
        Date newestTransactionDate = null;

        FluentIterable<Transaction> transactions = FluentIterable
                .from(context.getTransactions())
                .filter(t -> t.getCategoryType() == CategoryTypes.EXPENSES &&
                        currentYear == new DateTime(t.getDate()).getYear());

        // Find all bank fees
        for (Transaction t : transactions) {
            BankFeeType type = BankFeeRules.getInstance().matchDetails(t.getDescription()).type;

            if (type == null) {
                continue;
            }

            transactionsByFeeType.put(type, t);

            // Keep track of the newest transaction date
            if (newestTransactionDate == null || t.getDate().after(newestTransactionDate)) {
                newestTransactionDate = t.getDate();
            }
        }

        Map<BankFeeType, Double> spendingByType = aggregateByType(transactionsByFeeType);

        BankFeeSelfieData content = new BankFeeSelfieData();
        content.setFeesByType(spendingByType);
        content.setAverageSpendingInTink(getAverageSpendingInTink());
        content.setNewestTransactionDate(newestTransactionDate);

        Double total = content.getTotal();

        String title = catalog.getString("Bank fee selfie");
        String messageFormat = "You have spent {0} kr in bank fees so far this year";
        String message = Catalog.format(context.getCatalog().getString(messageFormat), total);

        String key = String.format("%s-%s", Activity.Types.BANK_SELFIE, currentYear);

        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

        if (total > 0) {
            context.addActivity(
                    createActivity(
                            context.getUser().getId(),
                            DateTime.now().toDate(),
                            Activity.Types.BANK_SELFIE,
                            title,
                            message,
                            content,
                            key,
                            feedActivityIdentifier));
        }
    }

    private Map<BankFeeType, Double> aggregateByType(ListMultimap<BankFeeType, Transaction> map) {

        HashMap<BankFeeType, Double> result = Maps.newHashMap();

        for (BankFeeType key : map.keys()) {

            double sum = 0;
            for (Transaction t : map.get(key)) {
                sum += Math.abs(t.getAmount());
            }

            result.put(key, sum);
        }

        return result;
    }

    private double getAverageSpendingInTink() {

        double total = 0;

        for (BankFeeStatistics fee : getCurrentYearBankFeeStatistics()) {
            total += fee.getAverageAmount();
        }

        return total;
    }

    /**
     * Returns statistics about how much the "average tink user" is spending on bank fees
     */
    private List<BankFeeStatistics> getCurrentYearBankFeeStatistics() {

        // Cache statistics for 30 minutes since they rarely changed
        bankFees.compareAndSet(null, Suppliers.memoizeWithExpiration(
                () -> ImmutableList.copyOf(bankFeeStatisticsRepository.findAllByProviderNameAndYear(
                        BankFeeConstants.Providers.ALL_PROVIDERS, DateTime.now().getYear())), 30, TimeUnit.MINUTES));

        return bankFees.get().get();
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
