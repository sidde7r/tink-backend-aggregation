package se.tink.backend.common.workers.fraud.processors;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudTransactionContent;
import se.tink.libraries.date.Period;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

/**
 * Generates Frequest Account activity events
 */
public class FraudDataFrequentAccountActivityProcessor extends FraudDataProcessor {

    private static final int NUMBER_DAYS_TO_AVERAGE_OVER = 100;
    private static final int NUMBER_DAYS_BACK = 7;

    private static final int THRESHOLD_DATA_POINTS = 30;

    private static final int ABSOLUTE_NUM_THRESHOLD = 5;
    private static final float RELATIVE_STD_THRESHOLD = 2;
    private static final float RELATIVE_MULTIPLYER_THRESHOLD = 3;
    
    private DecimalFormat decimalFormatter = new DecimalFormat();
    private static final LogUtils log = new LogUtils(FraudDataFrequentAccountActivityProcessor.class);

    private Period trainingPeriod;
    private Period testingPeriod;

    public FraudDataFrequentAccountActivityProcessor() {
        Date today = new Date();

        Date trainingLast = org.apache.commons.lang.time.DateUtils.addDays(today, -NUMBER_DAYS_BACK);
        Date trainingFirst = org.apache.commons.lang.time.DateUtils.addDays(trainingLast, -NUMBER_DAYS_TO_AVERAGE_OVER);
        trainingPeriod = new Period(DateUtils.setInclusiveStartTime(trainingFirst),
                DateUtils.inclusiveEndTime(trainingLast));

        Date testingFirst = trainingLast;
        Date testingLast = today;
        testingPeriod = new Period(DateUtils.setInclusiveStartTime(testingFirst), DateUtils.inclusiveEndTime(testingLast));
    }
    
    @Override
    public void process(FraudDataProcessorContext context) {
        Iterable<Account> accounts = context.getAccountsById().values();
        String userId = context.getUser().getId();

        // Filter expense only.

        Iterable<Transaction> transactions = Iterables.filter(context.getTransactionsById().values(),
                t -> t.getCategoryType() == CategoryTypes.EXPENSES);
        
        // Map transactions by account id and by date
        
        Map<String, ListMultimap<String, Transaction>> transactionsByDateByAccount =
                createTransactionsByDateByAccountMap(transactions);

        // Create statistics objects for each account
        
        Map<String, DescriptiveStatistics> statisticsByAccount =
                countDailyTransactionsByAccount(transactionsByDateByAccount, accounts, trainingPeriod, userId);

        // Compare the testing period vs the trained statistics and return those accounts with unusual activity
        
        ListMultimap<String, String> unusualAccountsByDate =
                getUnusualAccountsByDate(transactionsByDateByAccount, statisticsByAccount, testingPeriod);

        log.info(
                context.getUser().getId(),
                "Frequent account size in fraud processor context is "
                        + Iterables.size(unusualAccountsByDate.values()));

        // Create FraudDetailsContent objects from the found unusual accounts
        
        List<FraudDetailsContent> fraudContent = createFraudContent(context, unusualAccountsByDate, transactionsByDateByAccount);

        log.info(
                context.getUser().getId(),
                "Generated fraud details content size for frequent accounts is "
                        + Iterables.size(fraudContent));

        context.addFraudDetailsContent(fraudContent);
    }

    private Map<String, DescriptiveStatistics> countDailyTransactionsByAccount(
            Map<String, ListMultimap<String, Transaction>> transactionsByAccountByDate,
            Iterable<Account> accounts, Period period, String userId) {

        Map<String, DescriptiveStatistics> statisticsByAccount = createDescriptiveStatisticsByAccount(accounts);

        for (String date : transactionsByAccountByDate.keySet()) {

            if (!isDateWithinPeriod(period, date)) {
                continue;
            }

            ListMultimap<String, Transaction> dailyTransactionsByAccount = transactionsByAccountByDate.get(date);

            for (String accountId : statisticsByAccount.keySet()) {

                if (dailyTransactionsByAccount.containsKey(accountId)) {
                    int size = dailyTransactionsByAccount.get(accountId).size();

                    statisticsByAccount.get(accountId).addValue(size);
                }
            }
        }

        // This loop is only here for logging.
        
        for (String accountId : statisticsByAccount.keySet()) {
            DescriptiveStatistics stats = statisticsByAccount.get(accountId);
            if (stats.getMean() > 0) {
                log.debug(userId, "Statistics for " + accountId + ": mean:" + decimalFormatter.format(stats.getMean()) +
                        " std:" + decimalFormatter.format(stats.getStandardDeviation()) + ", numDays:" + stats.getN());
            }
        }
        return statisticsByAccount;
    }

