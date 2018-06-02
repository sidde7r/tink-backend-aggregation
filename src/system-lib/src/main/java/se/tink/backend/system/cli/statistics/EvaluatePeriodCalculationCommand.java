package se.tink.backend.system.cli.statistics;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserState;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;

public class EvaluatePeriodCalculationCommand extends ServiceContextCommand<ServiceConfiguration> {
    private final static int MONTLY_EXPENSE_COUNT_THRESHOLD = 5;

    private static final Ordering<Transaction> TRANSACTION_SORTING_BY_DATE = new Ordering<Transaction>() {
        @Override
        public int compare(Transaction t1, Transaction t2) {
            return t1.getOriginalDate().compareTo(t2.getOriginalDate());
        }
    };

    private AccountRepository accountRepository;
    private static final LogUtils log = new LogUtils(EvaluatePeriodCalculationCommand.class);
    private TransactionDao transactionDao;
    private UserRepository userRepository;

    private UserStateRepository userStateRepository;

    public EvaluatePeriodCalculationCommand() {
        super("evaluate-period-calculation", "Takes a user name and deletes the user and all its data");
    }

    private List<Period> calculatePeriods(UserState userState, UserProfile userProfile,
            Iterable<Transaction> transactions) {

        if (Iterables.isEmpty(transactions)) {
            return Lists.newArrayList();
        }

        Calendar calendar = DateUtils.getCalendar();

        // Figure out the earliest transaction date and create a periods list with 2 buffer months in each
        // direction from that date to today. Need future buffer to find period break date for last period.

        final Date earliestTransactionDate = TRANSACTION_SORTING_BY_DATE.min(transactions).getDate();
        calendar.setTime(earliestTransactionDate);
        calendar.add(Calendar.MONTH, -2);
        Date earliestTransactionDateWithMarginMonth = calendar.getTime();

        final Date today = new Date();
        calendar.setTime(today);
        calendar.add(Calendar.MONTH, 2);
        Date todaysDateWithMarginMonth = calendar.getTime();

        final ResolutionTypes resolution = userProfile.getPeriodMode();
        final int periodBreakDate = userProfile.getPeriodAdjustedDay();

        Map<String, Date> periodDateBreaks = Maps.newHashMap();
        List<String> periodNames = DateUtils.createPeriodList(earliestTransactionDateWithMarginMonth,
                todaysDateWithMarginMonth, ResolutionTypes.MONTHLY, -1);

        // Map transactions by period

        ImmutableListMultimap<String, Transaction> expensesByPeriodName = Multimaps.index(
                Iterables.filter(transactions, t -> t.getCategoryType() == CategoryTypes.EXPENSES), t -> {
                    // FIXME Make it to take `periodDateBreaks` into consideration
                    return DateUtils.getMonthPeriod(t.getDate(), resolution, periodBreakDate);
                });

        // Get the start date of the first (oldest) valid period

        Date oldest = today;

        for (Entry<String, Collection<Transaction>> entry : expensesByPeriodName.asMap().entrySet()) {
            Date date = DateUtils.getFirstDateFromPeriod(entry.getKey(), resolution, periodBreakDate);
            int transactionCount = entry.getValue().size();
            if (transactionCount >= MONTLY_EXPENSE_COUNT_THRESHOLD && date.before(oldest)) {
                oldest = date;
            }
        }

        // Construct the periods list.

        List<Period> periods = generatePeriodList(periodNames, resolution, periodBreakDate, periodDateBreaks);

        for (Period p : periods) {
            p.setClean(oldest.before(p.getEndDate()));
        }

        periods = Lists.newArrayList(Iterables.filter(periods, p -> (p.getEndDate() != null && p.getStartDate() != null
                && p.getEndDate().after(earliestTransactionDate) && p.getStartDate().before(today))));

        return periods;
    }

    private void evaluateUsers(List<User> users) {

        for (User user : users) {

            log.info(user.getId(), "Evaluate period calculation for user");

            UserState userState = userStateRepository.findOneByUserId(user.getId());
            List<Account> accounts = accountRepository.findByUserId(user.getId());
            List<Transaction> transactions = getNonExcludedTransactions(
                    transactionDao.findAllByUserId(user.getId()), accounts);

            List<Period> newPeriods = calculatePeriods(userState, user.getProfile(), transactions);
            List<Period> oldPeriods = userState.getPeriods();
            List<Period> removed = Lists.newArrayList(oldPeriods);
            removed.removeAll(newPeriods);
            List<Period> added = Lists.newArrayList(newPeriods);
            added.removeAll(oldPeriods);

            log.info(user.getId(),
                    String.format("Old periods (%s): %s", oldPeriods.size(), Iterables.toString(oldPeriods)));
            log.info(user.getId(),
                    String.format("New periods (%s): %s", newPeriods.size(), Iterables.toString(newPeriods)));
            log.info(user.getId(), String.format("Removed (%s): %s", removed.size(), Iterables.toString(removed)));
            log.info(user.getId(), String.format("Added (%s): %s", added.size(), Iterables.toString(added)));
        }
    }

    private List<Period> generatePeriodList(List<String> periodNames, ResolutionTypes resolution, int periodBreakDate,
            Map<String, Date> periodDateBreaks) {

        List<Period> periods = Lists.newArrayList();
        Calendar calendar = DateUtils.getCalendar();

        for (int i = 0; i < periodNames.size(); i++) {

            Period period = new Period();
            period.setName(periodNames.get(i));
            period.setResolution(resolution);

            Date periodStartDate = periodDateBreaks.get(periodNames.get(i));

            // If we don't have a detected period start date, just use the default.

            if (periodStartDate == null) {
                periodStartDate = DateUtils.getFirstDateFromPeriod(periodNames.get(i), resolution, periodBreakDate);
            }

            period.setStartDate(periodStartDate);

            if (i > 0) {
                // If this is not the first period, use the start date from the current period as the end date of the
                // previous period.

                calendar.setTime(periodStartDate);
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                DateUtils.setInclusiveEndTime(calendar);

                Period previousPeriod = periods.get(i - 1);
                previousPeriod.setEndDate(calendar.getTime());
            }

            if ((i + 1) == periodNames.size()) {
                // If this is the last period, use the default period end date.
                period.setEndDate(DateUtils.getLastDateFromPeriod(periodNames.get(i), resolution, periodBreakDate));
            }

            periods.add(period);
        }

        return periods;
    }

    private List<Transaction> getNonExcludedTransactions(List<Transaction> transactions, List<Account> accounts) {
        final ImmutableMap<String, Account> accountsById = Maps.uniqueIndex(accounts, Account::getId);

        return Lists.newArrayList(Iterables.filter(transactions,
                t -> (!accountsById.get(t.getAccountId()).isExcluded())));
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        log.info("EvaluatePeriodCalculationCommand: BEGIN");

        userRepository = serviceContext.getRepository(UserRepository.class);
        userStateRepository = serviceContext.getRepository(UserStateRepository.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);

        final String username = System.getProperty("username");
        final String count = System.getProperty("count");

        List<User> users;

        if (!Strings.isNullOrEmpty(username)) {
            users = Lists.newArrayList(userRepository.findOneByUsername(username));
        } else {
            users = userRepository.findAll();

            if (!Strings.isNullOrEmpty(count)) {
                users = Lists.newArrayList(Iterables.limit(users, Integer.parseInt(count)));
            }
        }

        evaluateUsers(users);

        log.info("EvaluatePeriodCalculationCommand: END");
    }
}
