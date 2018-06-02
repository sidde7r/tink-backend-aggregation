package se.tink.backend.abnamro.workers.activity.generators;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.abnamro.workers.activity.generators.models.AbnAmroAutomaticSavingsSummaryActivityData;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.guavaimpl.Orderings;

public class AbnAmroAutomaticSavingsSummaryActivityGenerator extends ActivityGenerator {
    
    private static final int MAX_NUMBER_OF_WEEKS = 6;
    
    public AbnAmroAutomaticSavingsSummaryActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(AbnAmroAutomaticSavingsSummaryActivityGenerator.class, 90, 90, deepLinkBuilderFactory);
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {

        // Retrieve all automatic saving transactions.

        final Iterable<Transaction> automaticSavingTransactions = Iterables.filter(context.getTransactions(),
                transaction -> {

                    // Only transfers qualify.
                    if (Objects.equal(CategoryTypes.TRANSFERS, transaction.getCategoryType())) {

                        // Automatic saving transactions have "Pinsparen" as Omschijving ("description"), which is
                        // persisted as a transaction message in the payload.
                        String message = transaction.getPayloadValue(TransactionPayloadTypes.MESSAGE);

                        if (!Strings.isNullOrEmpty(message)) {
                            return "pinsparen".equalsIgnoreCase(message);
                        }
                    }

                    return false;
                });
        
        // No automatic saving transactions. Bail.
        if (Iterables.isEmpty(automaticSavingTransactions)) {
            return;
        }
        
        // Group transactions by week.
        
        final int firstDayOfWeek = I18NUtils.getFirstDayOfWeek(context.getMarket());
        
        final Calendar calendar = DateUtils.getCalendar();
        calendar.setFirstDayOfWeek(firstDayOfWeek);
        
        final ImmutableListMultimap<String, Transaction> automaticSavingTransactionsByWeek = Multimaps.index(
                automaticSavingTransactions, transaction -> {
                    calendar.setTime(transaction.getDate());
                    return getYearAndWeek(calendar);
                });
        
        // Construct the weekly boundary calendars.
        
        Calendar weekStartCalendar = DateUtils.getCalendar();
        
        while (firstDayOfWeek != weekStartCalendar.get(Calendar.DAY_OF_WEEK)) {
            weekStartCalendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        
        Calendar weekEndCalendar = DateUtils.getCalendar(weekStartCalendar.getTime());
        weekEndCalendar.add(Calendar.WEEK_OF_YEAR, 1);
        weekEndCalendar.add(Calendar.DAY_OF_YEAR, -1);
        
        DateUtils.setInclusiveStartTime(weekStartCalendar); // 00:00:00
        DateUtils.setInclusiveEndTime(weekEndCalendar); // 23:59:59
        
        for (int i = 0; i < MAX_NUMBER_OF_WEEKS; i++) {
        
            final String yearWeek = getYearAndWeek(weekStartCalendar);
            
            List<Transaction> transactions = automaticSavingTransactionsByWeek.get(yearWeek);
            
            if (transactions != null && !transactions.isEmpty()) {

                // Sort the transactions in descending order.
                transactions = transactions.stream()
                        .sorted(Orderings.TRANSACTION_DATE_ORDERING.reversed())
                        .collect(Collectors.toCollection(ArrayList::new));
                
                Date endDate;
                String title;
                String message;
                
                // Current week
                if (i == 0) {
                    // The first transaction is the most current one.
                    endDate = transactions.get(0).getDate();
                    message = context.getCatalog().getString("This week");
                } else {
                    endDate = weekEndCalendar.getTime();
                    message = Catalog.format(context.getCatalog().getString("Week {0}"),
                            weekEndCalendar.get(Calendar.WEEK_OF_YEAR));
                }
                
                double amount = 0;
                int count = 0;
                
                for (Transaction transaction : transactions) {
                    if (transaction.getAmount() < 0) {
                        amount += Math.abs(transaction.getAmount()); 
                        count++;
                    }
                }
                
                title = Catalog.format("{0} pinsparen", count);
                
                String key = String.format("%s.week%s", Activity.Types.AUTOMATIC_SAVINGS_SUMMARY_ABNAMRO, yearWeek);
                String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);
                
                AbnAmroAutomaticSavingsSummaryActivityData data = new AbnAmroAutomaticSavingsSummaryActivityData();
                data.setAmount(amount);
                data.setCount(count);
                data.setEndDate(endDate);
                data.setStartDate(weekStartCalendar.getTime());
                data.setTransactions(transactions);
                
                context.addActivity(
                        createActivity(
                                context.getUser().getId(),
                                endDate,
                                Activity.Types.AUTOMATIC_SAVINGS_SUMMARY_ABNAMRO,
                                title,
                                message,
                                data,
                                key,
                                feedActivityIdentifier),
                        transactions);
            }
            
            weekStartCalendar.add(Calendar.WEEK_OF_YEAR, -1);
            weekEndCalendar.add(Calendar.WEEK_OF_YEAR, -1);
        }        
    }
    
    private String getYearAndWeek(Calendar calendar) {
        return String.format("%d-%d", calendar.getWeekYear(), calendar.get(Calendar.WEEK_OF_YEAR));
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
