package se.tink.backend.insights.transactions;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.User;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserState;
import se.tink.backend.insights.app.queryservices.CategoryQueryService;
import se.tink.backend.insights.core.domain.model.InsightTransaction;
import se.tink.backend.insights.core.valueobjects.Month;
import se.tink.backend.insights.core.valueobjects.MonthlyTransactions;
import se.tink.backend.insights.core.valueobjects.WeeklyTransactions;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.core.valueobjects.Week;
import se.tink.backend.insights.transactions.cache.CachedTransactions;
import se.tink.backend.insights.transactions.mapper.TransactionMapper;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;

public class TransactionQueryServiceImpl implements TransactionQueryService {

    private TransactionDao transactionDao;
    private CachedTransactions cache;
    private UserRepository userRepository;
    private UserStateRepository userStateRepository;
    private CategoryQueryService categoryQueryService;

    @Inject
    public TransactionQueryServiceImpl(TransactionDao transactionDao,
            CachedTransactions cache, UserRepository userRepository,
            UserStateRepository userStateRepository,
            CategoryQueryService categoryQueryService) {
        this.transactionDao = transactionDao;
        this.cache = cache;
        this.userRepository = userRepository;
        this.userStateRepository = userStateRepository;
        this.categoryQueryService = categoryQueryService;
    }

    private List<InsightTransaction> storeInCacheAndReturn(UserId userId, List<InsightTransaction> transactions) {
        cache.save(userId, transactions);
        return transactions;
    }

    public List<InsightTransaction> findAllByUserId(UserId userId) {

        List<InsightTransaction> cachedTransactions = cache.getIfAny(userId);
        if (cachedTransactions != null) {
            return cachedTransactions;
        }
        List<InsightTransaction> transactions = TransactionMapper
                .translate(transactionDao.findAllByUserId(userId.value()));
        return storeInCacheAndReturn(userId, transactions);
    }

    @Override
    public Integer getTransactionsCount(UserId userId) {
        return transactionDao.countByUserId(userId.value());
    }

    public List<InsightTransaction> findForCurrentAndPreviousPeriodByUserId(UserId userId) {

        User user = userRepository.findOne(userId.value());
        UserProfile profile = user.getProfile();
        int periodBreakDay = profile.getPeriodMode().equals(ResolutionTypes.MONTHLY_ADJUSTED) ? profile.getPeriodAdjustedDay() : 1;
        final DateTime startDate = DateTime.now().minusMonths(1).withDayOfMonth(periodBreakDay);
        List<Transaction> collect = transactionDao.findAllByUserIdAndTime(userId.value(), startDate,
                DateTime.now()).stream().filter(t -> t.getOriginalDate().after(startDate.toDate()))
                .collect(Collectors.toList());

        List<InsightTransaction> transactions = TransactionMapper.translate(collect);
        return storeInCacheAndReturn(userId, transactions);
    }

    public WeeklyTransactions findLastWeeksExpenseTransactions(UserId userId){

        Calendar weekStartCalendar = DateUtils.getCalendar(); // DEFAULT_LOCALE for now
        // Start of current week
        weekStartCalendar = DateUtils.getFirstDateOfWeek(weekStartCalendar);
        // Start of previous week
        weekStartCalendar.add(Calendar.DAY_OF_YEAR, -7);
        DateUtils.setInclusiveStartTime(weekStartCalendar);

        Calendar weekEndCalendar = (Calendar) weekStartCalendar.clone();
        weekEndCalendar.add(Calendar.DAY_OF_YEAR, 6);
        DateUtils.setInclusiveEndTime(weekEndCalendar);

        final Date weekEndDate = weekEndCalendar.getTime();
        final Date weekStartDate = weekStartCalendar.getTime();

        Set<String> uninterestedCategoryIds = categoryQueryService.getUninterestingMonthExpenseCategoryIds();
        List<InsightTransaction> transactions = findForCurrentAndPreviousPeriodByUserId(userId)
                .stream()
                .filter(t -> (t.getDate().after(weekStartDate)))
                .filter(t -> (t.getDate().before(weekEndDate)))
                .filter(t -> (Objects.equal(t.getCategoryTypes(), CategoryTypes.EXPENSES)))
                .filter(t -> !uninterestedCategoryIds.contains(t.getCategoryId()))
                .collect(Collectors.toList());
        return WeeklyTransactions.of(transactions, Week.of(weekStartCalendar, weekEndCalendar));
    }

    @Override
    public MonthlyTransactions findPreviousMonthsExpenseTransactions(UserId userId) {
        User user = userRepository.findOne(userId.value());
        UserProfile profile = user.getProfile();

        int periodBreakDay = profile.getPeriodMode().equals(ResolutionTypes.MONTHLY_ADJUSTED) ? profile.getPeriodAdjustedDay() : 1;
        final DateTime startDate = DateTime.now().minusMonths(1).withDayOfMonth(periodBreakDay);
        final DateTime endDate = DateTime.now().withDayOfMonth(periodBreakDay);

        Set<String> uninterestedCategoryIds = categoryQueryService.getUninterestingMonthExpenseCategoryIds();
        List<InsightTransaction> transactions = findForCurrentAndPreviousPeriodByUserId(userId)
                .stream()
                .filter(t -> (t.getDate().after(startDate.toDate())))
                .filter(t -> (t.getDate().before(endDate.toDate())))
                .filter(t -> !uninterestedCategoryIds.contains(t.getCategoryId()))
                .collect(Collectors.toList());
        Calendar monthStartCalendar = DateUtils.getCalendar();
        monthStartCalendar.setTime(startDate.toDate());

        Calendar monthEndCalendar = DateUtils.getCalendar();
        monthEndCalendar.setTime(endDate.toDate());

        return MonthlyTransactions.of(transactions, Month.of(monthStartCalendar, monthEndCalendar));
    }

    @Override
    public void findUpcomingExpenses(UserId userId) {
        // TODO
    }

    public List<InsightTransaction> getInsightTransactionForPeriodByUserId(UserId userId){
        UserState userState = userStateRepository.findOneByUserId(userId.value());

        Date periodStartDate = DateUtils.getCurrentPeriod(userState.getPeriods()).getStartDate();
        Date periodEndDate = DateUtils.getCurrentPeriod(userState.getPeriods()).getEndDate();

        return findForCurrentAndPreviousPeriodByUserId(userId).stream()
                .filter(t -> !t.getDate().before(periodStartDate))
                .filter(t -> !t.getDate().after(periodEndDate))
                .collect(Collectors.toList());
    }
}
