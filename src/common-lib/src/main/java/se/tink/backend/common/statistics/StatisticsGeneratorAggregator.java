package se.tink.backend.common.statistics;

import com.google.api.client.util.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.statistics.factory.ExtendedPeriodFunctionFactory;
import se.tink.backend.common.statistics.factory.PeriodFunctionFactory;
import se.tink.backend.common.statistics.factory.TransactionStatisticTransformerFactory;
import se.tink.backend.common.statistics.functions.MonthlyAdjustedPeriodizationFunction;
import se.tink.backend.common.statistics.functions.TransactionStatisticTransformer;
import se.tink.backend.common.statistics.functions.TransactionStatisticsTransformationFunction;
import se.tink.backend.common.statistics.predicates.TransactionPredicate;
import se.tink.backend.common.utils.DataUtils;
import se.tink.backend.common.workers.statistics.AggregateStatisticsRequest;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.property.Property;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class StatisticsGeneratorAggregator {
    private static final String LOAN_RATES_BY_ACCOUNT = "loan-rates-by-account";
    private static final LogUtils log = new LogUtils(StatisticsGeneratorAggregator.class);
    private static final Ordering<Loan> ORDERING_NATURAL_LOAN = Ordering.natural();
    private static final ImmutableList<String> leftToSpendAverageStatisticsTypes = ImmutableList.of(CategoryTypes.EXPENSES.toString(), CategoryTypes.INCOME.toString());


    public static List<Statistic> calculateIncomeAndExpensesAndTransfers(User user, List<Credentials> credentials,
            List<Account> accounts, List<Transaction> transactions, List<Category> categories,
            boolean disregardAccountOwnership, CategoryConfiguration categoryConfiguration) {

        UserData userData = new UserData();
        userData.setUser(user);
        userData.setCredentials(credentials);
        userData.setAccounts(accounts);
        userData.setTransactions(transactions);

        List<Transaction> transactionsFiltered = Lists.newArrayList(Iterables.filter(userData.getTransactions(),
                new TransactionPredicate(accounts, categories, categoryConfiguration)));

        MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction = new MonthlyAdjustedPeriodizationFunction(
                user.getProfile().getPeriodAdjustedDay());

        return StatisticsGeneratorAggregator.aggregateUserTransactionStatistics(userData,
                Statistic.Types.INCOME_AND_EXPENSES_AND_TRANSFERS, user.getProfile().getPeriodMode(),
                transactionsFiltered, transactions,
                StatisticsGeneratorFunctions.STATISTICS_SUM_FUNCTION, monthlyAdjustedPeriodizationFunction,
                StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION, disregardAccountOwnership, new TransactionStatisticsTransformationFunction() {
                    @Override
                    public String description(Transaction t) {
                        return t.getCategoryType().name();
                    }
                });
    }

    static List<Statistic> aggregateTransactionStatistics(ResolutionTypes resolution,
            List<Transaction> filteredTransactions, AggregateStatisticsRequest request, String logSubject,
            List<Transaction> unfilteredTransactions) {

        Function<Transaction, Statistic> transformationFunction = Preconditions
                .checkNotNull(request.getTransformationFunction());
        List<AggregateStatisticsRequest.GroupReducePair> groupReducePairs = Preconditions
                .checkNotNull(request.getGroupReducePairs());
        TransactionStatisticTransformerFactory transformerFactory = Preconditions
                .checkNotNull(request.getTransformerFactory());
        String type = Preconditions.checkNotNull(request.getType());
        Preconditions.checkNotNull(filteredTransactions);
        Preconditions.checkArgument(groupReducePairs.size() > 0);

        log.trace(
                logSubject,
                String.format("Generating transaction statistics: type=%s, resolution=%s", type,
                        resolution.toString()));

        // Transform the transactions to statistics objects.
        TransactionStatisticTransformer transformer = transformerFactory.createTransformer(type, resolution,
                transformationFunction, request.isDisregardAccountOwnership());

        List<Statistic> statistics = filteredTransactions.stream().map(transformer).flatMap(List::stream)
                .collect(Collectors.toList());

        /* If a user has set all transactions to excluded for a period, we add statistics to those days that are
           missing transactions. By comparing with the unfiltered transaction we can see how many days we miss
           statistics for. */
        if (unfilteredTransactions != null) {
            /*
             * This will not generate statistics for empty periods that do not contain transactions at all.
             * E.g. if a period contains only one transaction, statistics is generated, and then if that transaction
             * is somehow deleted nothing will be generated to replace the now empty period.
             */
            List<Statistic> originalStatistics = unfilteredTransactions.stream().map(transformer)
                    .flatMap(List::stream).collect(Collectors.toList());

            List<Statistic> emptyStatisticsToAdd = getStatisticsForEmptyPeriod(originalStatistics, statistics);

            statistics.addAll(emptyStatisticsToAdd);
        }

        for (AggregateStatisticsRequest.GroupReducePair pair : groupReducePairs) {
            // XXX: Bug. Seems like most groupers are using hashing to group the stuff to be reduced. On hash
            // collisions we will not calculate correct statistics here since we'll group too much into the same bucket.
            ImmutableListMultimap<Integer, Statistic> grouped = Multimaps.index(statistics, pair.grouper::apply);

            List<Statistic> tmpReduced = Lists.newArrayList();
            for (Integer key : grouped.keySet()) {
                tmpReduced.addAll(pair.reducer.apply(grouped.get(key)));
            }

            statistics = tmpReduced;
        }

        return statistics;
    }

    public static List<Statistic> getStatisticsForEmptyPeriod(
            List<Statistic> originalStatistics,
            List<Statistic> reducedStatistic) {

        List<Statistic> emptyStatisticsToAdd = Lists.newArrayList();

        for (Statistic statistic : originalStatistics) {

            if (!reducedStatistic.contains(statistic)) {

                Statistic newEmptystatisticWithValueZero = new Statistic();

                newEmptystatisticWithValueZero.setType(statistic.getType());
                newEmptystatisticWithValueZero.setResolution(statistic.getResolution());
                newEmptystatisticWithValueZero.setDescription(statistic.getDescription());
                newEmptystatisticWithValueZero.setUserId(statistic.getUserId());
                newEmptystatisticWithValueZero.setPeriod(statistic.getPeriod());
                newEmptystatisticWithValueZero.setPayload(null);
                newEmptystatisticWithValueZero.setValue(0);

                emptyStatisticsToAdd.add(newEmptystatisticWithValueZero);
            }
        }

        return emptyStatisticsToAdd;
    }

    public static List<Statistic> aggregateUserTransactionStatistics(UserData userData, String type,
            ResolutionTypes resolution,
            List<Transaction> transactions, List<Transaction> unfilteredTransactions,
            Function<Collection<Statistic>, Collection<Statistic>> reducerFunction,
            MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction,
            Function<Statistic, Integer> groupFunction, Function<Transaction, Statistic> transformationFunction) {

        return aggregateUserTransactionStatistics(userData, type, resolution, transactions, unfilteredTransactions,
                reducerFunction, monthlyAdjustedPeriodizationFunction, groupFunction, false, transformationFunction);
    }

    static List<Statistic> aggregateUserTransactionStatistics(UserData userData, String type,
            ResolutionTypes resolution,
            List<Transaction> transactions, List<Transaction> unfilteredTransactions,
            Function<Collection<Statistic>, Collection<Statistic>> reducerFunction,
            MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction,
            Function<Statistic, Integer> groupFunction, boolean disregardAccountOwnership,
            Function<Transaction, Statistic> transformationFunction) {

        PeriodFunctionFactory periodFunctionFactory = new ExtendedPeriodFunctionFactory(userData.getUser().getProfile()
                .getLocale(), monthlyAdjustedPeriodizationFunction);

        TransactionStatisticTransformerFactory transformerFactory = new TransactionStatisticTransformerFactory(
                userData.getAccounts(), periodFunctionFactory, userData.getUser().getId());

        AggregateStatisticsRequest request = new AggregateStatisticsRequest(type)
                .setTransformerFactory(transformerFactory)
                .setTransformationFunction(transformationFunction).addGroupReduce(groupFunction, reducerFunction)
                .setDisregardAccountOwnership(disregardAccountOwnership);

        return aggregateTransactionStatistics(resolution, transactions, request,
                userData.getUser().getId(), unfilteredTransactions);
    }

    static List<Statistic> aggregateAccountBalances(
            final UserData userData,
            final String type,
            final ResolutionTypes resolution,
            final MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction,
            Function<AccountBalance, String> transformerFunction) {

        return aggregateAccountBalances(
                userData,
                type,
                resolution,
                monthlyAdjustedPeriodizationFunction,
                transformerFunction,
                null, // No grouping function
                null); // No sum function
    }

    static List<Statistic> aggregateAccountBalances(
            final UserData userData,
            final String type,
            final ResolutionTypes resolution,
            final MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction,
            Function<AccountBalance, String> grouping,
            Function<Statistic, Integer> aggregationGrouping,
            BinaryOperator<Statistic> aggregationReducing) {

        log.trace(
                userData.getUser().getId(),
                String.format("Generating account balance statistics: type=%s, resolution=%s", type,
                        resolution.toString()));

        final String locale = userData.getUser().getProfile().getLocale();
        final Function<Date, String> finalPeriodFunction = new ExtendedPeriodFunctionFactory(locale,
                monthlyAdjustedPeriodizationFunction).getPeriodFunction(resolution);

        List<Statistic> statistics = userData.getAccountBalanceHistory().stream()
                .collect(Collectors.groupingBy(grouping,
                        Collectors.groupingBy(b -> finalPeriodFunction.apply(DateUtils.fromInteger(b.getDate())),
                                Collectors.groupingBy(AccountBalance::getAccountId,
                                        Collectors.maxBy(Orderings.ACCOUNT_BALANCE_HISTORY_ORDERING)))))
                .entrySet().stream()
                .flatMap(g -> {
                    List<Statistic> groupStatistics = g.getValue().entrySet().stream().map(p -> {
                        Statistic s = new Statistic();
                        s.setDescription(g.getKey());
                        s.setPeriod(p.getKey());
                        s.setResolution(resolution);
                        s.setType(type);
                        s.setUserId(userData.getUser().getId());
                        s.setValue(p.getValue().entrySet().stream().map(Map.Entry::getValue).map(Optional::get)
                                .mapToDouble(AccountBalance::getBalance).sum());
                        return s;
                    }).collect(Collectors.toList());

                    // TODO: The flat fill is probably quite expensive. If one could presume the input data to be complete (i.e.
                    // that the account balance history is exhaustive), the `flatFill` wouldn't be needed.
                    return DataUtils.flatFill(groupStatistics, resolution, false).stream();
                })
                .collect(Collectors.toList());

        // Group and reduce statistics if group and sum functions are provided
        if (aggregationGrouping == null || aggregationReducing == null) {
            return statistics;
        }

        return statistics.stream()
                .collect(Collectors.groupingBy(aggregationGrouping, Collectors.reducing(aggregationReducing)))
                .values()
                .stream()
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    static List<Statistic> aggregateLoanStatistics(UserData userData, final ResolutionTypes resolution) {
        // Create LOAN_RATES_BY_ACCOUNT.

        Multimap<String, Statistic> loanRatesStatisticsByAccountId = ArrayListMultimap.create();

        MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction = new MonthlyAdjustedPeriodizationFunction(
                userData.getUser().getProfile().getPeriodAdjustedDay());
        final String locale = userData.getUser().getProfile().getLocale();
        final Function<Date, String> periodFunction = new ExtendedPeriodFunctionFactory(locale,
                monthlyAdjustedPeriodizationFunction).getPeriodFunction(resolution);

        for (Map.Entry<String, Collection<Loan>> loansForAccount : userData.getLoanDataByAccount().asMap().entrySet()) {
            String accountId = loansForAccount.getKey();

            ImmutableListMultimap<String, Loan> loansForAccountByPeriod = FluentIterable.from(
                    loansForAccount.getValue()).index(loan -> periodFunction.apply(loan.getUpdated()));

            List<Statistic> statisticsForAccount = Lists.newArrayList();

            for (Map.Entry<String, Collection<Loan>> loansByAccount : loansForAccountByPeriod.asMap().entrySet()) {
                String period = loansByAccount.getKey();
                Loan loan = ORDERING_NATURAL_LOAN.max(loansByAccount.getValue());

                // If interest is null, don't include in statistics
                if (loan.getInterest() != null) {
                    Statistic s = new Statistic();

                    s.setUserId(userData.getUser().getId());
                    s.setResolution(resolution);
                    s.setPeriod(period);
                    s.setType(LOAN_RATES_BY_ACCOUNT);
                    s.setDescription(accountId);
                    s.setValue(loan.getInterest());

                    statisticsForAccount.add(s);
                }
            }

            loanRatesStatisticsByAccountId.putAll(
                    accountId, DataUtils.flatFill(statisticsForAccount, resolution, false));
        }

        List<Statistic> loanStatistics = new ArrayList<>(loanRatesStatisticsByAccountId.values());

        // Create LOAN_RATES_BY_PROPERTY and LOAN_BALANCES_BY_PROPERTY.

        ImmutableListMultimap<String, Statistic> loanBalancesStatisticsByAccountId = FluentIterable
                .from(userData.getStatistics())
                .filter(Predicates.statisticForTypeAndResolution(Statistic.Types.BALANCES_BY_ACCOUNT, resolution))
                .index(StatisticsGeneratorFunctions.STATISTICS_DESCRIPTION_FUNCTION::apply);

        Map<String, Map<String, Statistic>> loanRatesStatisticsByAccountIdAndPeriod = Maps.newHashMap();
        Map<String, Map<String, Statistic>> loanBalancesStatisticsByAccountIdAndPeriod = Maps.newHashMap();

        for (String accountId : loanRatesStatisticsByAccountId.keySet()) {
            loanRatesStatisticsByAccountIdAndPeriod.put(accountId,
                    FluentIterable.from(loanRatesStatisticsByAccountId.get(accountId))
                            .uniqueIndex(Statistic::getPeriod));
            loanBalancesStatisticsByAccountIdAndPeriod.put(accountId,
                    FluentIterable.from(loanBalancesStatisticsByAccountId.get(accountId))
                            .uniqueIndex(Statistic::getPeriod));
        }

        if (userData.getProperties() != null && !userData.getProperties().isEmpty()) {
            Multimap<String, Statistic> loanStatisticsByPropertyId = ArrayListMultimap.create();

            for (Property property : userData.getProperties()) {
                if (property.getLoanAccountIds() == null || property.getLoanAccountIds().isEmpty()) {
                    continue;
                }

                Set<String> periods = Sets.newHashSet();

                for (String accountId : property.getLoanAccountIds()) {
                    if (!loanRatesStatisticsByAccountIdAndPeriod.containsKey(accountId)) {
                        log.info(userData.getUser().getId(), "Could not find loan rates for account: " + accountId);
                        continue;
                    }

                    periods.addAll(loanRatesStatisticsByAccountIdAndPeriod.get(accountId).keySet());
                }

                for (String period : periods) {
                    double balance = 0;
                    double interest = 0;

                    for (String accountId : property.getLoanAccountIds()) {
                        if (!loanRatesStatisticsByAccountIdAndPeriod.containsKey(accountId) ||
                                !loanBalancesStatisticsByAccountIdAndPeriod.containsKey(accountId)) {
                            log.warn(userData.getUser().getId(),
                                    "Could not find loan rates/balances for account: " + accountId);
                            continue;
                        }

                        Statistic loanRateStatistic = loanRatesStatisticsByAccountIdAndPeriod.get(accountId)
                                .get(period);

                        Statistic loanBalanceStatistic = loanBalancesStatisticsByAccountIdAndPeriod.get(accountId)
                                .get(period);


                        if (loanRateStatistic == null) {
                            log.warn(userData.getUser().getId(), "Loan rate statistics not generated for account: " + accountId + ", and period: " + period);
                        }

                        if (loanBalanceStatistic == null) {
                            log.warn(userData.getUser().getId(), "Loan balance statistics not generated for account: " + accountId + ", and period: " + period);
                        }

                        if (loanRateStatistic == null || loanBalanceStatistic == null) {
                            continue;
                        }

                        balance += loanBalanceStatistic.getValue();
                        interest += loanBalanceStatistic.getValue() * loanRateStatistic.getValue();
                    }

                    if (balance == 0) {
                        continue;
                    }

                    double interestRate = interest / balance;

                    Statistic s = new Statistic();
                    s.setUserId(userData.getUser().getId());
                    s.setResolution(resolution);
                    s.setPeriod(period);
                    s.setType(Statistic.Types.LOAN_RATES_BY_PROPERTY);
                    s.setDescription(property.getId());
                    s.setValue(interestRate);
                    loanStatisticsByPropertyId.put(property.getId(), s);

                    s = new Statistic();
                    s.setUserId(userData.getUser().getId());
                    s.setResolution(resolution);
                    s.setPeriod(period);
                    s.setType(Statistic.Types.LOAN_BALANCES_BY_PROPERTY);
                    s.setDescription(property.getId());
                    s.setValue(balance);

                    loanStatisticsByPropertyId.put(property.getId(), s);
                }
            }

            loanStatistics.addAll(loanStatisticsByPropertyId.values());
        }

        return loanStatistics;
    }


    static List<Statistic> aggregateStatisticsBasedStatistics(
            final UserData userData,
            final String statisticType,
            final ResolutionTypes resolution,
            final MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction,
            Function<? super Collection<Statistic>, Collection<Statistic>> reducerFunction,
            Function<Statistic, Integer> aggregationGrouping) {

        if (log.isTraceEnabled()) {
            log.trace(
                    userData.getUser().getId(),
                    String.format("Generating statistics based statistics: type=%s, resolution=%s", statisticType,
                            resolution.toString()));
        }

        final String locale = userData.getUser().getProfile().getLocale();
        final Function<Date, String> finalPeriodFunction = new ExtendedPeriodFunctionFactory(locale,
                monthlyAdjustedPeriodizationFunction).getPeriodFunction(resolution);

        return userData.getStatistics().stream()
                .filter(s -> s.getType().equals(Statistic.Types.INCOME_AND_EXPENSES))
                .filter(s -> leftToSpendAverageStatisticsTypes.contains(s.getDescription()))
                .filter(s -> s.getResolution().equals(ResolutionTypes.DAILY))
                .map(s -> {
                    // Use TransformerFactory instead?
                    Statistic newStatistic = Statistic.copyOf(s);
                    newStatistic.setDescription(newStatistic.getPeriod());
                    newStatistic.setType(statisticType);
                    newStatistic.setResolution(resolution);
                    try {
                        newStatistic.setPeriod(finalPeriodFunction.apply(
                                ThreadSafeDateFormat.FORMATTER_DAILY.parse(newStatistic.getPeriod())));
                    } catch (ParseException e) {
                        throw new RuntimeException("Could not parse period.", e);
                    }
                    return newStatistic;
                })
                .collect(Collectors.groupingBy(aggregationGrouping))
                .values()
                .stream()
                .map(reducerFunction::apply)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