    private ListMultimap<String, String> getUnusualAccountsByDate(
            Map<String, ListMultimap<String, Transaction>> transactionsByAccountByDate,
            Map<String, DescriptiveStatistics> statisticsByAccount, Period period) {

        ListMultimap<String, String> frequentAccountsByDate = ArrayListMultimap.create();

        for (String date : transactionsByAccountByDate.keySet()) {

            if (!isDateWithinPeriod(period, date)) {
                continue;
            }

            ListMultimap<String, Transaction> dailyTransactionsByAccount = transactionsByAccountByDate.get(date);

            for (String accountId : statisticsByAccount.keySet()) {

                if (dailyTransactionsByAccount.containsKey(accountId)) {
                    int count = dailyTransactionsByAccount.get(accountId).size();

                    if (isUnusualActivity(statisticsByAccount.get(accountId), count)) {
                        frequentAccountsByDate.put(date, accountId);
                    }
                }
            }
        }

        return frequentAccountsByDate;
    }

    private boolean isDateWithinPeriod(Period period, String dateString) {
        try {
            Date date = ThreadSafeDateFormat.FORMATTER_DAILY.parse(dateString);
            return period.isDateWithin(date);
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isUnusualActivity(DescriptiveStatistics statistics, int count) {
        boolean unusual = true;

        unusual = unusual && statistics.getN() > THRESHOLD_DATA_POINTS;
        unusual = unusual && count > ABSOLUTE_NUM_THRESHOLD;
        unusual = unusual && count > (statistics.getMean() * RELATIVE_MULTIPLYER_THRESHOLD);
        unusual = unusual && count > (statistics.getMean() + (statistics.getStandardDeviation() * RELATIVE_STD_THRESHOLD));

        return unusual;
    }

    private List<FraudDetailsContent> createFraudContent(
            FraudDataProcessorContext context, ListMultimap<String, String> frequentAccountsByDate,
            Map<String, ListMultimap<String, Transaction>> transactionsByAccountByDate) {

        List<FraudDetailsContent> contents = Lists.newArrayList();

        for (String date : frequentAccountsByDate.keySet()) {
            List<String> accountIds = frequentAccountsByDate.get(date);

            ListMultimap<String, Transaction> dailyTransactionsByAccount = transactionsByAccountByDate.get(date);
            for (String accountId : accountIds) {
                Account account = context.getAccountsById().get(accountId);
                
                List<Transaction> transactions = dailyTransactionsByAccount.get(accountId);

                FraudTransactionContent content = new FraudTransactionContent();
                content.setTransactions(FraudUtils.convertTransacionListToFraudTransactionList(transactions));
                content.setTransactionIds(transformIntoIds(transactions));
                content.setContentType(FraudDetailsContentType.FREQUENT_ACCOUNT_ACTIVITY);
                content.setPayload(account.getName());
                contents.add(content);
            }
        }

        return contents;
    }

    private List<String> transformIntoIds(List<Transaction> transactions) {
        return Lists.newArrayList(Iterables.transform(transactions, Transaction::getId));
    }

    private Map<String, ListMultimap<String, Transaction>> createTransactionsByDateByAccountMap(
            Iterable<Transaction> transactions) {

        ListMultimap<String, Transaction> byDate = Multimaps.index(
                transactions, t -> ThreadSafeDateFormat.FORMATTER_DAILY.format(t.getDate()));

        Map<String, ListMultimap<String, Transaction>> byDateByAccount = Maps.newHashMap();

        for (String date : byDate.keySet()) {
            byDateByAccount.put(date, Multimaps.index(byDate.get(date), new Function<Transaction, String>() {
                @Nullable
                @Override
                public String apply(@Nullable Transaction t) {
                    return t.getAccountId();
                }
            }));
        }

        return byDateByAccount;
    }

    private Map<String, DescriptiveStatistics> createDescriptiveStatisticsByAccount(Iterable<Account> accounts) {
        Map<String, DescriptiveStatistics> statisticsByAccount = Maps.newHashMap();
        for (Account a : accounts) {
            statisticsByAccount.put(a.getId(), new DescriptiveStatistics());
        }
        return statisticsByAccount;
    }
}
